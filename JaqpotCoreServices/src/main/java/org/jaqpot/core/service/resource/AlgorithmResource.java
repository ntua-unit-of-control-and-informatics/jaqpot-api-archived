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
import org.jaqpot.core.service.data.TrainingService;

/**
 *
 * @author hampos
 */
@Path("algorithm")
@Api(value = "/algorithm", description = "Operations about Algorithms")
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
            @FormParam("algorithmId") String algorithmId,
            @FormParam("training_service") String trainingServiceURI,
            @FormParam("prediction_service") String predictionServiceURI,
            @FormParam("parameters") String parameters,
            @HeaderParam("subjectid") String subjectId) {

        Algorithm algorithm = new Algorithm(algorithmId);
        algorithm.setCreatedBy("hampos");
        algorithm.setTrainingService(trainingServiceURI);
        algorithm.setPredictionService(predictionServiceURI);
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
