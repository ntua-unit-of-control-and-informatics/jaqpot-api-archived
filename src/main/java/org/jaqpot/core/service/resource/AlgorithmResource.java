/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
//import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.jaxrs.PATCH;
import org.apache.commons.validator.routines.UrlValidator;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.AlgorithmBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.data.TrainingService;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.JaqpotForbiddenException;
import org.jaqpot.core.service.exceptions.parameter.*;
import org.jaqpot.core.service.exceptions.QuotaExceededException;
import org.jaqpot.core.service.validator.ParameterValidator;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("/algorithm")
//@Api(value = "/algorithm", description = "Algorithms API")
@Produces({"application/json", "text/uri-list"})
//@Authorize
@Tag(name = "algorithm")
@SecurityScheme(name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        description = "add the token retreived from oidc. Example:  Bearer <API_KEY>"
        )
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class AlgorithmResource {

    private static final Logger LOG = Logger.getLogger(AlgorithmResource.class.getName());

    private static final String DEFAULT_ALGORITHM = "{\n"
            + "  \"trainingService\":\"http://z.ch/t/a\",\n"
            + "  \"predictionService\":\"http://z.ch/p/b\",\n"
            + "  \"ontologicalClasses\":[\n"
            + "        \"ot:Algorithm\",\n"
            + "        \"ot:Regression\",\n"
            + "        \"ot:SupervisedLearning\"\n"
            + "       ],\n"
            + "  \"parameters\": [\n"
            + "    {\n"
            + "       \"name\":\"alpha\",\n"
            + "       \"scope\":\"OPTIONAL\",\n"
            + "       \"value\":101.635\n"
            + "    }\n"
            + "  ]\n"
            + "}",
            DEFAULT_DATASET = "http://app.jaqpot.org:8080/jaqpot/services/dataset/corona",
            DEFAULT_PRED_FEATURE = "https://apps.ideaconsult.net/enmtest/property/TOX/UNKNOWN_TOXICITY_SECTION/Log2+transformed/94D664CFE4929A0F400A5AD8CA733B52E049A688/3ed642f9-1b42-387a-9966-dea5b91e5f8a",
            DEFAULT_DOA = "http://app.jaqpot.org:8080/jaqpot/services/algorithm/leverage",
            SCALING = "http://app.jaqpot.org:8080/jaqpot/services/algorithm/scaling",
            DEFAULT_TRANSFORMATIONS = "http://app.jaqpot.org:8080/jaqpot/services/pmml/corona-standard-transformations",
            STANDARIZATION = "http://app.jaqpot.org:8080/jaqpot/services/algorithm/standarization";

    @EJB
    TrainingService trainingService;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ModelHandler modelHandler;

    @Context
    SecurityContext securityContext;

    @EJB
    DatasetHandler datasetHandler;

    @Context
    UriInfo uriInfo;

    @EJB
    UserHandler userHandler;

    @Inject
    @Jackson
    JSONSerializer serializer;

    @Inject
    ParameterValidator parameterValidator;

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    /*@ApiOperation(
     value = "Finds all Algorithms",
     notes = "Finds all Algorithms JaqpotQuattro supports",
     extensions = {
     @Extension(properties = {
     @ExtensionProperty(name = "orn-@type", value = "x-orn:Algorithm"),
     }
     ),
     @Extension(name = "orn:returns",properties={
     @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AlgorithmList")
     })
     }
     )
     @ApiResponses(value = {
     @ApiResponse(code = 401, response=  ErrorReport.class , message = "Wrong, missing or insufficient credentials. Error report is produced."),
     @ApiResponse(code = 200,  response = Algorithm.class, responseContainer = "List" , message = "A list of algorithms in the Jaqpot framework"),
     @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")

     })*/
    @Operation(
            summary = "Finds all Algorithms",
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:Algorithm"),}
                ),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AlgorithmList")
                })
            },
            responses = {
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Algorithm.class, description = "A list of algorithms in the Jaqpot framework")))),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class, description = "Internal server error - this request cannot be served.")))

            })
    public Response getAlgorithms(
            @Parameter(description = "Authorization token") @HeaderParam("apiKey") String apiKey,
            @Parameter(description = "class") @QueryParam("class") String ontologicalClass,
            @Parameter(description = "start", schema = @Schema(type = "String", defaultValue = "0")) @QueryParam("start") Integer start,
            @Parameter(description = "max", schema = @Schema(type = "String", defaultValue = "10")) @QueryParam("max") Integer max) {
        if (ontologicalClass != null && !ontologicalClass.isEmpty()) {
            return Response
                    .ok(algorithmHandler.findByOntologicalClass(ontologicalClass, start != null ? start : 0, max != null ? max : Integer.MAX_VALUE))
                    .header("total", algorithmHandler.countByOntologicalClass(ontologicalClass))
                    .build();
        }
        return Response
                .ok(algorithmHandler.findAll(start != null ? start : 0, max != null ? max : Integer.MAX_VALUE))
                .header("total", algorithmHandler.countAll())
                .build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(
            summary = "Creates Algorithm",
            description = "Registers a new JPDI-compliant algorithm service. When registering a new JPDI-compliant algorithm web service "
            + "it is crucial to propertly annotate your algorithm with appropriate ontological classes following the "
            + "<a href=\"http://opentox.org/dev/apis/api-1.1/Algorithms\">OpenTox algorithms ontology</a>. For instance, a "
            + "Clustering algorithm must be annotated with <code>ot:Clustering</code>. It is also important for "
            + "discoverability to add tags to your algorithm using the <code>meta.subjects</code> field. An example is "
            + "provided below.",
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:Algorithm"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Algorithm")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Status")
                })
            },
            responses = {
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Algorithm quota has been exceeded"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Algorithm.class)), description = "Algorithm successfully registered in the system"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response createAlgorithm(
            @Parameter(description = "Algorithm in JSON", schema = @Schema(implementation = Algorithm.class, defaultValue = DEFAULT_ALGORITHM), required = true) Algorithm algorithm,
            @Parameter(description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(description = "Title of your algorithm", schema = @Schema(implementation = String.class)) @HeaderParam("title") String title,
            @Parameter(description = "Short description of your algorithm", schema = @Schema(implementation = String.class)) @HeaderParam("description") String description,
            @Parameter(description = "Tags for your algorithm (in a comma separated list) to facilitate look-up", schema = @Schema(implementation = String.class)) @HeaderParam("tags") String tags
    ) throws QuotaExceededException, JaqpotDocumentSizeExceededException {

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long algorithmCount = algorithmHandler.countAllOfCreator(user.getId());
        int maxAllowedAlgorithms = new UserFacade(user).getMaxAlgorithms();

        if (algorithmCount > maxAllowedAlgorithms) {
            LOG.info(String.format("User %s has %d algorithms while maximum is %d",
                    user.getId(), algorithmCount, maxAllowedAlgorithms));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + algorithmCount + " algorithms. "
                    + "No more than " + maxAllowedAlgorithms + " are allowed with your subscription.");
        }

        if (algorithm.getId() == null) {
            ROG rog = new ROG(true);
            algorithm.setId(rog.nextString(10));
        }

        AlgorithmBuilder algorithmBuilder = AlgorithmBuilder.builder(algorithm);

        if (title != null) {
            algorithmBuilder.addTitles(title);
        }
        if (description != null) {
            algorithmBuilder.addDescriptions(description);
        }
        if (tags != null) {
            algorithmBuilder.addTagsCSV(tags);
        }
        algorithm = algorithmBuilder.build();
        if (algorithm.getMeta() == null) {
            algorithm.setMeta(new MetaInfo());
        }
        algorithm.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
        algorithmHandler.create(algorithm);
        return Response
                .status(Response.Status.OK)
                .header("Location", uriInfo.getBaseUri().toString() + "algorithm/" + algorithm.getId())
                .entity(algorithm).build();
    }

    @GET
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list", "application/ld+json"})
    @Operation(summary = "Finds Algorithm",
            description = "Finds Algorithm with provided name",
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:Algorithm"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AlgorithmId")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Algorithm")
                })
            },
            responses = {
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Algorithm was not found"),
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Algorithm.class)), description = "Algorithm was found in the system"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")

            })
    public Response getAlgorithm(
            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
            @PathParam("id") String algorithmId) throws ParameterIsNullException {
        if (algorithmId == null) {
            throw new ParameterIsNullException("algorithmId");
        }

        Algorithm algorithm = algorithmHandler.find(algorithmId);
        if (algorithm == null) {
            throw new NotFoundException("Could not find Algorithm with id:" + algorithmId);
        }
        return Response.ok(algorithm).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @Operation(summary = "Creates Model",
            description = "Applies Dataset and Parameters on Algorithm and creates Model.",
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:Model"),}),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AlgorithmId"),
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:OperrationParameters")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotModelingTaskId")
                })
            },
            responses = {
                @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Bad request. More info can be found in details of Error Report."),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Algorithm was not found."),
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Task.class)), description = "The process has successfully been started. A task URI is returned."),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
//    @Parameters({
////        @Parameter(name = "title", required = true, schema = @Schema(implementation = String.class, type = "String"), in = ParameterIn.QUERY),
////        @Parameter(name = "decription", required = true, schema = @Schema(implementation = String.class, type = "String"), in = ParameterIn.QUERY),
////        @Parameter(name = "dataset_uri", schema = @Schema(type = "String", defaultValue = DEFAULT_DATASET), in = ParameterIn.QUERY),
////        @Parameter(name = "prediction_feature", schema = @Schema(type = "String", defaultValue = DEFAULT_PRED_FEATURE), in = ParameterIn.QUERY),
////        @Parameter(name = "parameters", schema = @Schema(type = "String"), in = ParameterIn.QUERY),
////        @Parameter(name = "transformations", schema = @Schema(type = "String", defaultValue = DEFAULT_TRANSFORMATIONS), in = ParameterIn.QUERY),
////        @Parameter(name = "scaling", schema = @Schema(type = "String", defaultValue = STANDARIZATION), in = ParameterIn.QUERY),
////        @Parameter(name = "doa", schema = @Schema(type = "String", defaultValue = DEFAULT_DOA), in = ParameterIn.QUERY),
////        @Parameter(name = "id", schema = @Schema(type = "String"), in = ParameterIn.PATH),
////        @Parameter(name = "Authorization", schema = @Schema(type = "String"), in = ParameterIn.HEADER)
//    })

    @org.jaqpot.core.service.annotations.Task
    public Response trainModel(
            @Parameter(name = "title", required = true, schema = @Schema(implementation = String.class, type = "String")) @FormParam("title") String title,
            @Parameter(name = "decription", required = true, schema = @Schema(implementation = String.class, type = "String")) @FormParam("description") String description,
            @Parameter(name = "dataset_uri", schema = @Schema(type = "String", defaultValue = DEFAULT_DATASET)) @FormParam("dataset_uri") String datasetURI,
            @Parameter(name = "prediction_feature", schema = @Schema(type = "String", defaultValue = DEFAULT_PRED_FEATURE)) @FormParam("prediction_feature") String predictionFeature,
            @Parameter(name = "parameters", schema = @Schema(type = "String")) @FormParam("parameters") String parameters,
            @Parameter(name = "transformations", schema = @Schema(type = "String", defaultValue = DEFAULT_TRANSFORMATIONS)) @FormParam("transformations") String transformations,
            @Parameter(name = "scaling", schema = @Schema(type = "String", defaultValue = STANDARIZATION)) @FormParam("scaling") String scaling, //, allowableValues = SCALING + "," + STANDARIZATION
            @Parameter(name = "doa", schema = @Schema(type = "String", defaultValue = DEFAULT_DOA)) @FormParam("doa") String doa,
            @Parameter(name = "id", schema = @Schema(type = "String")) @PathParam("id") String algorithmId,
             @Parameter(name = "Authorization", schema = @Schema(type = "String")) @HeaderParam("Authorization") String api_key) throws QuotaExceededException, ParameterIsNullException, ParameterInvalidURIException, ParameterTypeException, ParameterRangeException, ParameterScopeException, JaqpotDocumentSizeExceededException {
        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Algorithm algorithm = algorithmHandler.find(algorithmId);
        if (algorithm == null) {
            throw new NotFoundException("Could not find Algorithm with id:" + algorithmId);
        }

        //Dataset validation should happen only in regression and classification algorithms
        if (algorithm.getOntologicalClasses().contains("ot:Regression")
                || algorithm.getOntologicalClasses().contains("ot:Classification")) {

            if (datasetURI == null) {
                throw new ParameterIsNullException("datasetURI");
            }

            if (!urlValidator.isValid(datasetURI)) {
                throw new ParameterInvalidURIException("Not valid Dataset URI.");
            }

            String datasetId = datasetURI.split("dataset/")[1];
            Dataset datasetMeta = datasetHandler.findMeta(datasetId);

            if (datasetMeta.getTotalRows() != null && datasetMeta.getTotalRows() < 2) {
                throw new BadRequestException("Cannot train model on dataset with less than 2 rows.");
            }
        }

        //Prediction validation should not happen in enm:NoTarget  algorithms
        if (algorithm.getOntologicalClasses().contains("enm:NoTarget")) {
            if (predictionFeature == null) {
                throw new ParameterIsNullException("predictionFeature");
            }
            if (!urlValidator.isValid(predictionFeature)) {
                throw new ParameterInvalidURIException("Not valid Prediction Feature URI.");
            }
        }

        if (title == null) {
            throw new ParameterIsNullException("title");
        }
        if (description == null) {
            throw new ParameterIsNullException("description");
        }

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

        Map<String, Object> options = new HashMap<>();
        options.put("title", title);
        options.put("description", description);
        options.put("dataset_uri", datasetURI);
        options.put("prediction_feature", predictionFeature);
        options.put("api_key", apiKey);
        options.put("algorithmId", algorithmId);
        options.put("parameters", parameters);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("creator", securityContext.getUserPrincipal().getName());

        Map<String, String> transformationAlgorithms = new LinkedHashMap<>();
        if (transformations != null && !transformations.isEmpty()) {
            transformationAlgorithms.put(uriInfo.getBaseUri().toString() + "algorithm/pmml",
                    "{\"transformations\" : \"" + transformations + "\"}");
        }
        if (scaling != null && !scaling.isEmpty()) {
            transformationAlgorithms.put(scaling, "");
        }
        if (doa != null && !doa.isEmpty()) {
            transformationAlgorithms.put(doa, "");
        }
        if (!transformationAlgorithms.isEmpty()) {
            String transformationAlgorithmsString = serializer.write(transformationAlgorithms);
            LOG.log(Level.INFO, "Transformations:{0}", transformationAlgorithmsString);
            options.put("transformations", transformationAlgorithmsString);
        }

        parameterValidator.validate(parameters, algorithm.getParameters());

        //return Response.ok().build();
        Task task = trainingService.initiateTraining(options, securityContext.getUserPrincipal().getName());

        return Response.ok(task).build();
    }

    @DELETE
    @TokenSecured({RoleEnum.ADMNISTRATOR})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Operation(summary = "Unregisters an algorithm of given ID",
            description = "Deletes an algorithm of given ID. The application of this method "
            + "requires authentication and assumes certain priviledges.",
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:DeletesAlgorithm"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AlgorithmId"),
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:OperrationParameters")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:HttpStatus")
                })
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Algorithm deleted successfully"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This is a forbidden operation (do not attempt to repeat it)."),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response deleteAlgorithm(
            @Parameter(description = "ID of the algorithm which is to be deleted.", required = true) @PathParam("id") String id,
            @HeaderParam("apiKey") String apiKey) throws ParameterIsNullException, JaqpotForbiddenException {

        if (id == null) {
            throw new ParameterIsNullException("id");
        }

        Algorithm algorithm = algorithmHandler.find(id);

        MetaInfo metaInfo = algorithm.getMeta();
        if (metaInfo.getLocked()) {
            throw new JaqpotForbiddenException("You cannot delete an Algorithm that is locked.");
        }

        String userName = securityContext.getUserPrincipal().getName();

        if (!algorithm.getMeta().getCreators().contains(userName)) {
            return Response.status(Response.Status.FORBIDDEN).entity("You cannot delete an Algorithm that was not created by you.").build();
        }

        algorithmHandler.remove(new Algorithm(id));
        return Response.ok().build();
    }

    //@PATCH
    @PUT
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Modifies a particular Algorithm resource",
            description = "Modifies an Algorithm resource of a given ID. ",
           // description = "Modifies (applies a patch on) an Algorithm resource of a given ID. "
           // + "This implementation of PATCH follows the RFC 6902 proposed standard. "
           // + "See https://tools.ietf.org/rfc/rfc6902.txt for details.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Algorithm.class)), description = "Algorithm patched successfully"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "This is a forbidden operation (do not attempt to repeat it)."),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response modifyAlgorithm(
            @Parameter(description = "Clients need to authenticate in order to create resources on the server") @HeaderParam("apiKey") String apiKey,
            @Parameter(description = "ID of an existing BibTeX.", required = true) @PathParam("id") String id,
            @Parameter(description = "The patch in JSON according to the RFC 6902 specs", required = true) String patch
    ) throws JsonPatchException, JsonProcessingException {

        Algorithm originalAlgorithm = algorithmHandler.find(id); // find doc in DB
        if (originalAlgorithm == null) {
            throw new NotFoundException("Algorithm with ID " + id + " not found.");
        }

        Algorithm modifiedAsAlgorithm = serializer.patch(originalAlgorithm, patch, Algorithm.class);
        if (modifiedAsAlgorithm == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ErrorReportFactory.badRequest("Patch cannot be applied because the request is malformed", "Bad patch"))
                    .build();
        }

        algorithmHandler.edit(modifiedAsAlgorithm); // update the entry in the DB

        return Response
                .ok(modifiedAsAlgorithm)
                .build();
    }
}
