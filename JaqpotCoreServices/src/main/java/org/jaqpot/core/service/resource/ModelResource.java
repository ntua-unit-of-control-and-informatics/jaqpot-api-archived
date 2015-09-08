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
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.data.PredictionService;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("model")
@Api(value = "/model", description = "Models API")
@Authorize
public class ModelResource {

    private static final String DEFAULT_DATASET = "http://app.jaqpot.org:8080/jaqpot/services/dataset/corona";

    @Context
    UriInfo uriInfo;

    @EJB
    ModelHandler modelHandler;

    @EJB
    PredictionService predictionService;

    @Context
    SecurityContext securityContext;

    @Inject
    @UnSecure
    Client client;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Models",
            notes = "Finds all Models from Jaqpot Dataset. The response will list all models and will return either a URI list "
            + "of a list of JSON model objects. In the latter case, only the IDs, metadata, ontological classes "
            + "and reliability of the models will be returned. "
            + "Use the parameters start and max to get paginated results.",
            response = Model.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Models found and are listed in the response body"),
        @ApiResponse(code = 204, message = "No content: The request succeeded, but there are no models "
                + "matching your search criteria."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listModels(
            @ApiParam(value = "Creator of the model (username)") @QueryParam("creator") String creator,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", defaultValue = "20") @QueryParam("max") Integer max
    ) {
        if (max == null || max > 500) {
            max = 500;
        }
        return Response.ok(modelHandler.listOnlyIDsOfCreator(creator, start != null ? start : 0, max)).build();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Count all Models", response = Long.class)
    public Response countModels(@QueryParam("creator") String creator) {
        return Response.ok(modelHandler.countAllOfCreator(creator)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Path("/{id}")
    @ApiOperation(value = "Finds Model by Id",
            notes = "Finds specified Model",
            response = Model.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Model is found"),
        @ApiResponse(code = 401, message = "You are not authorized to access this model"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 404, message = "This model was not found."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getModel(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("subjectid") String subjectId) {
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
    @Produces(MediaType.APPLICATION_XML)
    @Path("/{id}/pmml")
    @ApiOperation(value = "Finds Model by Id",
            notes = "Finds specified Model",
            response = Model.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Model is found"),
        @ApiResponse(code = 401, message = "You are not authorized to access this model"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 404, message = "This model was not found."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getModelPmml(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("subjectid") String subjectId) {
        Model model = modelHandler.findModelPmml(id);
        if (model == null || model.getPmmlModel() == null) {
            throw new NotFoundException("The requested model was not found on the server.");
        }

        Object pmmlObj = model.getPmmlModel();
        if (pmmlObj instanceof List) {
            List pmmlObjList = (List) pmmlObj;
            Object pmml = pmmlObjList.stream().findFirst().orElse(null);
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
    @Produces({"text/uri-list"})
    @Path("/{id}/independent")
    @ApiOperation(value = "Lists the independent features of a Model",
            notes = "Lists the independent features of a Model. The result is available as a URI list.",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Model is found and its independent features are listed in the response body."),
        @ApiResponse(code = 401, message = "You are not authorized to access this model"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 404, message = "This model was not found."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listModelIndependentFeatures(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("subjectid") String subjectId) {

        Model foundModel = modelHandler.findModelIndependentFeatures(id);
        if (foundModel == null) {
            throw new NotFoundException("The requested model was not found on the server.");
        }
        return Response.ok(foundModel.getIndependentFeatures()).build();

    }

    @GET
    @Produces({"text/uri-list"})
    @Path("/{id}/dependent")
    @ApiOperation(value = "Lists the dependent features of a Model",
            notes = "Lists the dependent features of a Model identified by its ID. The result is available as a URI list.",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Model is found and its independent features are listed in the response body."),
        @ApiResponse(code = 401, message = "You are not authorized to access this model"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 404, message = "This model was not found."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listModelDependentFeatures(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("subjectid") String subjectId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(ErrorReportFactory.notImplementedYet())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();

    }

    @GET
    @Produces({"text/uri-list"})
    @Path("/{id}/predicted")
    @ApiOperation(value = "Lists the dependent features of a Model",
            notes = "Lists the predicted features of a Model identified by its ID. The result is available as a URI list.",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Model is found and its independent features are listed in the response body."),
        @ApiResponse(code = 401, message = "You are not authorized to access this model"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 404, message = "This model was not found."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listModelPredictedFeatures(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("subjectid") String subjectId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(ErrorReportFactory.notImplementedYet())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();

    }

    @GET
    @Produces({"text/uri-list"})
    @Path("/{id}/required")
    @ApiOperation(value = "Lists the required features of a Model",
            notes = "Lists the required features of a Model identified by its ID. The result is available as a URI list.",
            response = String.class,
            responseContainer = "List")
    public Response listModelRequiredFeatures(
            @PathParam("id") String id,
            @HeaderParam("subjectId") String subjectId) {
        Model model = modelHandler.find(id);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<String> requiredFeatures;
        if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
            requiredFeatures = client.target(model.getTransformationModels().get(0) + "/independent")
                    .request()
                    .accept("text/uri-list")
                    .header("subjectId", subjectId)
                    .get(List.class);

        } else {
            requiredFeatures = model.getIndependentFeatures();
        }
        return Response.status(Response.Status.OK).entity(requiredFeatures).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}")
    @ApiOperation(value = "Creates Prediction",
            notes = "Creates Prediction",
            response = Task.class
    )
    public Response makePrediction(
            @ApiParam(name = "dataset_uri", defaultValue = DEFAULT_DATASET) @FormParam("dataset_uri") String datasetURI,
            @PathParam("id") String id,
            @HeaderParam("subjectid") String subjectId) throws GeneralSecurityException {

        Model model = modelHandler.find(id);
        if (model == null) {
            throw new NotFoundException("Model not found.");
        }

        Map<String, Object> options = new HashMap<>();
        options.put("dataset_uri", datasetURI);
        options.put("subjectid", subjectId);
        options.put("modelId", id);
        options.put("createdBy", securityContext.getUserPrincipal().getName());
        options.put("base_uri", uriInfo.getBaseUri().toString());
        Task task = predictionService.initiatePrediction(options);
        return Response.ok(task).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Deletes a particular Model resource",
            notes = "Deletes a Model of a given ID. The method is idempondent, that is it can be used more than once without "
            + "triggering an exception/error. If the Model does not exist, the method will return without errors. "
            + "Authentication and authorization requirements apply, so clients that are not authenticated with a "
            + "valid token or do not have sufficient priviledges will not be able to delete Models using this method.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Model entry was deleted successfully (if found)."),
        @ApiResponse(code = 401, message = "You are not authorized to delete this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response deleteModel(
            @ApiParam("Clients need to authenticate in order to create resources on the server") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "ID of the Model.", required = true) @PathParam("id") String id
    ) {
        modelHandler.remove(new Model(id));
        return Response.ok().build();
    }
}
