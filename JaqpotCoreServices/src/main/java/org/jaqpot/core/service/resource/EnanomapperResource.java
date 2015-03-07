/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.service.data.ConjoinerService;
import org.jaqpot.core.service.data.TrainingService;
import org.jaqpot.core.service.dto.dataset.Dataset;

/**
 *
 * @author hampos
 */
@Path("enanomapper")
@Api(value = "/enanomapper", description = "Operations on eNM")
public class EnanomapperResource {

    @EJB
    ConjoinerService conjoinerService;

    @EJB
    TrainingService trainingService;

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

        Dataset dataset = conjoinerService.prepareDataset(bundleURI, subjectId);

        Map<String, Object> options = new HashMap<>();
        //  options.put("dataset_uri", datasetURI);
        options.put("prediction_feature", predictionFeature);
        options.put("subjectid", subjectId);
        options.put("algorithmId", algorithmURI);
        options.put("parameters", parameters);
        // Task task = trainingService.initiateTraining(options);
        return Response.ok(dataset).build();
    }

}
