/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licenced
 * with the aforementioned licence. 
 */
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
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
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.client.ClientUtils;
import org.jaqpot.core.service.data.PredictionService;
import org.jaqpot.core.service.dto.dataset.Dataset;
import org.jaqpot.core.service.dto.jpdi.PredictionRequest;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("model")
@Api(value = "/model", description = "Models API")
public class ModelResource {

    @Context
    UriInfo uriInfo;

    @EJB
    ModelHandler modelHandler;

    @EJB
    PredictionService predictionService;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Models",
            notes = "Finds all Models from Jaqpot Dataset",
            response = Model.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Models found and are listed in the response body"),
        @ApiResponse(code = 204, message = "No content: The request succeeded, but there are no models "
                + "matching your search criteria."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getModels(
            @ApiParam(value = "Creator of the model (username)") @QueryParam("creator") String creator
    ) {
        return Response.ok(modelHandler.findAll()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
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
        Model model = modelHandler.find(id);
        if (model == null) {
            return Response
                    .ok(ErrorReportFactory.notFoundError(uriInfo.getPath()))
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }
        return Response.ok(model).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}")
    @ApiOperation(value = "Creates Prediction",
            notes = "Creates Prediction",
            response = Task.class
    )
    public Response makePrediction(
            @FormParam("dataset_uri") String datasetURI,
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
        Task task = predictionService.initiatePrediction(options);
        return Response.ok(task).build();
    }
}
