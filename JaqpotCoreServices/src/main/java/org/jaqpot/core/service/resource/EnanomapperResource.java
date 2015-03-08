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
