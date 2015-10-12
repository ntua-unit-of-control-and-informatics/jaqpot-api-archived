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
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.Authorize;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("validation")
@Api(value = "/validation", description = "Validation API")
@Authorize
public class ValidationResource {

    @EJB
    TaskHandler taskHandler;

    @Context
    SecurityContext securityContext;

    @Resource(lookup = "java:jboss/exported/jms/topic/validation")
    private Topic validationQueue;

    @Inject
    private JMSContext jmsContext;

    @POST
    @Path("/test_set_validation")
    @ApiOperation(value = "Creates Validation Report",
            notes = "Creates Validation Report",
            response = Task.class
    )
    public Response validateModel(
            @FormParam("model_uri") String modelURI,
            @FormParam("test_dataset_uri") String datasetURI
    ) {

        return null;
    }

    @POST
    @Path("/training_test_cross")
    @ApiOperation(value = "Creates Validation Report",
            notes = "Creates Validation Report",
            response = Task.class
    )
    public Response crossValidateAlgorithm(
            @FormParam("algorithm_uri") String algorithmURI,
            @FormParam("training_dataset_uri") String datasetURI,
            @FormParam("algorithm_params") String algorithmParameters,
            @FormParam("prediction_feature") String predictionFeature,
            @FormParam("folds") Integer folds,
            @HeaderParam("subjectId") String subjectId
    ) {

        Task task = new Task(new ROG(true).nextString(12));
        task.setMeta(
                MetaInfoBuilder.builder()
                .setCurrentDate()
                .addTitles("Validation on algorithm: " + algorithmURI)
                .addComments("Validation task created")
                .addDescriptions("Validation task using algorithm " + algorithmURI + " and dataset " + datasetURI)
                .build());
        task.setType(Task.Type.VALIDATION);
        task.setCreatedBy(securityContext.getUserPrincipal().getName());
        task.setHttpStatus(202);
        task.setStatus(Task.Status.QUEUED);
        Map<String, Object> options = new HashMap<>();
        options.put("taskId", task.getId());
        options.put("algorithm_uri", algorithmURI);
        options.put("dataset_uri", datasetURI);
        options.put("algorithm_params", algorithmParameters);
        options.put("prediction_feature", predictionFeature);
        options.put("folds", folds);
        options.put("type", "CROSS");
        options.put("subjectId", subjectId);
        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(validationQueue, options);

        return Response.ok(task).build();
    }

    @POST
    @Path("/training_test_split")
    @ApiOperation(value = "Creates Validation Report",
            notes = "Creates Validation Report",
            response = Task.class
    )
    public Response splitValidateAlgorithm(
            @FormParam("algorithm_uri") String algorithmURI,
            @FormParam("training_dataset_uri") String datasetURI,
            @FormParam("algorithm_params") String algorithmParameters,
            @FormParam("prediction_feature") String predictionFeature,
            @FormParam("split_ratio") Double splitRatio,
            @HeaderParam("subjectId") String subjectId
    ) {

        Task task = new Task(new ROG(true).nextString(12));
        task.setMeta(
                MetaInfoBuilder.builder()
                .setCurrentDate()
                .addTitles("Validation on algorithm: " + algorithmURI)
                .addComments("Validation task created")
                .addDescriptions("Validation task using algorithm " + algorithmURI + " and dataset " + datasetURI)
                .build());
        task.setType(Task.Type.VALIDATION);
        task.setCreatedBy(securityContext.getUserPrincipal().getName());
        task.setHttpStatus(202);
        task.setStatus(Task.Status.QUEUED);
        Map<String, Object> options = new HashMap<>();
        options.put("taskId", task.getId());
        options.put("algorithm_uri", algorithmURI);
        options.put("dataset_uri", datasetURI);
        options.put("algorithm_params", algorithmParameters);
        options.put("prediction_feature", predictionFeature);
        options.put("split_ratio", splitRatio);
        options.put("type", "SPLIT");
        options.put("subjectId", subjectId);

        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(validationQueue, options);

        return Response.ok(task).build();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Retrieves Validation Report")
    public Response getValidationReport() {

        return null;
    }

}
