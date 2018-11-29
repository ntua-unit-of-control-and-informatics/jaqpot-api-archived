/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.jaqpot.core.service.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.dto.ambit.ProtocolCategory;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.client.ambit.Ambit;
import org.jaqpot.core.service.data.ConjoinerService;
import org.jaqpot.core.service.data.PredictionService;
import org.jaqpot.core.service.data.TrainingService;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("enm")
@Api(value = "/enm", description = "eNM API")
public class EnanomapperResource {

    private static final Logger LOG = Logger.getLogger(EnanomapperResource.class.getName());

    @EJB
    ConjoinerService conjoinerService;

    @EJB
    TrainingService trainingService;

    @EJB
    PredictionService predictionService;

    @EJB
    ModelHandler modelHandler;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    UserHandler userHandler;

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    @Inject
    @UnSecure
    Client client;


    @Inject
    Ambit ambitClient;

    @Inject
    @Jackson
    JSONSerializer serializer;

    private static final String DEFAULT_DATASET_DATA = "{\n"
            + "	\"title\" : \"another corona dataset\",\n"
            + "	\"description\" : \"This dataset contains corona data\",\n"
            + "	\"bundle\": \"https://apps.ideaconsult.net/enmtest/bundle/14\",\n"
            + "	\"descriptors\":[\n"
            + "		\"IMAGE\",\n"
            + "		\"EXPERIMENTAL\"\n"
            + "	],\n"
            + " \"intersectColumns\": true\n"
            + "}",
            DEFAULT_BUNDLE_DATA = "{\n"
            + "	\"description\":\"a bundle with protein corona data\",\n"
            + "	\"substanceOwner\":\"https://apps.ideaconsult.net/enmtest/substanceowner/FCSV-B8A9C515-7A79-32A9-83D2-8FF2FEC8ADCB\",\n"
            + "	\"substances\":[\n"
            + "		\"https://apps.ideaconsult.net/enmtest/substance/FCSV-8b479138-4775-3aba-b9cc-f01cc967d42b\",\n"
            + "		\"https://apps.ideaconsult.net/enmtest/substance/FCSV-0e1a05ec-6045-3419-89e5-6e48e1c62e3c\"\n"
            + "	],\n"
            + "	\"properties\":{\n"
            + "		\"P-CHEM\" : [\n"
            + "			\"PC_GRANULOMETRY_SECTION\"	\n"
            + "		]\n"
            + "	}\n"
            + "}";

//    @POST
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @Path("/training")
//    @ApiOperation(value = "Creates Model",
//            notes = "Reads Studies from Bundle's Substances, creates Dateaset,"
//            + "calculates Descriptors, applies Dataset and Parameters on "
//            + "Algorithm and creates Model.",
//            response = Task.class
//    )
//    public Response trainEnmModel(
//            @FormParam("bundle_uri") String bundleURI,
//            @FormParam("algorithm_uri") String algorithmURI,
//            @FormParam("prediction_feature") String predictionFeature,
//            @FormParam("parameters") String parameters,
//            @HeaderParam("subjectid") String subjectId) {
//
//        Map<String, Object> options = new HashMap<>();
//        options.put("bundle_uri", bundleURI);
//        options.put("prediction_feature", predictionFeature);
//        options.put("subjectid", subjectId);
//        options.put("algorithmId", algorithmURI);
//        options.put("parameters", parameters);
//        options.put("base_uri", uriInfo.getBaseUri().toString());
//        options.put("mode", "TRAINING");
//        Task task = conjoinerService.initiatePreparation(options, securityContext.getUserPrincipal().getName());
//        return Response.ok(task).build();
//    }
//    @POST
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @Path("/prediction")
//    @ApiOperation(value = "Creates Prediction",
//            notes = "Reads Studies from Bundle's Substances, creates Dateaset,"
//            + "calculates Descriptors, applies Dataset and Parameters on "
//            + "Algorithm and creates Model.",
//            response = Task.class
//    )
//    public Response makeEnmPrediction(
//            @FormParam("bundle_uri") String bundleURI,
//            @FormParam("model_uri") String modelURI,
//            @HeaderParam("subjectid") String subjectId) {
//
//        Model model = modelHandler.find(modelURI);
//        if (model == null) {
//            throw new NotFoundException("Model not found.");
//        }
//        Map<String, Object> options = new HashMap<>();
//        options.put("bundle_uri", bundleURI);
//        options.put("subjectid", subjectId);
//        options.put("modelId", modelURI);
//        options.put("base_uri", uriInfo.getBaseUri().toString());
//        options.put("mode", "PREDICTION");
//        Task task = conjoinerService.initiatePreparation(options, securityContext.getUserPrincipal().getName());
//        return Response.ok(task).build();
//    }
    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dataset")
    @ApiOperation(value = "Creates Dataset By Study",
            notes = "Reads Studies from Bundle's Substances, creates Dataset,"
            + "calculates Descriptors, returns Dataset",
            response = Task.class

    )
    @org.jaqpot.core.service.annotations.Task
    public Response createDatasetByStudy(
            @ApiParam(name = "Data for dataset creation ", defaultValue = DEFAULT_DATASET_DATA) DatasetData datasetData,
            @HeaderParam("Authorization") String subjectId) throws QuotaExceededException, ExecutionException, InterruptedException,JaqpotDocumentSizeExceededException {

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long datasetCount = datasetHandler.countAllOfCreator(user.getId());
        int maxAllowedDatasets = new UserFacade(user).getMaxDatasets();

        if (datasetCount > maxAllowedDatasets) {
            LOG.info(String.format("User %s has %d datasets while maximum is %d",
                    user.getId(), datasetCount, maxAllowedDatasets));
            throw new QuotaExceededException("Dear " + user.getName()
                    + ", your quota has been exceeded; you already have " + datasetCount + " datasets. "
                    + "No more than " + maxAllowedDatasets + " are allowed with your subscription.");
        }

        List<String> descriptors = datasetData.getDescriptors();
        String descriptorsString;

        if (descriptors != null) {
            descriptorsString = serializer.write(descriptors);
        } else {
            descriptorsString = serializer.write(new ArrayList<>());
        }
        String substancesString;
        if (datasetData.getSubstances() != null) {
            substancesString = serializer.write(datasetData.getSubstances());
        } else {
            substancesString = serializer.write(new ArrayList<>());
        }

        String propertiesString = serializer.write(datasetData.getProperties().values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));


        Map<String, Object> options = new HashMap<>();
        options.put("substance_owner", datasetData.getSubstanceOwner().split("substanceowner/")[1]);
        options.put("substances", substancesString);
        options.put("title", datasetData.getTitle());
        options.put("description", datasetData.getDescription());
        options.put("descriptors", descriptorsString);
        options.put("properties", propertiesString);
        options.put("intersect_columns", datasetData.getIntersectColumns() != null ? datasetData.getIntersectColumns() : true);
        options.put("subjectId", subjectId.split("\\s+")[1]);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("mode", "PREPARATION");
        options.put("retain_null_values", datasetData.getRetainNullValues() != null ? datasetData.getRetainNullValues() : false);
        Task task = conjoinerService.initiatePreparation(options, securityContext.getUserPrincipal().getName());
        return Response.ok(task).build();
    }

  /*@POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/bundle")
    @ApiOperation(value = "Creates Bundle",
            notes = "Reads Substances from SubstanceOwner and creates Bundle.",
            response = String.class
    )
    public Response createBundle(
            @ApiParam(value = "Data for bundle creation", defaultValue = DEFAULT_BUNDLE_DATA, required = true) BundleData bundleData,
            @HeaderParam("subjectId") String subjectId) throws ExecutionException, InterruptedException {
        if (bundleData == null) {
            throw new BadRequestException("Post data cannot be empty");
        }
        String substanceOwner = bundleData.getSubstanceOwner();
        if (substanceOwner == null || substanceOwner.isEmpty()) {
            throw new BadRequestException("Field substanceOwner cannot be empty.");
        }
        String userName = securityContext.getUserPrincipal().getName();
        String result = ambitClient.createBundle(bundleData,userName,subjectId);
       try {
           return Response.created(new URI(result.replaceAll("\\s+",""))).entity(result).build();
        } catch (URISyntaxException ex) {
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity(ErrorReportFactory.remoteError(result, ErrorReportFactory.internalServerError(), ex))
                    .build();
        }
    }*/

    @GET
    @Path("/property/categories")
    @ApiOperation(value = "Retrieves property categories",
            response = Map.class
    )
    public Response getPropertyCategories() {

        Map<String, List<String>> categories = new HashMap<>();

        for (ProtocolCategory category : ProtocolCategory.values()) {
            String topCategoryName = category.getTopCategory();
            String categoryName = category.name();

            if (categories.containsKey(topCategoryName)) {
                List<String> categoryValues = categories.get(topCategoryName);
                categoryValues.add(categoryName);
                categories.put(topCategoryName, categoryValues);
            } else {
                List<String> categoryValues = new ArrayList<>();
                categoryValues.add(categoryName);
                categories.put(topCategoryName, categoryValues);
            }
        }

        return Response.ok(categories).build();
    }

    @GET
    @Path("/descriptor/categories")
    @ApiOperation(value = "Retrieves descriptor calculation categories",
            response = List.class
    )
    public Response getDescriptorCategories() {
        List<DescriptorCategory> descriptorCategories = new ArrayList<>();

        for (Dataset.DescriptorCategory category : Dataset.DescriptorCategory.values()) {
            DescriptorCategory cat = new DescriptorCategory();
            cat.setId(category.name());
            cat.setName(category.getName());
            cat.setDescription(category.getDescription());
            descriptorCategories.add(cat);
        }

        //        DescriptorCategory image = new DescriptorCategory();
        //        image.setId("IMAGE");
        //        image.setName("ImageAnalysis descriptors");
        //        image.setDescription("Descriptors derived from analyzing substance images by the ImageAnalysis software.");
        //
        //        DescriptorCategory go = new DescriptorCategory();
        //        go.setId("GO");
        //        go.setName("GO descriptors");
        //        go.setDescription("Descriptors derived by proteomics data.");
        //
        //        DescriptorCategory mopac = new DescriptorCategory();
        //        mopac.setId("MOPAC");
        //        mopac.setName("Mopac descriptors");
        //        mopac.setDescription("Descriptors derived by crystallographic data.");
        //
        //        DescriptorCategory cdk = new DescriptorCategory();
        //        cdk.setId("CDK");
        //        cdk.setName("CDK descriptors");
        //        cdk.setDescription("Descriptors derived from cdk software.");
        //
        //        descriptorCategories.add(image);
        //        descriptorCategories.add(go);
        //        descriptorCategories.add(mopac);
        //        descriptorCategories.add(cdk);
        return Response.ok(descriptorCategories).build();
    }

    public static class DescriptorCategory {

        private String id;
        private String name;
        private String description;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    public static class DatasetData {

        private String title;
        private String description;
        private String substanceOwner;
        private List<String> substances;
        private Map<String, List<String>> properties;
        private List<String> descriptors;
        private Boolean intersectColumns;
        private Boolean retainNullValues;

        public String getSubstanceOwner() {
            return substanceOwner;
        }

        public void setSubstanceOwner(String substanceOwner) {
            this.substanceOwner = substanceOwner;
        }

        public List<String> getSubstances() {
            return substances;
        }

        public void setSubstances(List<String> substances) {
            this.substances = substances;
        }

        public Map<String, List<String>> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, List<String>> properties) {
            this.properties = properties;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getDescriptors() {
            return descriptors;
        }

        public void setDescriptors(List<String> descriptors) {
            this.descriptors = descriptors;
        }

        public Boolean getIntersectColumns() {
            return intersectColumns;
        }

        public void setIntersectColumns(Boolean intersectColumns) {
            this.intersectColumns = intersectColumns;
        }

        public Boolean getRetainNullValues() {
            return retainNullValues;
        }

        public void setRetainNullValues(Boolean retainNullValues) {
            this.retainNullValues = retainNullValues;
        }

    }

}
