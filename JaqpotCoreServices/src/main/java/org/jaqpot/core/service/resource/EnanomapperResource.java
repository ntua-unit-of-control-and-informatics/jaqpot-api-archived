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
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.service.data.ConjoinerService;
import org.jaqpot.core.service.data.TrainingService;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.PredictionService;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("enanomapper")
@Api(value = "/enanomapper", description = "eNM API")
@Authorize
public class EnanomapperResource {

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

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Path("/training")
    @ApiOperation(value = "Creates Model",
            notes = "Reads Studies from Bundle's Substances, creates Dateaset,"
            + "calculates Descriptors, applies Dataset and Parameters on "
            + "Algorithm and creates Model.",
            response = Task.class
    )
    public Response trainEnmModel(
            @FormParam("bundle_uri") String bundleURI,
            @FormParam("algorithm_uri") String algorithmURI,
            @FormParam("prediction_feature") String predictionFeature,
            @FormParam("parameters") String parameters,
            @HeaderParam("subjectid") String subjectId) {

        Map<String, Object> options = new HashMap<>();
        options.put("bundle_uri", bundleURI);
        options.put("prediction_feature", predictionFeature);
        options.put("subjectid", subjectId);
        options.put("algorithmId", algorithmURI);
        options.put("parameters", parameters);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("mode", "TRAINING");
        Task task = conjoinerService.initiatePreparation(options, securityContext.getUserPrincipal().getName());
        return Response.ok(task).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Path("/prediction")
    @ApiOperation(value = "Creates Prediction",
            notes = "Reads Studies from Bundle's Substances, creates Dateaset,"
            + "calculates Descriptors, applies Dataset and Parameters on "
            + "Algorithm and creates Model.",
            response = Task.class
    )
    public Response makeEnmPrediction(
            @FormParam("bundle_uri") String bundleURI,
            @FormParam("model_uri") String modelURI,
            @HeaderParam("subjectid") String subjectId) {

        Model model = modelHandler.find(modelURI);
        if (model == null) {
            throw new NotFoundException("Model not found.");
        }
        Map<String, Object> options = new HashMap<>();
        options.put("bundle_uri", bundleURI);
        options.put("subjectid", subjectId);
        options.put("modelId", modelURI);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("mode", "PREDICTION");
        Task task = conjoinerService.initiatePreparation(options, securityContext.getUserPrincipal().getName());
        return Response.ok(task).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Path("/dataset")
    @ApiOperation(value = "Creates Dataset",
            notes = "Reads Studies from Bundle's Substances, creates Dateaset,"
            + "calculates Descriptors, returns Dataset",
            response = Task.class
    )
    public Response prepareDataset(
            @FormParam("bundle_uri") String bundleURI,
            @FormParam("transformations") String transformations,
            @HeaderParam("subjectid") String subjectId) {

        Map<String, Object> options = new HashMap<>();
        options.put("bundle_uri", bundleURI);
        options.put("transformations", transformations);
        options.put("subjectid", subjectId);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("mode", "PREPARATION");
        Task task = conjoinerService.initiatePreparation(options, securityContext.getUserPrincipal().getName());
        return Response.ok(task).build();
    }

}
