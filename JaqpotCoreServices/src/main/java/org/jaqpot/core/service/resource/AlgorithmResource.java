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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.AlgorithmBuilder;
import org.jaqpot.core.service.data.TrainingService;

/**
 *
 * @author hampos
 */
@Path("algorithm")
@Api(value = "/algorithm", description = "Algorithms API")
@Produces({"application/json", "text/uri-list"})
public class AlgorithmResource {

    @EJB
    TrainingService trainingService;

    @EJB
    AlgorithmHandler algorithmHandler;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Algorithms",
            notes = "Finds all Algorithms JaqpotQuattro supports",
            response = Algorithm.class,
            responseContainer = "List")

    public Response getAlgorithms() {
        return Response.ok(algorithmHandler.findAll()).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Creates Algorithm",
            notes = "Creates a new JPDI compliant Algorithm Service",
            response = Algorithm.class
    )
    public Response createAlgorithm(
            @ApiParam(value = "Unique ID of the algorithm. If not provided, a UUID will be created") @FormParam("algorithmId") String algorithmId,
            @ApiParam(value = "URI of the JPDI training service", required = true) @FormParam("training_service") String trainingServiceURI,
            @ApiParam(value = "URI of the corresponding JPDI prediction service", required = true) @FormParam("prediction_service") String predictionServiceURI,
            @ApiParam(value = "Parameters that are expected by the algorithm", required = true) @FormParam("parameters") String parameters,
            @ApiParam(value = "Auth token", required = true) @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "Title of your algorithm") @HeaderParam("title") String title,
            @ApiParam(value = "Short description of your algorithm") @HeaderParam("description") String description,
            @ApiParam(value = "Tags for your algorithm (in a comma separated list) to facilitate look-up") @HeaderParam("tags") String tags
    ) {

        if (algorithmId == null) {
            algorithmId = UUID.randomUUID().toString();
        }
        Algorithm algorithm = AlgorithmBuilder
                .builder(algorithmId)
                .setCreatedBy("user")
                .setTrainingService(trainingServiceURI)
                .setPredictionService(predictionServiceURI)
                .addTitles(title)
                .addDescriptions(description)
                .addTagsCSV(tags)
                .build();
        //TODO: Take care of parameters. How are they provided? As JSON?
        algorithmHandler.create(algorithm);
        return Response.status(Response.Status.CREATED).entity(algorithm).build();
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds Algorithm",
            notes = "Finds Algorithm with provided name",
            response = Algorithm.class
    )
    public Response getAlgorithm(@PathParam("id") String algorithmId) {
        Algorithm algorithm = algorithmHandler.find(algorithmId);
        if (algorithm == null) {
            throw new NotFoundException("Could not find Algorithm with id:" + algorithmId);
        }
        return Response.ok(algorithm).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Path("/{id}")
    @ApiOperation(value = "Creates Model",
            notes = "Applies Dataset and Parameters on Algorithm and creates Model.",
            response = Task.class
    )
    public Response trainModel(
            @FormParam("dataset_uri") String datasetURI,
            @FormParam("prediction_feature") String predictionFeature,
            @FormParam("parameters") String parameters,
            @PathParam("id") String algorithmId,
            @HeaderParam("subjectid") String subjectId) {
        Map<String, Object> options = new HashMap<>();
        options.put("dataset_uri", datasetURI);
        options.put("prediction_feature", predictionFeature);
        options.put("subjectid", subjectId);
        options.put("algorithmId", algorithmId);
        options.put("parameters", parameters);
        Task task = trainingService.initiateTraining(options);
        return Response.ok(task).build();
    }
}
