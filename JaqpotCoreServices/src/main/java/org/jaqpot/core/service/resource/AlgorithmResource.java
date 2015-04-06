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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.EJB;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.builder.AlgorithmBuilder;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.TrainingService;
import org.jaqpot.core.service.exceptions.QuotaExceededException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("algorithm")
@Api(value = "/algorithm", description = "Algorithms API")
@Produces({"application/json", "text/uri-list"})
@Authorize
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
            DEFAULT_DATASET = "http://enanomapper.ntua.gr:8880/jaqpot/services/dataset/ca8da7f6-ee9f-4a61-9ae4-b1d1525cef88",
            DEFAULT_PRED_FEATURE = "property/TOX/UNKNOWN_TOXICITY_SECTION/Total+surface+area++SAtot+/52D93BC3B68F26C8E787CC7A05E5130A23164405/3ed642f9-1b42-387a-9966-dea5b91e5f8a",
            DEFAULT_DOA = "http://enanomapper.ntua.gr:8880/jaqpot/services/algorithm/l2";

    @EJB
    TrainingService trainingService;

    @EJB
    AlgorithmHandler algorithmHandler;

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;

    @EJB
    UserHandler userHandler;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Algorithms",
            notes = "Finds all Algorithms JaqpotQuattro supports",
            response = Algorithm.class,
            responseContainer = "List")

    public Response getAlgorithms(@ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max", defaultValue = "10") @QueryParam("max") Integer max) {
        return Response
                .ok(algorithmHandler.findAll(start != null ? start : 0, max != null ? max : Integer.MAX_VALUE))
                .build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Creates Algorithm",
            notes = "Creates a new JPDI compliant Algorithm Service",
            response = Algorithm.class
    )
    public Response createAlgorithm(
            @ApiParam(value = "Algorithm in JSON", defaultValue = DEFAULT_ALGORITHM, required = true) Algorithm algorithm,
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "Title of your algorithm") @HeaderParam("title") String title,
            @ApiParam(value = "Short description of your algorithm") @HeaderParam("description") String description,
            @ApiParam(value = "Tags for your algorithm (in a comma separated list) to facilitate look-up") @HeaderParam("tags") String tags
    ) throws QuotaExceededException {

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long algorithmCount = algorithmHandler.countByUser(user.getId());
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

        AlgorithmBuilder algorithmBuilder = AlgorithmBuilder.builder(algorithm)
                .setCreatedBy(securityContext.getUserPrincipal().getName());
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
        algorithmHandler.create(algorithm);
        return Response
                .status(Response.Status.OK)
                .header("Location", uriInfo.getBaseUri().toString() + "algorithm/" + algorithm.getId())
                .entity(algorithm).build();
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
            @ApiParam(name = "dataset_uri", defaultValue = DEFAULT_DATASET) @FormParam("dataset_uri") String datasetURI,
            @ApiParam(name = "prediction_feature", defaultValue = DEFAULT_PRED_FEATURE) @FormParam("prediction_feature") String predictionFeature,
            @FormParam("parameters") String parameters,
            @FormParam("transformations") String transformations,
            @ApiParam(name = "doa", defaultValue = DEFAULT_DOA) @FormParam("doa") String doa,
            @PathParam("id") String algorithmId,
            @HeaderParam("subjectid") String subjectId) {
        Map<String, Object> options = new HashMap<>();
        options.put("dataset_uri", datasetURI);
        options.put("prediction_feature", predictionFeature);
        options.put("subjectid", subjectId);
        options.put("algorithmId", algorithmId);
        options.put("parameters", parameters);
        options.put("transformations", transformations);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("doa", doa);
        Task task = trainingService.initiateTraining(options, securityContext.getUserPrincipal().getName());
        task.setHttpStatus(202);
        task.setStatus(Task.Status.QUEUED);
        return Response.ok(task).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(value = "Unregisters an algorithm of given ID",
            notes = "Deletes an algorithm of given ID. The application of this method "
            + "requires authentication and assumes certain priviledges."
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Algorithm deleted successfully"),
        @ApiResponse(code = 401, message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 403, message = "This is a forbidden operation (do not attempt to repeat it)."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response deleteAlgorithm(
            @ApiParam(value = "ID of the task which is to be deleted.", required = true) @PathParam("id") String id,
            @HeaderParam("subjectid") String subjectId) {

        algorithmHandler.remove(new Algorithm(id));
        return Response.ok().build();
    }

}
