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
import java.util.HashMap;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.ReportHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Report;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.annotations.UnSecure;

/**
 *
 * @author hampos
 */
@Path("interlab")
@Api(value = "/interlab", description = "Interlab Testing API")
@Produces(MediaType.APPLICATION_JSON)
@Authorize
public class InterLabTestingResource {

    @Inject
    @UnSecure
    Client client;

    @Inject
    @Jackson
    JSONSerializer jsonSerializer;

    @EJB
    ReportHandler reportHandler;

    @POST
    @Path("/test")
    @ApiOperation(value = "Creates Interlab Testing Report",
            notes = "Creates Interlab Testing Report",
            response = Report.class
    )
    public Response interLabTest(
            @FormParam("dataset_uri") String datasetURI,
            @FormParam("prediction_feature") String predictionFeature,
            @FormParam("parameters") String parameters,
            @HeaderParam("subjectid") String subjectId
    ) {

        Dataset dataset = client.target(datasetURI)
                .request()
                .header("subjectid", subjectId)
                .accept(MediaType.APPLICATION_JSON)
                .get(Dataset.class);
        dataset.setDatasetURI(datasetURI);

        TrainingRequest trainingRequest = new TrainingRequest();
        trainingRequest.setDataset(dataset);
        trainingRequest.setPredictionFeature(predictionFeature);

        if (parameters != null && !parameters.isEmpty()) {
            HashMap<String, Object> parameterMap = jsonSerializer.parse(parameters, new HashMap<String, Object>().getClass());
            trainingRequest.setParameters(parameterMap);
        }

        Report report = client.target("http://test.jaqpot.org:8090/pws/interlabtest")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(trainingRequest), Report.class);

        reportHandler.create(report);

        return Response.ok(report).build();
    }

}
