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

//import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.validator.routines.UrlValidator;
import org.jaqpot.core.data.*;
import org.jaqpot.core.data.wrappers.DatasetLegacyWrapper;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.builder.FeatureBuilder;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.EntryId;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.client.jpdi.JPDIClient;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.JaqpotForbiddenException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;
import org.jaqpot.core.service.exceptions.parameter.ParameterInvalidURIException;
import org.jaqpot.core.service.exceptions.parameter.ParameterIsNullException;
import org.jaqpot.core.service.exceptions.parameter.ParameterRangeException;
import org.jaqpot.core.service.exceptions.parameter.ParameterScopeException;
import org.jaqpot.core.service.exceptions.parameter.ParameterTypeException;
import org.jaqpot.core.service.httphandlers.Rights;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import static org.jaqpot.core.util.CSVUtils.parseLine;
import org.jaqpot.core.util.ObjectSizeEstimator;
import xyz.euclia.euclia.accounts.client.models.User;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@Path("/dataset")
//@Api(value = "dataset", description = "Dataset API")
@Produces({"application/json", "text/uri-list"})
@Tag(name = "dataset")
@SecurityScheme(name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        description = "add the token retreived from oidc. Example:  Bearer <API_KEY>"
)
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class DatasetResource {

    private static final Logger LOG = Logger.getLogger(DatasetResource.class.getName());

    @Inject
    PropertyManager propertyManager;

    @EJB
    FeatureHandler featureHandler;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    UserHandler userHandler;

    @Context
    UriInfo uriInfo;

    @EJB
    ModelHandler modelHandler;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    DataEntryHandler dataEntryHandler;

    @EJB
    ReportHandler reportHandler;

    @EJB
    DatasetLegacyWrapper datasetLegacyWrapper;

    @EJB
    Rights rights;

//    @Inject
//    Ambit ambitClient;
    @Inject
    @UnSecure
    Client client;

    @Inject
    JPDIClient jpdiClient;

    @Inject
    PropertyManager properyManager;
    
    @Inject
    ObjectSizeEstimator ob;

    @Context
    SecurityContext securityContext;

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(summary = "Finds all Datasets",
            description = "Finds all Datasets in the DB of Jaqpot and returns them in a list. Results can be obtained "
            + "either in the form of a URI list or as a JSON list as specified by the Accept HTTP header. "
            + "In the latter case, a list will be returned containing only the IDs of the datasets, their metadata "
            + "and their ontological classes. The parameter max, which specifies the maximum number of IDs to be "
            + "listed is limited to 500; if the client specifies a larger value, an HTTP Warning Header will be "
            + "returned (RFC 2616) with code P670.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))),
                        description = "Datasets found and are listed in the response body"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response listDatasets(
            @Parameter(name = "start", description = "start", schema = @Schema(implementation = Integer.class, defaultValue = "0")) @QueryParam("start") Integer start,
            @Parameter(name = "max", description = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", schema = @Schema(implementation = Integer.class, defaultValue = "10")) @QueryParam("max") Integer max,
            @Parameter(name = "existence", description = "description for the dataset", required = false, schema = @Schema(implementation = String.class, allowableValues = {"UPLOADED", "CREATED", "TRANSFORMED", "PREDICTED", "FROMPRETRAINED", "DESCRIPTORS", "ALL"})) @QueryParam("existence") String datasetexistence,
            @Parameter(name = "ontrash", description = "onTrash for the dataset", required = false, schema = @Schema(implementation = Boolean.class, allowableValues = {"true", "false"})) @QueryParam("ontrash") Boolean ontrash,
            @Parameter(name = "organization", description = "organization for the dataset", required = false, schema = @Schema(implementation = String.class)) @QueryParam("organization") String organization,
            @Parameter(name = "byModel", description = "by Model", required = false, schema = @Schema(implementation = String.class)) @QueryParam("byModel") String byModel
    ) {
        start = start != null ? start : 0;
        if (max == null || max > 500) {
            max = 500;
        }
        String creator = securityContext.getUserPrincipal().getName();
        List<Dataset> datasets = new ArrayList();
        Number total = null;
        if (datasetexistence == null || datasetexistence.equals("ALL")) {
            if (organization == null && ontrash == null  && byModel == null) {
                datasets.addAll(datasetHandler.listMetaOfCreator(creator, start, max));
                total = datasetHandler.countAllOfCreator(creator);
            } else if (ontrash != null) {
                List<String> fields = new ArrayList<>();
                fields.add("_id");
                fields.add("meta");
                fields.add("ontologicalClasses");
                fields.add("organizations");
                fields.add("totalRows");
                fields.add("totalColumns");
                Map<String, Object> properties = new HashMap<>();
                properties.put("onTrash", ontrash);
                properties.put("meta.creators", Arrays.asList(creator));
                datasets.addAll(datasetHandler.find(properties, fields, start, max));
                total = datasetHandler.countCreatorsInTrash(creator);
            } else if(byModel != null){
                
                List<String> fields = new ArrayList<>();
                fields.add("_id");
                fields.add("meta");
                fields.add("ontologicalClasses");
                fields.add("organizations");
                fields.add("totalRows");
                fields.add("totalColumns");
                fields.add("features");
                Map<String, Object> properties = new HashMap<>();
                properties.put("byModel", byModel);
                properties.put("visible", true);
                Map<String, Object> neProperties = new HashMap<>();
                neProperties.put("onTrash", true);
                datasets.addAll(datasetHandler.findAllAndNe(properties, neProperties, fields, start, max));
                total = datasetHandler.countCreatorsByModel(creator, byModel);
                
            }
            else {
                List<String> fields = new ArrayList<>();
                fields.add("_id");
                fields.add("meta");
                fields.add("ontologicalClasses");
                fields.add("organizations");
                fields.add("totalRows");
                fields.add("totalColumns");
                Map<String, Object> properties = new HashMap<>();
                properties.put("meta.read", organization);
                //            properties.put("meta.creators", Arrays.asList(creator));
                Map<String, Object> neProperties = new HashMap<>();
                neProperties.put("onTrash", true);
                datasets.addAll(datasetHandler.findAllAndNe(properties, neProperties, fields, start, max));
                total = datasetHandler.countAllOfOrg(organization);
            }
        } else {
            switch (datasetexistence) {
                case "UPLOADED":
                    datasets.addAll(datasetHandler.listDatasetCreatorsExistence(creator, Dataset.DatasetExistence.UPLOADED, start, max));
                    total = datasetHandler.countCreatorsExistenseDatasets(creator, Dataset.DatasetExistence.UPLOADED);
                    break;
                case "CREATED":
                    datasets.addAll(datasetHandler.listDatasetCreatorsExistence(creator, Dataset.DatasetExistence.CREATED, start, max));
                    total = datasetHandler.countCreatorsExistenseDatasets(creator, Dataset.DatasetExistence.CREATED);
                    break;
                case "TRANSFORMED":
                    datasets.addAll(datasetHandler.listDatasetCreatorsExistence(creator, Dataset.DatasetExistence.TRANFORMED, start, max));
                    total = datasetHandler.countCreatorsExistenseDatasets(creator, Dataset.DatasetExistence.TRANFORMED);
                    break;
                case "PREDICTED":
                    if (byModel != null) {
                        datasets.addAll(datasetHandler.listDatasetByModelExistence(creator, byModel, Dataset.DatasetExistence.PREDICTED, start, max));

                    } else {
                        datasets.addAll(datasetHandler.listDatasetCreatorsExistence(creator, Dataset.DatasetExistence.PREDICTED, start, max));
                        total = datasetHandler.countCreatorsExistenseDatasets(creator, Dataset.DatasetExistence.PREDICTED);
                    }
                    break;
                case "PRETRAINED":
                    datasets.addAll(datasetHandler.listDatasetCreatorsExistence(creator, Dataset.DatasetExistence.FROMPRETRAINED, start, max));
                    total = datasetHandler.countCreatorsExistenseDatasets(creator, Dataset.DatasetExistence.FROMPRETRAINED);
                    break;
                case "DESCRIPTORS":
                    datasets.addAll(datasetHandler.listDatasetCreatorsExistence(creator, Dataset.DatasetExistence.DESCRIPTORSADDED, start, max));
                    total = datasetHandler.countCreatorsExistenseDatasets(creator, Dataset.DatasetExistence.DESCRIPTORSADDED);
                    break;
            }

        }

        return Response.ok(datasets)
                .status(Response.Status.OK)
                .header("total", total)
                .build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({"text/csv", MediaType.APPLICATION_JSON})

    @Path("{id}")

    @Operation(summary = "Finds Dataset by Id",
            description = "Finds specified Dataset",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Dataset.class)),
                        description = "Dataset was found"),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset was not found in the system"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response getDataset(
            //@ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id,
            @QueryParam("dataEntries") Boolean dataEntries,
            @QueryParam("rowStart") Integer rowStart,
            @QueryParam("rowMax") Integer rowMax,
            @QueryParam("colStart") Integer colStart,
            @QueryParam("colMax") Integer colMax,
            @QueryParam("stratify") String stratify,
            @QueryParam("seed") Long seed,
            @QueryParam("folds") Integer folds,
            @QueryParam("target_feature") String targetFeature) {
        Dataset dataset = null;
        if (dataEntries == null) {
            dataEntries = false;
        }
        if (dataEntries) {
            dataset = datasetLegacyWrapper.find(id, rowStart, rowMax, colStart, colMax);
        } else {
            dataset = datasetHandler.find(id);
        }
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        return Response.ok(dataset).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/featured")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(summary = "Finds all Datasets",
            description = "Finds Featured Datasets in the DB of Jaqpot and returns them in a list. Results can be obtained "
            + "either in the form of a URI list or as a JSON list as specified by the Accept HTTP header. "
            + "In the latter case, a list will be returned containing only the IDs of the datasets, their metadata "
            + "and their ontological classes. The parameter max, which specifies the maximum number of IDs to be "
            + "listed is limited to 500; if the client specifies a larger value, an HTTP Warning Header will be "
            + "returned (RFC 2616) with code P670.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))),
                        description = "Datasets found and are listed in the response body"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response listFeaturedDatasets(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "start", description = "start", schema = @Schema(implementation = Integer.class, defaultValue = "0")) @QueryParam("start") Integer start,
            @Parameter(name = "max", description = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", schema = @Schema(implementation = Integer.class, defaultValue = "10")) @QueryParam("max") Integer max
    ) {
        start = start != null ? start : 0;
        boolean doWarnMax = false;
        if (max == null || max > 500) {
            max = 500;
        }
        return Response.ok(datasetHandler.findFeatured(start, max))
                .status(Response.Status.OK)
                .header("total", datasetHandler.countFeatured())
                .build();

    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/features")
    @Operation(summary = "Finds Features of Dataset by Id",
            description = "Finds specified Dataset's features",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Dataset.class)),
                        description = "Dataset's features found and are listed in the response body"),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset was not found in the system"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response getDatasetFeatures(
            //@ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id
    ) {
        Dataset dataset = datasetHandler.find(id);
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        return Response.ok(dataset.getFeatures()).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/meta")
    @Operation(summary = "Finds MetaData of Dataset by Id",
            description = "Finds specified Dataset's MetaData",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Dataset.class)),
                        description = "Dataset's meta data found and are listed in the response body"),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset was not found in the system"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response getDatasetMeta(
            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id) {
        Dataset dataset = datasetHandler.find(id);
        dataset.setDataEntry(new ArrayList<>());
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }

        return Response.ok(dataset).build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"text/uri-list", MediaType.APPLICATION_JSON})
    @Operation(summary = "Creates a new Dataset",
            description = "The new Dataset created will be assigned on a random generated Id",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Dataset.class)),
                        description = "Dataset was created succesfully"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset quota has been exceeded"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response createDataset(
            //@ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
            Dataset dataset) throws QuotaExceededException, URISyntaxException, JaqpotDocumentSizeExceededException {

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
//        User user = userHandler.find(securityContext.getUserPrincipal().getName(), apiKey);
        
        
                
        ROG randomStringGenerator = new ROG(true);
        dataset.setId(randomStringGenerator.nextStringId(22));
        dataset.setFeatured(Boolean.FALSE);
        if (dataset.getMeta() == null) {
            dataset.setMeta(new MetaInfo());
        }
        dataset.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
        dataset.setVisible(Boolean.TRUE);
        datasetLegacyWrapper.create(dataset);
        //datasetHandler.create(dataset);

        return Response.created(new URI(dataset.getId())).entity(dataset).build();

    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/empty")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({"text/uri-list", MediaType.APPLICATION_JSON})
    @Operation(summary = "Creates a new empty Dataset",
            description = "The new empty Dataset created will be assigned on a random generated Id",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Dataset.class)),
                        description = "Dataset was created succesfully"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset quota has been exceeded"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response createEmptyDataset(
            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(name = "title", schema = @Schema(implementation = String.class)) @FormParam("title") String title,
            @Parameter(name = "description", schema = @Schema(implementation = String.class)) @FormParam("description") String description) throws URISyntaxException, QuotaExceededException, JaqpotDocumentSizeExceededException {

        Dataset emptyDataset = DatasetFactory.createEmpty(0);
        ROG randomStringGenerator = new ROG(true);
        emptyDataset.setId(randomStringGenerator.nextString(14));
        emptyDataset.setFeatured(Boolean.FALSE);
        emptyDataset.setMeta(MetaInfoBuilder.builder()
                .addTitles(title)
                .addDescriptions(description)
                .build()
        );

        emptyDataset.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
        emptyDataset.setVisible(Boolean.TRUE);
        datasetLegacyWrapper.create(emptyDataset);
        //datasetHandler.create(emptyDataset);

        return Response.created(new URI(emptyDataset.getId())).entity(emptyDataset).build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/merge")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Operation(summary = "Merges Datasets",
            description = "The new intersected Dataset created will be assigned on a random generated Id",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Dataset.class)),
                        description = "Dataset was created succesfully"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset quota has been exceeded"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response mergeDatasets(
            @Parameter(name = "dataset_uris", description = "dataset_uris", schema = @Schema(implementation = String.class)) @FormParam("dataset_uris") String datasetURIs,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws URISyntaxException, QuotaExceededException, JaqpotDocumentSizeExceededException {

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
//        User user = userHandler.find(securityContext.getUserPrincipal().getName(), apiKey);

        String[] datasets = datasetURIs.split(",");
        Dataset dataset = null;
        for (String datasetURI : datasets) {
            Dataset d = client.target(datasetURI)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .get(Dataset.class);
        }
        ROG randomStringGenerator = new ROG(true);
        dataset.setId(randomStringGenerator.nextString(14));
        dataset.setFeatured(Boolean.FALSE);
        dataset.setVisible(true);
        if (dataset.getMeta() == null) {
            dataset.setMeta(new MetaInfo());
        }
        dataset.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));

        datasetLegacyWrapper.create(dataset);

        return Response.created(new URI(dataset.getId())).entity(dataset).build();

    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/merge/features")
//    @Parameters({
//        @Parameter(name = "dataset_uris", schema = @Schema(implementation = String.class)),
//        @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class), in = ParameterIn.HEADER)
//    })
    @Operation(summary = "Merges the features of two or more Datasets",
            description = "The new intersected Dataset created will be assigned on a random generated Id",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Dataset.class)),
                        description = "Dataset was created succesfully"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset quota has been exceeded"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response mergeFeaturesDatasets(
            @Parameter(name = "dataset_uris", schema = @Schema(implementation = String.class)) @FormParam("dataset_uris") String datasetURIs,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws URISyntaxException, QuotaExceededException, JaqpotDocumentSizeExceededException {

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
//        User user = userHandler.find(securityContext.getUserPrincipal().getName(), apiKey);

        String[] datasets = datasetURIs.split(",");
        Dataset dataset = null;
        for (String datasetURI : datasets) {
            Dataset d = client.target(datasetURI)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .get(Dataset.class);
            //dataset = DatasetFactory.mergeColumns(dataset, d);
        }
        ROG randomStringGenerator = new ROG(true);
        dataset.setId(randomStringGenerator.nextString(14));
        dataset.setFeatured(Boolean.FALSE);
        dataset.setVisible(true);
        if (dataset.getMeta() == null) {
            dataset.setMeta(new MetaInfo());
        }
        dataset.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));

        datasetLegacyWrapper.create(dataset);
        //datasetHandler.create(dataset);

        return Response.created(new URI(dataset.getId())).entity(dataset).build();

    }

    @DELETE
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("{id}")
    @Operation(summary = "Deletes dataset",
            responses = {
                @ApiResponse(responseCode = "200", description = "Dataset was succesfully deleted"),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset was not found in the system"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset quota has been exceeded"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response deleteDataset(
            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id) throws JaqpotForbiddenException {
        Dataset ds = datasetHandler.find(id);
        if (ds == null) {
            throw new NotFoundException("Dataset with id:" + id + " was not found on the server.");
        }
        MetaInfo metaInfo = ds.getMeta();
        if (metaInfo.getLocked()) {
            throw new JaqpotForbiddenException("You cannot delete a Dataset that is locked.");
        }

        String userName = securityContext.getUserPrincipal().getName();
        if (!ds.getMeta().getCreators().contains(userName)) {
            return Response.status(Response.Status.FORBIDDEN).entity("You cannot delete a Dataset that was not created by you.").build();
        }
        datasetHandler.remove(ds);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

//    @POST
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("{id}/qprf")
//    @ApiOperation("Creates QPRF Report")
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Dataset was succesfully deleted")
//        ,
//            @ApiResponse(code = 400, response = ErrorReport.class, message = "Bad Request. More details can be found in details of ErrorReport")
//        ,
//            @ApiResponse(code = 404, response = ErrorReport.class, message = "Dataset was not found in the system")
//        ,
//            @ApiResponse(code = 403, response = ErrorReport.class, message = "Dataset quota has been exceeded")
//        ,
//            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource")
//        ,
//            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)")
//        ,
//            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
//    })
//
//    public Response createQPRFReport(
//            @ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
//            @PathParam("id") String id,
//            @FormParam("substance_uri") String substanceURI,
//            @FormParam("title") String title,
//            @FormParam("description") String description
//    ) throws QuotaExceededException, ExecutionException, InterruptedException, JaqpotDocumentSizeExceededException {
//
//        String[] apiA = api_key.split("\\s+");
//        String apiKey = apiA[1];
//        User user = userHandler.find(securityContext.getUserPrincipal().getName());
//        long reportCount = reportHandler.countAllOfCreator(user.getId());
//        int maxAllowedReports = new UserFacade(user).getMaxReports();
//
//        if (reportCount > maxAllowedReports) {
//            LOG.info(String.format("User %s has %d algorithms while maximum is %d",
//                    user.getId(), reportCount, maxAllowedReports));
//            throw new QuotaExceededException("Dear " + user.getId()
//                    + ", your quota has been exceeded; you already have " + reportCount + " reports. "
//                    + "No more than " + maxAllowedReports + " are allowed with your subscription.");
//        }
//
//        Dataset ds = datasetLegacyWrapper.find(id);
//        //Dataset ds = datasetHandler.find(id);
//        if (ds == null) {
//            throw new NotFoundException("Dataset with id:" + id + " was not found on the server.");
//        }
//        if (ds.getByModel() == null || ds.getByModel().isEmpty()) {
//            throw new BadRequestException("Selected dataset was not produced by a valid model.");
//        }
//        Model model = modelHandler.find(ds.getByModel());
//        if (model == null) {
//            throw new BadRequestException("Selected dataset was not produced by a valid model.");
//        }
//        String datasetURI = model.getDatasetUri();
//        if (datasetURI == null || datasetURI.isEmpty()) {
//            throw new BadRequestException("The model that created this dataset does not point to a valid training dataset.");
//        }
//        Dataset trainingDS = client.target(datasetURI)
//                .queryParam("dataEntries", true)
//                .request()
//                .accept(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + apiKey)
//                .get(Dataset.class);
//        if (trainingDS == null) {
//            throw new BadRequestException("The model that created this dataset does not point to a valid training dataset.");
//        }
//
//        if (model.getTransformationModels() != null) {
//            for (String transModelURI : model.getTransformationModels()) {
//                Model transModel = modelHandler.find(transModelURI.split("model/")[1]);
//                if (transModel == null) {
//                    throw new NotFoundException("Transformation model with id:" + transModelURI + " was not found.");
//                }
//                try {
//                    trainingDS = jpdiClient.predict(trainingDS, transModel, trainingDS.getMeta(), UUID.randomUUID().toString()).get();
//                } catch (InterruptedException ex) {
//                    LOG.log(Level.SEVERE, "JPDI Training procedure interupted", ex);
//                    throw new InternalServerErrorException("JPDI Training procedure interupted", ex);
//                } catch (ExecutionException ex) {
//                    LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
//                    throw new InternalServerErrorException("JPDI Training procedure error", ex.getCause());
//                } catch (CancellationException ex) {
//                    throw new InternalServerErrorException("Procedure was cancelled");
//                }
//            }
//        }
//
//        List<String> retainableFeatures = new ArrayList<>(model.getIndependentFeatures());
//        retainableFeatures.addAll(model.getDependentFeatures());
//
//        trainingDS.getDataEntry().parallelStream()
//                .forEach(dataEntry -> {
//                    dataEntry.getValues().keySet().retainAll(retainableFeatures);
//                });
//
//        DataEntry dataEntry = ds.getDataEntry().stream()
//                .filter(de -> de.getEntryId().getURI().equals(substanceURI))
//                .findFirst()
//                .orElseThrow(() -> new BadRequestException(""));
//
//        trainingDS.getDataEntry().add(dataEntry);
//        trainingDS.getMeta().setCreators(new HashSet<>(Arrays.asList(user.getId())));
//
//        Map<String, Object> parameters = new HashMap<>();
//
//        UrlValidator urlValidator = new UrlValidator();
//        if (urlValidator.isValid(substanceURI)) {
//
//            String substanceId = substanceURI.split("substance/")[1];
//
//            Dataset structures = ambitClient.getDatasetStructures(substanceId, apiKey);
//
//            List<Map<String, String>> structuresList = structures.getDataEntry()
//                    .stream()
//                    .map(de -> {
//                        String compound = de.getEntryId().getURI();
//                        String casrn = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23CASRNDefault")).orElse("").toString();
//                        String einecs = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23EINECSDefault")).orElse("").toString();
//                        String iuclid5 = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23IUCLID5_UUIDDefault")).orElse("").toString();
//                        String inchi = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23InChI_stdDefault")).orElse("").toString();
//                        String reach = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23REACHRegistrationDateDefault")).orElse("").toString();
//                        String iupac = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23IUPACNameDefault")).orElse("").toString();
//
//                        Map<String, String> structuresMap = new HashMap<>();
//                        structuresMap.put("Compound", compound);
//                        structuresMap.put("CasRN", casrn);
//                        structuresMap.put("EC number", einecs);
//                        structuresMap.put("REACH registration date", reach);
//                        structuresMap.put("IUCLID 5 Reference substance UUID", iuclid5);
//                        structuresMap.put("Std. InChI", inchi);
//                        structuresMap.put("IUPAC name", iupac);
//
//                        return structuresMap;
//                    })
//                    .collect(Collectors.toList());
//            if (structuresList.isEmpty()) {
//                Map<String, String> structuresMap = new HashMap<>();
//                structuresMap.put("Compound", "");
//                structuresMap.put("CasRN", "");
//                structuresMap.put("EC number", "");
//                structuresMap.put("REACH registration date", "");
//                structuresMap.put("IUCLID 5 Reference substance UUID", "");
//                structuresMap.put("Std. InChI", "");
//                structuresMap.put("IUPAC name", "");
//                structuresList.add(structuresMap);
//                parameters.put("structures", structuresList);
//            }
//            parameters.put("structures", structuresList);
//        } else {
//            List<Map<String, String>> structuresList = new ArrayList<>();
//            Map<String, String> structuresMap = new HashMap<>();
//            structuresMap.put("Compound", "");
//            structuresMap.put("CasRN", "");
//            structuresMap.put("EC number", "");
//            structuresMap.put("REACH registration date", "");
//            structuresMap.put("IUCLID 5 Reference substance UUID", "");
//            structuresMap.put("Std. InChI", "");
//            structuresMap.put("IUPAC name", "");
//            structuresList.add(structuresMap);
//            parameters.put("structures", structuresList);
//        }
//
//        parameters.put("predictedFeature",
//                model
//                        .getPredictedFeatures()
//                        .stream()
//                        .findFirst()
//                        .orElseThrow(() -> new BadRequestException("Model does not have a valid predicted feature")));
//
//        parameters.put("algorithm", algorithmHandler.find(model.getAlgorithm().getId()));
//        parameters.put("substanceURI", substanceURI);
//        if (model.getLinkedModels() != null && !model.getLinkedModels().isEmpty()) {
//            Model doa = modelHandler.find(model.getLinkedModels().get(0).split("model/")[1]);
//            if (doa != null) {
//                parameters.put("doaURI", doa.getPredictedFeatures().get(0));
//                parameters.put("doaMethod", doa.getAlgorithm().getId());
//            }
//        }
//        TrainingRequest request = new TrainingRequest();
//
//        request.setDataset(trainingDS);
//        request.setParameters(parameters);
//        request.setPredictionFeature(model.getDependentFeatures()
//                .stream()
//                .findFirst()
//                .orElseThrow(() -> new BadRequestException("Model does not have a valid prediction feature")));
//        String qprfHost = properyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_QPRF);
//        LOG.log(Level.INFO, qprfHost);
//        Report report = client.target(qprfHost)
//                .request()
//                .header("Content-Type", MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .post(Entity.json(request), Report.class);
//
//        report.setMeta(MetaInfoBuilder.builder()
//                .addTitles(title)
//                .addDescriptions(description)
//                .addCreators(securityContext.getUserPrincipal().getName())
//                .build()
//        );
//        report.setId(new ROG(true).nextString(15));
//        report.setVisible(Boolean.TRUE);
//        reportHandler.create(report);
//
//        return Response.ok(report).build();
//    }
    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Path("{id}/qprf-dummy")
    @Operation(summary = "Creates QPRF Dummy Report",
            responses = {
                @ApiResponse(responseCode = "200", description = "Dataset was succesfully deleted"),
                @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Bad Request. More details can be found in details of ErrorReport"),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset was not found in the system"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset quota has been exceeded"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response createQPRFReportDummy(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "substance_uri", schema = @Schema(implementation = String.class)) @FormParam("substance_uri") String substanceURI,
            @Parameter(name = "title", schema = @Schema(implementation = String.class)) @FormParam("title") String title,
            @Parameter(name = "description", schema = @Schema(implementation = String.class)) @FormParam("description") String description
    ) {

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Dataset ds = datasetLegacyWrapper.find(id);
        //Dataset ds = datasetHandler.find(id);
        if (ds == null) {
            throw new NotFoundException("Dataset with id:" + id + " was not found on the server.");
        }
        if (ds.getByModel() == null || ds.getByModel().isEmpty()) {
            throw new BadRequestException("Selected dataset was not produced by a valid model.");
        }
        Model model = modelHandler.find(ds.getByModel());
        if (model == null) {
            throw new BadRequestException("Selected dataset was not produced by a valid model.");
        }
        String datasetURI = model.getDatasetUri();
        if (datasetURI == null || datasetURI.isEmpty()) {
            throw new BadRequestException("The model that created this dataset does not point to a valid training dataset.");
        }
        Dataset trainingDS = client.target(datasetURI)
                .queryParam("dataEntries", true)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .get(Dataset.class);
        if (trainingDS == null) {
            throw new BadRequestException("The model that created this dataset does not point to a valid training dataset.");
        }

        if (model.getTransformationModels() != null) {
            for (String transModelURI : model.getTransformationModels()) {
                Model transModel = modelHandler.find(transModelURI.split("model/")[1]);
                if (transModel == null) {
                    throw new NotFoundException("Transformation model with id:" + transModelURI + " was not found.");
                }
                try {
                    trainingDS = jpdiClient.predict(trainingDS, transModel, trainingDS.getMeta(), UUID.randomUUID().toString(), null).get();
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, "JPDI Training procedure interupted", ex);
                    throw new InternalServerErrorException("JPDI Training procedure interupted", ex);
                } catch (ExecutionException ex) {
                    LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
                    throw new InternalServerErrorException("JPDI Training procedure error", ex.getCause());
                } catch (CancellationException ex) {
                    throw new InternalServerErrorException("Procedure was cancelled");
                }
            }
        }

        List<String> retainableFeatures = new ArrayList<>(model.getIndependentFeatures());
        retainableFeatures.addAll(model.getDependentFeatures());

        trainingDS.getDataEntry().parallelStream()
                .forEach(dataEntry -> {
                    dataEntry.getValues().keySet().retainAll(retainableFeatures);
                });

        DataEntry dataEntry = ds.getDataEntry().stream()
                .filter(de -> de.getEntryId().getURI().equals(substanceURI))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(""));

        trainingDS.getDataEntry().add(dataEntry);

        Map<String, Object> parameters = new HashMap<>();

        UrlValidator urlValidator = new UrlValidator();
        if (urlValidator.isValid(substanceURI)) {
            Dataset structures = client.target(substanceURI + "/structures")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .get(Dataset.class);
            List<Map<String, String>> structuresList = structures.getDataEntry()
                    .stream()
                    .map(de -> {
                        String compound = de.getEntryId().getURI();
                        String casrn = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23CASRNDefault")).orElse("").toString();
                        String einecs = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23EINECSDefault")).orElse("").toString();
                        String iuclid5 = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23IUCLID5_UUIDDefault")).orElse("").toString();
                        String inchi = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23InChI_stdDefault")).orElse("").toString();
                        String reach = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23REACHRegistrationDateDefault")).orElse("").toString();
                        String iupac = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23IUPACNameDefault")).orElse("").toString();

                        Map<String, String> structuresMap = new HashMap<>();
                        structuresMap.put("Compound", compound);
                        structuresMap.put("CasRN", casrn);
                        structuresMap.put("EC number", einecs);
                        structuresMap.put("REACH registration date", reach);
                        structuresMap.put("IUCLID 5 Reference substance UUID", iuclid5);
                        structuresMap.put("Std. InChI", inchi);
                        structuresMap.put("IUPAC name", iupac);

                        return structuresMap;
                    })
                    .collect(Collectors.toList());
            parameters.put("structures", structuresList);
        } else {
            List<Map<String, String>> structuresList = new ArrayList<>();
            Map<String, String> structuresMap = new HashMap<>();
            structuresMap.put("Compound", "");
            structuresMap.put("CasRN", "");
            structuresMap.put("EC number", "");
            structuresMap.put("REACH registration date", "");
            structuresMap.put("IUCLID 5 Reference substance UUID", "");
            structuresMap.put("Std. InChI", "");
            structuresMap.put("IUPAC name", "");
            structuresList.add(structuresMap);
            parameters.put("structures", structuresList);
        }
        parameters.put("predictedFeature",
                model
                .getPredictedFeatures()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Model does not have a valid predicted feature")));
        parameters.put("algorithm", algorithmHandler.find(model.getAlgorithm().getId()));
        parameters.put("substanceURI", substanceURI);
        if (model.getLinkedModels() != null && !model.getLinkedModels().isEmpty()) {
            Model doa = modelHandler.find(model.getLinkedModels().get(0).split("model/")[1]);
            if (doa != null) {
                parameters.put("doaURI", doa.getPredictedFeatures().get(0));
                parameters.put("doaMethod", doa.getAlgorithm().getId());
            }
        }
        TrainingRequest request = new TrainingRequest();
        request.setDataset(trainingDS);
        request.setParameters(parameters);
        request.setPredictionFeature(model.getDependentFeatures()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Model does not have a valid prediction feature")));

        return Response.ok(request).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({"application/json", MediaType.APPLICATION_JSON})
    @Path("{did}/dataentry/{id}")
    @Operation(summary = "Finds Data Entry by Id",
            description = "Finds specified Data Entry",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = DataEntry.class)), description = "Dataset Entry was found"),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset Entry was not found in the system"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response getDataEntry(
            //@ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @PathParam("did") String datasetId,
            @PathParam("id") String id)
            throws NotFoundException, IllegalArgumentException {
        Dataset dataset = datasetHandler.find(datasetId);
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + datasetId);
        }

        DataEntry dataEntry = dataEntryHandler.find(id);
        if (dataEntry == null) {
            throw new NotFoundException("Could not find DataEntry with id:" + id);
        }
        if (!dataEntry.getDatasetId().equals(dataset.getId())) {
            throw new IllegalArgumentException("Data Entry " + id + " is not part of Dataset with id :" + datasetId);
        }
        return Response.ok(dataEntry).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({"application/json", MediaType.APPLICATION_JSON})
    @Path("{id}/dataentry")
    @Operation(summary = "Finds Data Entries of Dataset with given id",
            description = "Finds Data entries of specified Dataset",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))), description = "Dataset Entry was found"),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Dataset Entry was not found in the system"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response getDataEntries(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "rowStart", description = "rowStart", schema = @Schema(implementation = Integer.class, defaultValue = "0"), required = false) @QueryParam("rowStart") Integer rowStart,
            @Parameter(name = "rowMax", description = "rowMax", schema = @Schema(implementation = Integer.class), required = false) @QueryParam("rowMax") Integer rowMax,
            @Parameter(name = "colStart", description = "colStart", schema = @Schema(implementation = Integer.class)) @QueryParam("colStart") Integer colStart,
            @Parameter(name = "colMax", description = "colMax", schema = @Schema(implementation = Integer.class)) @QueryParam("colMax") Integer colMax,
            @Parameter(name = "stratify", description = "stratify", schema = @Schema(implementation = Long.class)) @QueryParam("stratify") String stratify,
            @Parameter(name = "seed", description = "seed", schema = @Schema(implementation = Long.class)) @QueryParam("seed") Long seed,
            @Parameter(name = "folds", description = "folds", schema = @Schema(implementation = Integer.class)) @QueryParam("folds") Integer folds,
            @Parameter(name = "target_feature", description = "target_feature", schema = @Schema(implementation = Integer.class)) @QueryParam("target_feature") String targetFeature
    ) throws NotFoundException {
        Dataset dataset = datasetHandler.find(id);
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        List<DataEntry> dataEntries = dataEntryHandler.findDataEntriesByDatasetId(id, rowStart, rowMax, colStart, colMax);
        if (dataEntries == null || dataEntries.isEmpty()) {
            throw new NotFoundException("Could not find DataEntries associated with dataset with id:" + id);
        }
        return Response.ok(dataEntries).build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"application/json", MediaType.APPLICATION_JSON})
    @Path("{id}/dataentry")
    @Operation(summary = "Creates DataEntry",
            description = "The new Data Entry created will be assigned on a random generated Id",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = DataEntry.class)), description = "DataEntry was created succesfully"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response createDataEntry(
            //@ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id,
            DataEntry dataentry) throws URISyntaxException, JaqpotDocumentSizeExceededException {
        Dataset dataset = datasetHandler.find(id);
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        dataentry.setDatasetId(id);
        ROG randomStringGenerator = new ROG(true);
        dataentry.setId(randomStringGenerator.nextString(14));
        dataEntryHandler.create(dataentry);
        datasetHandler.updateField(id, "totalRows", dataset.getTotalRows() + 1);
        return Response.created(new URI(dataentry.getId())).entity(dataentry).build();

    }

    @PUT
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"application/json", MediaType.APPLICATION_JSON})
    @Path("{id}/meta")
    @Operation(summary = "Updates meta info of a dataset",
            description = "TUpdates meta info of a dataset",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = DataEntry.class)), description = "Meta was updated succesfully"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response updateMeta(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            Dataset datasetForUpdate) throws URISyntaxException, JaqpotDocumentSizeExceededException, JaqpotNotAuthorizedException {
        String userId = securityContext.getUserPrincipal().getName();
        Dataset dataset = datasetHandler.find(id);
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        Boolean canUpdate = rights.canWrite(datasetForUpdate.getMeta(), userHandler.find(userId, apiKey));
        if (canUpdate == true) {
            datasetHandler.updateMeta(id, datasetForUpdate.getMeta());
        } else {
            throw new JaqpotNotAuthorizedException("You are not authorized to update this resource");
        }

        return Response.accepted().entity(datasetForUpdate.getMeta()).build();

    }

    @PUT
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"application/json", MediaType.APPLICATION_JSON})
    @Path("{id}/ontrash")
    @Operation(summary = "Updates meta info of a dataset",
            description = "TUpdates meta info of a dataset",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = DataEntry.class)), description = "Meta was updated succesfully"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response updateOnTrash(
            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id,
            Dataset datasetForUpdate) throws URISyntaxException, JaqpotDocumentSizeExceededException, JaqpotNotAuthorizedException {
        String userId = securityContext.getUserPrincipal().getName();
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Dataset dataset = datasetHandler.find(id);
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        Boolean canTrash = rights.canTrash(dataset.getMeta(), userHandler.find(userId, apiKey));
        if (canTrash == true) {
            datasetHandler.updateField(id, "onTrash", datasetForUpdate.getOnTrash());
        } else {
            throw new JaqpotNotAuthorizedException("You are not authorized to update this resource");
        }

        return Response.accepted().entity(datasetForUpdate.getMeta()).build();

    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/csv")
    @Consumes( MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Creates dataset By .csv document",
            description = "Creates features/substances, returns Dataset",
            responses = {
                @ApiResponse(content = @Content(schema = @Schema(implementation = Dataset.class)))
            }
    )
    public Response createDummyDataset(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(type = "string")) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "file", description = "xml[m,x] file", required = true, schema = @Schema(type = "string", format = "binary")) @FormParam("file") String file,
            @Parameter(name = "title", description = "Title of dataset", required = true, schema = @Schema(type = "string")) @FormParam("title") String title,
            @Parameter(name = "description", description = "Description of model", required = true, schema = @Schema(type = "string")) @FormParam("description") String description,
            @Parameter(description = "multipartFormData input", hidden = true) MultipartFormDataInput input) 
            throws ParameterIsNullException, ParameterInvalidURIException, QuotaExceededException, IOException, ParameterScopeException, ParameterRangeException, ParameterTypeException, URISyntaxException, JaqpotDocumentSizeExceededException {

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Dataset dataset = new Dataset();
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        dataset.setFeatured(Boolean.FALSE);

        dataset.setMeta(MetaInfoBuilder.builder()
                .addTitles(uploadForm.get("title").get(0).getBodyAsString())
                .addDescriptions(uploadForm.get("description").get(0).getBodyAsString())
                .build()
        );

        List<InputPart> inputParts = uploadForm.get("file");
        for (InputPart inputPart : inputParts) {

            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                calculateRowsAndColumns(dataset, inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        populateFeatures(dataset);

        ROG randomStringGenerator = new ROG(true);
        dataset.setId(randomStringGenerator.nextString(14));
        dataset.setFeatured(Boolean.FALSE);
        if (dataset.getMeta() == null) {
            dataset.setMeta(new MetaInfo());
        }
        dataset.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
        dataset.setVisible(Boolean.TRUE);
        datasetLegacyWrapper.create(dataset);

        return Response.created(new URI(dataset.getId())).entity(dataset).build();

    }

    private void calculateRowsAndColumns(Dataset dataset, InputStream stream) {
        Scanner scanner = new Scanner(stream);

        Set<FeatureInfo> featureInfoList = new HashSet<>();
        List<DataEntry> dataEntryList = new ArrayList<>();
        List<String> feature = new LinkedList<>();
        boolean firstLine = true;
        int count = 0;
        while (scanner.hasNext()) {

            List<String> line = parseLine(scanner.nextLine());
            if (firstLine) {
                for (String l : line) {
                    String pseudoURL = "/feature/" + l.trim().replaceAll("[ .]", "_"); //uriInfo.getBaseUri().toString()+
                    feature.add(pseudoURL);
                    featureInfoList.add(new FeatureInfo(pseudoURL, l, "NA", new HashMap<>(), Dataset.DescriptorCategory.EXPERIMENTAL));
                }
                firstLine = false;
            } else {
                Iterator<String> it1 = feature.iterator();
                Iterator<String> it2 = line.iterator();
                TreeMap<String, Object> values = new TreeMap<>();
                while (it1.hasNext() && it2.hasNext()) {
                    String it = it2.next();
                    if (!NumberUtils.isParsable(it)) {
                        values.put(it1.next(), it);
                    } else {
                        values.put(it1.next(), Float.parseFloat(it));
                    }
                }

                DataEntry dataEntry = new DataEntry();
                dataEntry.setValues(values);
                EntryId entryId = new EntryId();
                entryId.setName("row" + count);
                entryId.setURI(propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE) + "substance/" + new ROG(true).nextString(12));
                entryId.setOwnerUUID("7da545dd-2544-43b0-b834-9ec02553f7f2");

                dataEntry.setEntryId(entryId);
                dataEntryList.add(dataEntry);
            }
            count++;
        }
        scanner.close();
        dataset.setFeatures(featureInfoList);
        dataset.setDataEntry(dataEntryList);
    }

    private void populateFeatures(Dataset dataset) throws JaqpotDocumentSizeExceededException {
        int key = 0;
        for (FeatureInfo featureInfo : dataset.getFeatures()) {
            
            String trimmedFeatureURI = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE) + "feature/" + featureInfo.getName().replaceAll("\\s+", " ").replaceAll("[ .]", "_") + "_" + new ROG(true).nextString(12);

            String trimmedFeatureName = featureInfo.getName().replaceAll("\\s+", " ").replaceAll("[.]", "_").replaceAll("[.]", "_");

            Feature f = FeatureBuilder.builder(trimmedFeatureURI.split("feature/")[1])
                    .addTitles(featureInfo.getName()).build();
            featureHandler.create(f);

            //Update FeatureURIS in Data Entries
            for (DataEntry dataentry : dataset.getDataEntry()) {
                Object value = dataentry.getValues().remove(featureInfo.getURI());
                dataentry.getValues().put(String.valueOf(key), value);
            }
            //Update FeatureURI in Feature Info
            featureInfo.setURI(trimmedFeatureURI);
            featureInfo.setKey(String.valueOf(key));
            featureInfo.setName(trimmedFeatureName);
            key += 1;
        }
    }
}
