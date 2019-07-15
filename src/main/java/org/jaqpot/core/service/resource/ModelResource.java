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
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URISyntaxException;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.validator.routines.UrlValidator;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.wrappers.DatasetLegacyWrapper;
import org.jaqpot.core.messagebeans.DeleteIndexedEntityProducer;
import org.jaqpot.core.messagebeans.IndexEntityProducer;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.dto.jpdi.TrainingResponse;
import org.jaqpot.core.model.dto.models.ModelId;
import org.jaqpot.core.model.dto.models.PretrainedModel;
import org.jaqpot.core.model.dto.models.QuickPredictionNeeds;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.factory.FeatureFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.data.PredictionService;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.JaqpotForbiddenException;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;
import org.jaqpot.core.service.exceptions.parameter.ParameterInvalidURIException;
import org.jaqpot.core.service.exceptions.parameter.ParameterIsNullException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;
import org.jaqpot.core.service.httphandlers.Rights;
import org.jaqpot.core.service.validator.ParameterValidator;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("/model")
//@Api(value = "/model", description = "Models API")
@Produces({"application/json", "text/uri-list"})
@Tag(name = "model")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class ModelResource {

    private static final Logger LOG = Logger.getLogger(ModelResource.class.getName());

    private static final String DEFAULT_DATASET = "http://app.jaqpot.org:8080/jaqpot/services/dataset/corona";

    private final ROG randomStringGenerator = new ROG(true);

    @Context
    UriInfo uriInfo;

    @EJB
    ModelHandler modelHandler;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    DatasetLegacyWrapper datasetLegacyWrapper;

    @EJB
    UserHandler userHandler;

    @EJB
    PredictionService predictionService;

    @EJB
    FeatureHandler featureHandler;

    @EJB
    AlgorithmHandler algoHandler;

    @EJB
    PropertyManager propertyManager;

    @EJB
    Rights rights;

    @Context
    SecurityContext securityContext;

    @Inject
    @UnSecure
    Client client;

    @Inject
    ParameterValidator parameterValidator;

    @EJB
    IndexEntityProducer iep;

    @EJB
    DeleteIndexedEntityProducer diep;

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(summary = "Finds all Models",
            description = "Finds all Models from Jaqpot Dataset. The response will list all models and will return either a URI list "
            + "of a list of JSON model objects. In the latter case, only the IDs, metadata, ontological classes "
            + "and reliability of the models will be returned. "
            + "Use the parameters start and max to get paginated results.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Model.class))),
                        description = "Models found and are listed in the response body"),
                @ApiResponse(responseCode = "204", description = "No content: The request succeeded, but there are no models matching your search criteria."),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            },
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:JapotModelList"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:ModelList")
                })
            })
    public Response listModels(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "start", description = "start", schema = @Schema(implementation = Integer.class, defaultValue = "0")) @QueryParam("start") Integer start,
                    @Parameter(name = "max", description = "max - the server imposes an upper limit of 500 on this "
                + "parameter.", schema = @Schema(implementation = Integer.class, defaultValue = "10")) @QueryParam("max") Integer max,
            @Parameter(name = "ontrash", description = "on trash datasets", required = false, schema = @Schema(implementation = Boolean.class, allowableValues = {"true", "false"})) @QueryParam("ontrash") Boolean ontrash,
            @Parameter(name = "organization", description = "organization", schema = @Schema(implementation = String.class)) @QueryParam("organization") String organization,
            @Parameter(name = "byAlgorithm", description = "byAlgorithm", schema = @Schema(implementation = String.class)) @QueryParam("byAlgorithm") String byAlgorithm
    ) {
        if (max == null || max > 500) {
            max = 500;
        }

        String creator = securityContext.getUserPrincipal().getName();

        List<Model> modelsFound = new ArrayList();
        Long total = null;
        if (organization == null && ontrash == null && byAlgorithm == null) {
            modelsFound.addAll(modelHandler.listMetaOfCreator(creator, start != null ? start : 0, max));
            total = modelHandler.countAllOfCreator(creator);
        } else if (ontrash != null) {
            List<String> fields = new ArrayList<>();
            fields.add("_id");
            fields.add("meta");
            fields.add("predictedFeatures");
            fields.add("independentFeatures");
            Map<String, Object> properties = new HashMap<>();
            properties.put("onTrash", ontrash);
            properties.put("meta.creators", Arrays.asList(creator));
            modelsFound.addAll(modelHandler.find(properties, fields, start, max));
            total = modelHandler.countCreatorsInTrash(creator);
        } else if( byAlgorithm != null){
            List<String> fields = new ArrayList<>();
            fields.add("_id");
            fields.add("meta");
            fields.add("predictedFeatures");
            fields.add("independentFeatures");
            fields.add("algorithm");
            fields.add("parameters");
            Map<String, Object> properties = new HashMap<>();
            properties.put("meta.creators", Arrays.asList(creator));
            properties.put("visible", true);
            properties.put("algorithm._id", byAlgorithm);
            Map<String, Object> neProperties = new HashMap<>();
            neProperties.put("onTrash", true);
            modelsFound.addAll(modelHandler.findAllAndNe(properties, neProperties, fields, start, max));
            total = modelHandler.countAllOfAlgos(creator, byAlgorithm);
        }
        
        else {
            List<String> fields = new ArrayList<>();
            fields.add("_id");
            fields.add("meta");
            fields.add("predictedFeatures");
            fields.add("independentFeatures");
            Map<String, Object> properties = new HashMap<>();
            properties.put("meta.read", organization);
            properties.put("visible", true);
//            properties.put("meta.creators", Arrays.asList(creator));
            Map<String, Object> neProperties = new HashMap<>();
            neProperties.put("onTrash", true);
            neProperties.put("algorithm._id", "httk");
            modelsFound.addAll(modelHandler.findAllAndNe(properties, neProperties, fields, start, max));
            total = modelHandler.countAllOfOrg(creator, organization);
        }
        return Response.ok(modelsFound)
                .header("total", total)
                .build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/featured")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(summary = "Finds all Models",
            description = "Finds featured Models from Jaqpot database. The response will list all models and will return either a URI list "
            + "of a list of JSON model objects. In the latter case, only the IDs, metadata, ontological classes "
            + "and reliability of the models will be returned. "
            + "Use the parameters start and max to get paginated results.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Model.class))),
                        description = "Models found and are listed in the response body"),
                @ApiResponse(responseCode = "204", description = "No content: The request succeeded, but there are no models matching your search criteria."),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            },
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:JapotModelList"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:ModelList")
                })
            })
    public Response listFeaturedModels(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "start", description = "start", schema = @Schema(implementation = Integer.class, defaultValue = "0")) @QueryParam("start") Integer start,
            @Parameter(name = "max", description = "max - the server imposes an upper limit of 500 on this "
                + "parameter.", schema = @Schema(implementation = Integer.class, defaultValue = "10")) @QueryParam("max") Integer max
    ) {
        if (max == null || max > 500) {
            max = 500;
        }
        return Response.ok(modelHandler.findFeatured(start != null ? start : 0, max))
                .header("total", modelHandler.countFeatured())
                .build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list", "application/ld+json"})
    @Operation(summary = "Finds Model by Id",
            description = "Finds specified Model",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Model.class))),
                        description = "Model is found"),
                @ApiResponse(responseCode = "401", description = "You are not authorized to access this model"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "404", description = "This model was not found."),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            },
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:Model"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AcessToken"),
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotModelId")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotModel")
                })
            })
    public Response getModel(
            @Parameter(name = "id", description = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access models", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Model model = modelHandler.findModel(id);
        if (model == null) {
            return Response
                    .ok(ErrorReportFactory.notFoundError(uriInfo.getPath()))
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }
        return Response.ok(model).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces(MediaType.APPLICATION_XML)
    @Path("/{id}/pmml")
    @Operation(summary = "Finds Model by Id",
            description = "Finds specified Model",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Model.class)),
                        description = "Model is found"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this model"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This model was not found."),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response getModelPmml(
            @Parameter(name = "id", description = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access models", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws NotFoundException {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Model model = modelHandler.findModelPmml(id);
        if (model == null || model.getPmmlModel() == null) {
            throw new NotFoundException("The requested model was not found on the server.");
        }

        Object pmmlObj = model.getPmmlModel();
        if (pmmlObj == null || pmmlObj.toString().isEmpty()) {
            throw new NotFoundException("This model does not have a PMML representation.");
        }
        if (pmmlObj instanceof List) {
            List pmmlObjList = (List) pmmlObj;
            Object pmml = pmmlObjList.stream().findFirst();
            if (pmml == null) {
                throw new NotFoundException("This model does not have a PMML representation.");
            }
            return Response
                    .ok(pmml.toString(), MediaType.APPLICATION_XML)
                    .build();
        } else {
            return Response
                    .ok(pmmlObj.toString(), MediaType.APPLICATION_XML)
                    .build();
        }
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}/independent")
    @Operation(summary = "Lists the independent features of a Model",
            description = "Lists the independent features of a Model. The result is available as a URI list.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))),
                        description = "Model is found and its independent features are listed in the response body."),
                @ApiResponse(responseCode = "401", description = "You are not authorized to access this model"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "404", description = "This model was not found."),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            })
    public Response listModelIndependentFeatures(
            @Parameter(name = "id", description = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            //@ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("Authorization") String api_key) {
            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access models", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Model foundModel = modelHandler.find(id);
        if (foundModel == null) {
            throw new NotFoundException("The requested model was not found on the server.");
        }

        List<Feature> independentFeatures = new ArrayList();
        List<String> features = foundModel.getIndependentFeatures();
        features.forEach(feat -> {
            String[] featSplited = feat.split("/");
            Feature feature = featureHandler.find(featSplited[featSplited.length - 1]);
            independentFeatures.add(feature);
        });

        return Response.ok(independentFeatures).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}/dependent")
    @Operation(summary = "Lists the dependent features of a Model",
            description = "Lists the dependent features of a Model identified by its ID. The result is available as a URI list.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))),
                        description = "Model is found and its independent features are listed in the response body."),
                @ApiResponse(responseCode = "401", description = "You are not authorized to access this model"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "404", description = "This model was not found."),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            })
    public Response listModelDependentFeatures(
            @Parameter(name = "id", description = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access models", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Model foundModel = modelHandler.find(id);
        if (foundModel == null) {
            throw new NotFoundException("The requested model was not found on the server.");
        }

        List<Feature> dependentFeatures = new ArrayList();
        List<String> features = foundModel.getDependentFeatures();
        features.forEach(feat -> {
            String[] featSplited = feat.split("/");
            Feature feature = featureHandler.find(featSplited[featSplited.length - 1]);
            dependentFeatures.add(feature);
        });

        return Response.ok(dependentFeatures).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}/predicted")
    @Operation(summary = "Lists the dependent features of a Model",
            description = "Lists the predicted features of a Model identified by its ID. The result is available as a URI list.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))),
                        description = "Model is found and its independent features are listed in the response body."),
                @ApiResponse(responseCode = "401", description = "You are not authorized to access this model"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "404", description = "This model was not found."),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            })
    public Response listModelPredictedFeatures(
             @Parameter(name = "id", description = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
             @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access models", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Model foundModel = modelHandler.findModel(id);
        if (foundModel == null) {
            throw new NotFoundException("The requested model was not found on the server.");
        }
        List<Feature> predictedFeatures = new ArrayList<>();
        foundModel.getPredictedFeatures().forEach(feat -> {
            String[] featSpl = feat.split("/");
            Feature predFeat = featureHandler.find(featSpl[featSpl.length - 1]);
            predictedFeatures.add(predFeat);
        });

        if (foundModel.getLinkedModels() != null) {
            foundModel.getLinkedModels().stream()
                    .map(m -> m.split("model/")[1])
                    .forEach(mid -> {
                        Model linkedModel = modelHandler.findModel(mid);
                        linkedModel.getPredictedFeatures().forEach((feat) -> {
                            String[] featSpl = feat.split("/");
                            Feature predFeat = featureHandler.find(featSpl[featSpl.length - 1]);
                            predictedFeatures.add(predFeat);
                        });

                    });
        }
        return Response.ok(predictedFeatures).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/required")
    @Operation(summary = "Lists the required features of a Model",
            description = "Lists the required features of a Model identified by its ID. The result is available as a URI list.",
            responses = {
                @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),})
    public Response listModelRequiredFeatures(
            @Parameter(name = "id", description = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "Authorization", description = "Authorization required", schema = @Schema(implementation = String.class) ) @HeaderParam("Authorization") String api_key) {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Model model = modelHandler.find(id);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<String> requiredFeatures;
        String datasetURI;

        if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
            Model firstTransformation = client.target(model.getTransformationModels().get(0))
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .get(Model.class);
            requiredFeatures = firstTransformation.getIndependentFeatures();
            datasetURI = firstTransformation.getDatasetUri();
        } else {
            requiredFeatures = model.getIndependentFeatures();
            datasetURI = model.getDatasetUri();
        }
        Set<FeatureInfo> featureSet;
        if (datasetURI != null) {
            featureSet = client.target(datasetURI.split("\\?")[0] + "/features")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .get(new GenericType<Set<FeatureInfo>>() {
                    });
        } else {
            featureSet = new HashSet<>();
        }

        Set<String> requiredFeatureSet = new HashSet<>(requiredFeatures);
        List<FeatureInfo> selectedFeatures = featureSet.stream()
                .filter(f -> requiredFeatureSet.contains(f.getURI()))
                .collect(Collectors.toList());
        return Response.status(Response.Status.OK).entity(selectedFeatures).build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}")
    @Operation(summary = "Creates Prediction",
            description = "Creates Prediction",
            responses = {
                @ApiResponse(content = @Content(schema = @Schema(implementation = Task.class))),},
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:JaqpotPredictionTaskId"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AcessToken"),
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotModelId"),
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Dataset")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotPredictionTaskId")
                })
            })
    @org.jaqpot.core.service.annotations.Task
    public Response makePrediction(
            @Parameter(name = "dataset_uri", description = "dataset_uri", required = true, schema = @Schema(implementation = String.class)) @FormParam("dataset_uri") String datasetURI,
            @Parameter(name = "visible", description = "visible", required = true, schema = @Schema(implementation = Boolean.class)) @FormParam("visible") Boolean visible,
            @Parameter(name = "id", description = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "Authorization", description = "Authorization required", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws GeneralSecurityException, QuotaExceededException, ParameterIsNullException, ParameterInvalidURIException, JaqpotDocumentSizeExceededException {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        if (datasetURI == null) {
            throw new ParameterIsNullException("datasetURI");
        }
        if (id == null) {
            throw new ParameterIsNullException("id");
        }

        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
        if (!urlValidator.isValid(datasetURI)) {
            throw new ParameterInvalidURIException("Not valid dataset URI.");
        }

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long datasetCount = datasetHandler.countCreatorsExistenseDatasets(user.getId(), Dataset.DatasetExistence.UPLOADED);
        int maxAllowedDatasets = new UserFacade(user).getMaxDatasets();

        if (datasetCount > maxAllowedDatasets) {
            LOG.info(String.format("User %s has %d datasets while maximum is %d",
                    user.getId(), datasetCount, maxAllowedDatasets));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + datasetCount + " datasets. "
                    + "No more than " + maxAllowedDatasets + " are allowed with your subscription.");
        }

        Model model = modelHandler.find(id);
        if (model == null) {
            throw new NotFoundException("Model not found.");
        }
        String datasetId = datasetURI.split("dataset/")[1];
        Dataset datasetMeta = datasetHandler.findMeta(datasetId);
        List<String> requiredFeatures = retrieveRequiredFeatures(model);

        parameterValidator.validateDataset(datasetMeta, requiredFeatures);

        Map<String, Object> options = new HashMap<>();
        options.put("dataset_uri", datasetURI);
        options.put("api_key", apiKey);
        options.put("modelId", id);
        options.put("creator", securityContext.getUserPrincipal().getName());
        options.put("base_uri", uriInfo.getBaseUri().toString());
        Task task = predictionService.initiatePrediction(options);
        return Response.ok(task).build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/")
    @Operation(summary = "Stores a pretrained model",
            description = "Stores a pretrained model",
            //            response = Task.class,
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:JaqpotPredictionTaskId"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AcessToken"),
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:PretrainedModel"),}),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:ModelId")
                })
            })

    @org.jaqpot.core.service.annotations.Task
    public Response storePretrained(
            PretrainedModel pretrainedModelRequest,
            @HeaderParam("Authorization") String api_key) throws GeneralSecurityException, QuotaExceededException, ParameterIsNullException, ParameterInvalidURIException, IllegalArgumentException, JaqpotDocumentSizeExceededException {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];

        User user = userHandler.find(securityContext.getUserPrincipal().getName());

        long modelCount = modelHandler.countAllOfCreator(user.getId());
        int maxAllowedModels = new UserFacade(user).getMaxModels();

        if (modelCount > maxAllowedModels) {
            LOG.info(String.format("User %s has %d models while maximum is %d",
                    user.getId(), modelCount, maxAllowedModels));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + modelCount + " models. "
                    + "No more than " + maxAllowedModels + " are allowed with your subscription.");
        }

//        TrainingResponse trainingResponse = serializer.parse(responseStream, TrainingResponse.class);
        Model model = new Model();

        model.setId(randomStringGenerator.nextStringId(20));
        model.setActualModel(pretrainedModelRequest.getRawModel());
        model.setPmmlModel(pretrainedModelRequest.getPmmlModel());

        model.setImplementedIn(pretrainedModelRequest.getRuntime().get(0));
        model.setImplementedWith(pretrainedModelRequest.getImplementedWith().get(0));

        String runtime = pretrainedModelRequest.getRuntime().get(0);
        String algoId = runtime + "-pretrained";
        Algorithm algo = algoHandler.find(algoId);
        model.setAlgorithm(algo);

        MetaInfo mf = new MetaInfo();
        Set<String> titles = new HashSet();
        Set<String> descriptions = new HashSet();
        Set<String> creators = new HashSet();
        creators.add(user.getId());
        if (pretrainedModelRequest.getTitle().get(0) != null) {
            titles.add(pretrainedModelRequest.getTitle().get(0).toString());
        }
        if (pretrainedModelRequest.getDescription().get(0) != null) {
            descriptions.add(pretrainedModelRequest.getDescription().get(0));
        }

        mf.setTitles(titles);
        mf.setDescriptions(descriptions);
        mf.setCreators(creators);
        model.setMeta(mf);
        model.setVisible(Boolean.TRUE);

        HashMap additionalInfo = new HashMap();

//        if (pretrainedModelRequest.getAdditionalInfo() != null) {
//            Map addFromUser = (Map) pretrainedModelRequest.getAdditionalInfo();
//            if (!addFromUser.isEmpty()) {
//                additionalInfo.putAll(addFromUser);
//            }
//        }
        HashMap independentFeaturesForAdd = new HashMap();

        List<String> pretrainedIndependentFeatures = new ArrayList();

        pretrainedModelRequest.getIndependentFeatures().forEach(indf -> {
            String linkedFeatID = randomStringGenerator.nextString(12);
            Feature pretrainedIndF = new Feature();
            pretrainedIndF.setId(linkedFeatID);
            pretrainedIndF.setVisible(Boolean.TRUE);
            MetaInfo featMetaInf = new MetaInfo();
            featMetaInf.setCreators(creators);
            Set<String> featDescr = new HashSet();
            featDescr.add("Feature created to link to independent feature of model " + model.getMeta().getTitles().stream().findFirst().get().toString());
            featMetaInf.setDescriptions(featDescr);
            Set<String> hasSources = new HashSet();
            hasSources.add("model/" + model.getId());
            featMetaInf.setHasSources(hasSources);
            Set<String> featTitles = new HashSet();
            featTitles.add(indf);
            featMetaInf.setTitles(featTitles);
            pretrainedIndF.setMeta(featMetaInf);
            try {
                featureHandler.create(pretrainedIndF);
            } catch (JaqpotDocumentSizeExceededException ex) {
                Logger.getLogger(ModelResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            String featURI = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE) + "feature/" + linkedFeatID;
            pretrainedIndependentFeatures.add(featURI);
            independentFeaturesForAdd.put(featURI, indf);
        });

        additionalInfo.put("fromUser", pretrainedModelRequest.getAdditionalInfo());
        additionalInfo.put("independentFeatures", independentFeaturesForAdd);
        model.setIndependentFeatures(pretrainedIndependentFeatures);

        List<String> pretrainedDependentFeatures = new ArrayList();

        pretrainedModelRequest.getDependentFeatures().forEach(depenf -> {
            String linkedFeatID = randomStringGenerator.nextString(12);
            Feature pretrainedDepF = new Feature();
            pretrainedDepF.setId(linkedFeatID);
            pretrainedDepF.setVisible(Boolean.TRUE);
            MetaInfo featMetaInf = new MetaInfo();
            featMetaInf.setCreators(creators);
            Set<String> featDescr = new HashSet();
            featDescr.add("Feature created to link to independent feature of model " + model.getMeta().getTitles().stream().findFirst().get().toString());
            featMetaInf.setDescriptions(featDescr);
            Set<String> hasSources = new HashSet();
            hasSources.add("model/" + model.getId());
            featMetaInf.setHasSources(hasSources);
            Set<String> featTitles = new HashSet();
            featTitles.add(depenf);
            featMetaInf.setTitles(featTitles);
            pretrainedDepF.setMeta(featMetaInf);
//            pretrainedDepF.setFromPretrained(Boolean.TRUE);
//            pretrainedDepF.setActualIndependentFeatureName(depenf);

            try {
                featureHandler.create(pretrainedDepF);
            } catch (JaqpotDocumentSizeExceededException ex) {
                Logger.getLogger(ModelResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            String depDeatURI = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE) + "feature/" + linkedFeatID;
            pretrainedDependentFeatures.add(depDeatURI);
//            additionalInfo.put(depDeatURI, depenf);
        });

        model.setDependentFeatures(pretrainedDependentFeatures);

        HashMap predictedFeaturesForAdd = new HashMap();
        List<String> pretrainedPredictedFeatures = new ArrayList<>();
        for (String featureTitle : pretrainedModelRequest.getPredictedFeatures()) {
            Feature predictionFeatureResource = featureHandler.find(featureTitle);
            if (predictionFeatureResource == null) {
                String predFeatID = randomStringGenerator.nextString(12);
                predictionFeatureResource = new Feature();
                predictionFeatureResource.setId(predFeatID);

                predictionFeatureResource.setMeta(MetaInfoBuilder
                        .builder()
                        .addSources(/*messageBody.get("base_uri") + */"model/" + model.getId())
                        .addComments("Feature created to hold predictions for model with Title " + model.getMeta().getTitles().toArray()[0])
                        .addTitles(featureTitle)
                        //                        .addSeeAlso(predictionFeature)
                        .addCreators(user.getId())
                        .build());
                /* Create feature */
//                predictionFeatureResource.setActualIndependentFeatureName(featureTitle);
//                predictionFeatureResource.setFromPretrained(Boolean.TRUE);
                featureHandler.create(predictionFeatureResource);
            }
            String predictFeat = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE) + "feature/" + predictionFeatureResource.getId();
            pretrainedPredictedFeatures.add(predictFeat);
            predictedFeaturesForAdd.put(predictFeat, featureTitle);

        }
        additionalInfo.put("predictedFeatures", predictedFeaturesForAdd);
        List<String> m = new ArrayList();
        model.setLinkedModels(m);
        model.setPredictedFeatures(pretrainedPredictedFeatures);
        model.setAdditionalInfo(additionalInfo);
        model.setPretrained(Boolean.TRUE);

//        Dataset datasetForPretrained = DatasetFactory.createEmpty(0);
//        ROG randomStringGenerator = new ROG(true);
//        datasetForPretrained.setId(randomStringGenerator.nextString(14));
//        datasetForPretrained.setFeatured(Boolean.FALSE);
//        datasetForPretrained.setMeta(MetaInfoBuilder.builder()
//                .addTitles("Dataset for pretrained model " + model.getMeta().getTitles().toArray()[0])
//                .addDescriptions("Dataset created to hold the independent and predictes features for "
//                        + "the pretrained model " + model.getMeta().getTitles().toArray()[0])
//                .addCreators(apiA)
//                .build()
//        );
//
//        datasetForPretrained.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
//        datasetForPretrained.setVisible(Boolean.TRUE);
//        datasetForPretrained.setExistence(Dataset.DatasetExistence.FROMPRETRAINED);
//        datasetForPretrained = DatasetFactory.addNullFeaturesFromPretrained(datasetForPretrained, pretrainedIndependentFeatures, pretrainedPredictedFeatures);
//
//        Set<FeatureInfo> featureInfos = new HashSet();
//        for (String indFeat : model.getIndependentFeatures()) {
//            String[] indFs = indFeat.split("/");
//            Feature feature = featureHandler.find(indFs[indFs.length - 1]);
//            FeatureInfo featInfo = new FeatureInfo();
//            featInfo.setName(feature.getMeta().getTitles().toArray()[0].toString());
//            String featureURI = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE) + "feature/" + feature.getId();
//            featInfo.setURI(featureURI);
//            featureInfos.add(featInfo);
//        }
//        for (String depFeat : model.getPredictedFeatures()) {
//            String[] indFs = depFeat.split("/");
//            Feature feature = featureHandler.find(indFs[indFs.length - 1]);
//            FeatureInfo featInfo = new FeatureInfo();
//            featInfo.setName(feature.getMeta().getTitles().toArray()[0].toString());
//            String featureURI = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE) + "feature/" + feature.getId();
//            featInfo.setURI(featureURI);
//            featureInfos.add(featInfo);
//        }
//
//        datasetForPretrained.setFeatures(featureInfos);
//
//        datasetLegacyWrapper.create(datasetForPretrained);
        //datasetHandler.create(datasetForPretrained);
//        String datasetURI = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE) + "dataset/" + datasetForPretrained.getId();
//        model.setDatasetUri(datasetURI);
        modelHandler.create(model);

        if (propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_EXISTS).equals("true")) {
            this.iep.sendJaqpotModelIDForIndex(model.getId(), IndexEntityProducer.EntityType.MODEL, IndexEntityProducer.IndexTransaction.INDEX);
        }

        ModelId mi = new ModelId();
        mi.setModelId(model.getId());
        return Response.ok(mi).build();
    }

    @DELETE
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(summary = "Deletes a particular Model resource",
            description = "Deletes a Model of a given ID. The method is idempondent, that is it can be used more than once without "
            + "triggering an exception/error. If the Model does not exist, the method will return without errors. "
            + "Authentication and authorization requirements apply, so clients that are not authenticated with a "
            + "valid token or do not have sufficient priviledges will not be able to delete Models using this method.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Model entry was deleted successfully (if found)."),
                @ApiResponse(responseCode = "401", description = "You are not authorized to delete this resource"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            },
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:DeleteJaqpotModel"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AcessToken"),
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotModelId")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:HttpStatus")
                })
            })

    public Response deleteModel(
            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to create resources on the server", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "id", description = "Id of the Model.", required = true, schema = @Schema(implementation = String.class)) @PathParam("id") String id
    ) throws JaqpotForbiddenException {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Model model = modelHandler.find(id);
        if (model == null) {
            throw new NotFoundException("The model with id:" + id + " was not found.");
        }

        MetaInfo metaInfo = model.getMeta();
        if (metaInfo.getLocked()) {
            throw new JaqpotForbiddenException("You cannot delete a Model that is locked.");
        }

        String userName = securityContext.getUserPrincipal().getName();
        if (!model.getMeta().getCreators().contains(userName)) {
            return Response.status(Response.Status.FORBIDDEN).entity("You cannot delete a Model that was not created by you.").build();
        }
        if (model.getTransformationModels() != null) {
            for (String transformationModel : model.getTransformationModels()) {
                modelHandler.remove(new Model(transformationModel));
            }
        }
        if (model.getLinkedModels() != null) {
            for (String linkedModel : model.getLinkedModels()) {
                modelHandler.remove(new Model(linkedModel));
            }
        }
        modelHandler.remove(new Model(id));

        if (propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_EXISTS).equals("true")) {
            this.diep.sendJaqpotEntityIDForDelete(id, IndexEntityProducer.EntityType.MODEL);
        }

        return Response.ok().build();
    }

    private List<String> retrieveRequiredFeatures(Model model) {
        if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
            String transModelId = model.getTransformationModels().get(0).split("model/")[1];
            Model transformationModel = modelHandler.findModelIndependentFeatures(transModelId);
            if (transformationModel != null && transformationModel.getIndependentFeatures() != null) {
                return transformationModel.getIndependentFeatures();
            }
        }
        return model.getIndependentFeatures();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/dataset")
    @Operation(summary = "Gets a dataset of a Model",
            description = "Geth the dataset of a model upon the criteria given",
            responses = {
                @ApiResponse(content = @Content(schema = @Schema(implementation = Dataset.class)))
            })
    public Response getModelDataset(
            @Parameter(name = "id", description = "id", required = true, schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "modeldataset", description = "description for the dataset", required = true, schema = @Schema(implementation = String.class, allowableValues = {"TRAINEDUPON","ALLEMPTY","EMPTYPREDICTION"})) @QueryParam("modeldataset") String modeldataset,
            @Parameter(name = "Authorization", description = "Authorization required", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Model model = modelHandler.find(id);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Dataset dataset = new Dataset();

        switch (modeldataset) {
            case "TRAINEDUPON":
                if (model.getDatasetUri() != null) {
                    String uri = model.getDatasetUri();
                    String[] urisplitted = uri.split("/");
                    String dataset_id = urisplitted[urisplitted.length - 1];
                    datasetLegacyWrapper.find(dataset_id);
                    //dataset = datasetHandler.find(dataset_id);
                    if (dataset == null) {
                        throw new NotFoundException(String.format("Dataset with id %s"
                                + " not found. Please contact admins", dataset_id));
                    }
                } else {
                    throw new BadRequestException("The specific model does not have a"
                            + " dataset trained upon. Propably a pretrained or a live model");
                }
                break;
            case "ALLEMPTY":
                model.getDependentFeatures();
        }

        return Response.status(Response.Status.OK).entity(dataset).build();
    }

    @PUT
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"application/json", MediaType.APPLICATION_JSON})
    @Path("{id}/meta")
    @Operation(summary = "Updates meta info of a dataset",
            description = "TUpdates meta info of a dataset",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MetaInfo.class)),
                        description = "Meta was updated succesfully"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this model"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response updateMeta(
            //@ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "id", description = "id", required = true, schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "modelForUpdate", schema = @Schema(implementation = Model.class)) Model modelForUpdate) throws URISyntaxException, JaqpotDocumentSizeExceededException, JaqpotNotAuthorizedException {

        String userId = securityContext.getUserPrincipal().getName();
        Model model = modelHandler.find(id);
        if (model == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        User user = userHandler.find(userId);
        Boolean canUpdate = rights.canWrite(modelForUpdate.getMeta(), user);
        if (canUpdate == true) {
            modelHandler.updateMeta(id, modelForUpdate.getMeta());
        } else {
            throw new JaqpotNotAuthorizedException("You are not authorized to update this resource");
        }

        if (propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_EXISTS).equals("true")) {
            this.iep.sendJaqpotModelIDForIndex(modelForUpdate.getId(), IndexEntityProducer.EntityType.MODEL, IndexEntityProducer.IndexTransaction.UPDATE);
        }

        return Response.accepted().entity(modelForUpdate.getMeta()).build();
    }

    @PUT
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"application/json", MediaType.APPLICATION_JSON})
    @Path("{id}/ontrash")
    @Operation(summary = "Puts a model on users trash",
            description = "Puts a model on users trash",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MetaInfo.class)),
                        description = "Meta was updated succesfully"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "You are not authorized to access this model"),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response updateOnTrash(
            //@ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "id", description = "id", required = true, schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            Model modelForUpdate) throws URISyntaxException, JaqpotDocumentSizeExceededException, JaqpotNotAuthorizedException {

        String userId = securityContext.getUserPrincipal().getName();
        Model model = modelHandler.find(id);
        if (model == null) {
            throw new NotFoundException("Could not find Model with id:" + id);
        }
        User user = userHandler.find(userId);
        Boolean canTrash = rights.canTrash(model.getMeta(), user);
        if (canTrash == true) {
            modelHandler.updateField(id, "onTrash", modelForUpdate.getOnTrash());
        } else {
            throw new JaqpotNotAuthorizedException("You are not authorized to update this resource");
        }
        return Response.accepted().entity(modelForUpdate.getMeta()).build();
    }

}
