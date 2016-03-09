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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.dto.ambit.AmbitTask;
import org.jaqpot.core.model.dto.ambit.AmbitTaskArray;
import org.jaqpot.core.model.dto.ambit.ProtocolCategory;
import org.jaqpot.core.model.dto.bundle.BundleSubstances;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.data.ConjoinerService;
import org.jaqpot.core.service.data.TrainingService;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.data.PredictionService;
import org.jaqpot.core.service.exceptions.QuotaExceededException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("enm")
@Api(value = "/enm", description = "eNM API")
@Authorize
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
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dataset")
    @ApiOperation(value = "Creates Dataset",
            notes = "Reads Studies from Bundle's Substances, creates Dateaset,"
            + "calculates Descriptors, returns Dataset",
            response = Task.class
    )
    public Response createDataset(
            @ApiParam(name = "data", defaultValue = DEFAULT_DATASET_DATA) DatasetData datasetData,
            @HeaderParam("subjectid") String subjectId) throws QuotaExceededException {

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long datasetCount = datasetHandler.countAllOfCreator(user.getId());
        int maxAllowedDatasets = new UserFacade(user).getMaxDatasets();

        if (datasetCount > maxAllowedDatasets) {
            LOG.info(String.format("User %s has %d algorithms while maximum is %d",
                    user.getId(), datasetCount, maxAllowedDatasets));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + datasetCount + " datasets. "
                    + "No more than " + maxAllowedDatasets + " are allowed with your subscription.");
        }

        String bundleURI = datasetData.getBundle();
        if (bundleURI == null || bundleURI.isEmpty()) {
            throw new BadRequestException("Bundle URI cannot be empty.");
        }

        List<String> descriptors = datasetData.getDescriptors();
        String descriptorsString;
        if (descriptors != null) {
            descriptorsString = serializer.write(descriptors);
        } else {
            descriptorsString = serializer.write(new ArrayList<>());
        }

        Map<String, Object> options = new HashMap<>();
        options.put("bundle_uri", bundleURI);
        options.put("title", datasetData.getTitle());
        options.put("description", datasetData.getDescription());
        options.put("descriptors", descriptorsString);
        options.put("intersect_columns", datasetData.getIntersectColumns() != null ? datasetData.getIntersectColumns() : true);
        options.put("subjectid", subjectId);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("mode", "PREPARATION");
        options.put("retain_null_values", datasetData.getRetainNullValues() != null ? datasetData.getRetainNullValues() : false);
        Task task = conjoinerService.initiatePreparation(options, securityContext.getUserPrincipal().getName());
        return Response.ok(task).build();
    }

    @POST
    @Produces("text/uri-list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/bundle")
    @ApiOperation(value = "Creates Bundle",
            notes = "Reads Substances from SubstanceOwner and creates Bundle.",
            response = String.class
    )
    public Response createBundle(
            @ApiParam(value = "Data for bundle creation", defaultValue = DEFAULT_BUNDLE_DATA, required = true) BundleData bundleData,
            @HeaderParam("subjectid") String subjectId) {

        if (bundleData == null) {
            throw new BadRequestException("Post data cannot be empty");
        }

        String substanceOwner = bundleData.getSubstanceOwner();
        if (substanceOwner == null || substanceOwner.isEmpty()) {
            throw new BadRequestException("Field substanceOwner cannot be empty.");
        }

        String userName = securityContext.getUserPrincipal().getName();

        MultivaluedMap<String, String> formParameters = new MultivaluedHashMap<>();
        formParameters.add("title", "owner-bundle");
        formParameters.add("description", bundleData.getDescription());
        formParameters.add("source", userName);
        formParameters.add("seeAlso", substanceOwner);
        formParameters.add("license", "Copyright of " + userName);
        formParameters.add("rightsHolder", userName);
        formParameters.add("maintainer", userName);
        formParameters.add("stars", "1");

        String[] parts = substanceOwner.split("substanceowner");
        String bundleBaseUri = parts[0] + "bundle";
        AmbitTaskArray ambitTaskArray = client.target(bundleBaseUri)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("subjectid", subjectId)
                .post(Entity.form(formParameters), AmbitTaskArray.class);

        AmbitTask ambitTask = ambitTaskArray.getTask().get(0);
        String ambitTaskUri = ambitTask.getUri();
        while (ambitTask.getStatus().equals("Running")) {
            ambitTaskArray = client.target(ambitTaskUri)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("subjectid", subjectId)
                    .get(AmbitTaskArray.class);
            ambitTask = ambitTaskArray.getTask().get(0);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                continue;
            }
        }
        String bundleUri;
        if (ambitTask.getStatus().equals("Completed")) {
            bundleUri = ambitTask.getResult();
        } else {
            return Response
                    .status(Response.Status.BAD_GATEWAY)
                    .entity(ErrorReportFactory.remoteError(ambitTaskUri, ErrorReportFactory.internalServerError(), null))
                    .build();
        }
        List<String> substances = bundleData.getSubstances();

        if (substances == null || substances.isEmpty()) {
            BundleSubstances ownerSubstances = client.target(substanceOwner + "/substance")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("subjectid", subjectId)
                    .get(BundleSubstances.class);
            substances = ownerSubstances.getSubstance()
                    .stream()
                    .map((s) -> {
                        return s.getURI();
                    })
                    .collect(Collectors.toList());
        }

        for (String substance : substances) {
            formParameters.clear();
            formParameters.putSingle("substance_uri", substance);
            formParameters.putSingle("command", "add");
            client.target(bundleUri + "/substance")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("subjectid", subjectId)
                    .put(Entity.form(formParameters))
                    .close();
        }

        Map<String, List<String>> properties = bundleData.getProperties();

        if (properties == null || properties.isEmpty()) {
            properties = new HashMap<>();
            for (ProtocolCategory category : ProtocolCategory.values()) {
                String topCategoryName = category.getTopCategory();
                String categoryName = category.name();

                if (properties.containsKey(topCategoryName)) {
                    List<String> categoryValues = properties.get(topCategoryName);
                    categoryValues.add(categoryName);
                    properties.put(topCategoryName, categoryValues);
                } else {
                    List<String> categoryValues = new ArrayList<>();
                    categoryValues.add(categoryName);
                    properties.put(topCategoryName, categoryValues);
                }
            }
        }

        for (String topCategory : properties.keySet()) {
            List<String> subCategories = properties.get(topCategory);
            for (String subCategory : subCategories) {
                formParameters.clear();
                formParameters.putSingle("topcategory", topCategory);
                formParameters.putSingle("endpointcategory", subCategory);
                formParameters.putSingle("command", "add");
                client.target(bundleUri + "/property")
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("subjectid", subjectId)
                        .put(Entity.form(formParameters))
                        .close();
            }
        }

        try {
            return Response.created(new URI(bundleUri)).entity(bundleUri).build();
        } catch (URISyntaxException ex) {
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity(ErrorReportFactory.remoteError(bundleUri, ErrorReportFactory.internalServerError(), ex))
                    .build();
        }
    }

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

    public static class BundleData {

        private String description;
        private String substanceOwner;
        private List<String> substances;
        private Map<String, List<String>> properties;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

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
        private String bundle;
        private List<String> descriptors;
        private Boolean intersectColumns;
        private Boolean retainNullValues;

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

        public String getBundle() {
            return bundle;
        }

        public void setBundle(String bundle) {
            this.bundle = bundle;
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
