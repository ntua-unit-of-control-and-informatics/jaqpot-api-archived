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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.validator.routines.UrlValidator;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.ReportHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.exceptions.parameter.ParameterInvalidURIException;
import org.jaqpot.core.service.exceptions.parameter.ParameterIsNullException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;

/**
 * @author Angelos Valsamis
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("validation")
@Api(value = "/validation", description = "Validation API")
@Produces(MediaType.APPLICATION_JSON)
@Authorize
public class ValidationResource {

    private static final Logger LOG = Logger.getLogger(ValidationResource.class.getName());

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
            DEFAULT_DATASET = "http://app.jaqpot.org:8080/jaqpot/services/dataset/corona",
            DEFAULT_PRED_FEATURE = "https://apps.ideaconsult.net/enmtest/property/TOX/UNKNOWN_TOXICITY_SECTION/Log2+transformed/94D664CFE4929A0F400A5AD8CA733B52E049A688/3ed642f9-1b42-387a-9966-dea5b91e5f8a",
            DEFAULT_DOA = "http://app.jaqpot.org:8080/jaqpot/services/algorithm/leverage",
            SCALING = "http://app.jaqpot.org:8080/jaqpot/services/algorithm/scaling",
            DEFAULT_TRANSFORMATIONS = "http://app.jaqpot.org:8080/jaqpot/services/pmml/corona-standard-transformations",
            STANDARIZATION = "http://app.jaqpot.org:8080/jaqpot/services/algorithm/standarization";

    @EJB
    TaskHandler taskHandler;

    @EJB
    UserHandler userHandler;

    @EJB
    ReportHandler reportHandler;

    @Inject
    @Jackson
    JSONSerializer serializer;

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;

    @Resource(lookup = "java:jboss/exported/jms/topic/validationCross")
    private Topic crossValidationQueue;

    @Resource(lookup = "java:jboss/exported/jms/topic/validationSplit")
    private Topic splitValidationQueue;

    @Resource(lookup = "java:jboss/exported/jms/topic/validationExternal")
    private Topic externalValidationQueue;

    @Inject
    private JMSContext jmsContext;


    @POST
    @Path("/training_test_cross")
    @ApiOperation(value = "Creates Validation Report",
            notes = "Creates Validation Report",
            response = Task.class
    )
    @org.jaqpot.core.service.annotations.Task
    public Response crossValidateAlgorithm(
            @FormParam("algorithm_uri") String algorithmURI,
            @FormParam("training_dataset_uri") String datasetURI,
            @FormParam("algorithm_params") String algorithmParameters,
            @FormParam("prediction_feature") String predictionFeature,
            @ApiParam(name = "transformations", defaultValue = DEFAULT_TRANSFORMATIONS) @FormParam("transformations") String transformations,
            @ApiParam(name = "scaling", defaultValue = STANDARIZATION) @FormParam("scaling") String scaling, //, allowableValues = SCALING + "," + STANDARIZATION
            @FormParam("folds") Integer folds,
            @FormParam("stratify") String stratify,
            @FormParam("seed") Integer seed,
            @HeaderParam("subjectId") String subjectId
    ) throws QuotaExceededException, JMSException, ParameterInvalidURIException, ParameterIsNullException {
        if (algorithmURI==null)
            throw new ParameterIsNullException("algorithmURI");
        if (datasetURI==null)
            throw new ParameterIsNullException("datasetURI");
        if (folds==null)
            throw new ParameterIsNullException("folds");

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long reportCount = reportHandler.countAllOfCreator(user.getId());
        int maxAllowedReports = new UserFacade(user).getMaxReports();

        if (reportCount > maxAllowedReports) {
            LOG.info(String.format("User %s has %d algorithms while maximum is %d",
                    user.getId(), reportCount, maxAllowedReports));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + reportCount + " reports. "
                    + "No more than " + maxAllowedReports + " are allowed with your subscription.");
        }

        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(algorithmURI)) {
            throw new ParameterInvalidURIException("Not valid algorithm URI.");
        }
        if (!urlValidator.isValid(datasetURI)) {
            throw new ParameterInvalidURIException("Not valid dataset URI.");
        }
        if (!urlValidator.isValid(predictionFeature)) {
            throw new ParameterInvalidURIException("Not valid prediction feature URI.");
        }
        if (transformations != null && !transformations.isEmpty() && !urlValidator.isValid(transformations)) {
            throw new ParameterInvalidURIException("Not valid transformation URI.");
        }
        if (scaling != null && !scaling.isEmpty() && !urlValidator.isValid(scaling)) {
            throw new ParameterInvalidURIException("Not valid scaling URI.");
        }
        if ((stratify != null && !stratify.isEmpty() && !stratify.equals("random") && !stratify.equals("normal"))) {
            throw new BadRequestException("Not valid stratify option - choose between random and normal");
        }

        Task task = new Task(new ROG(true).nextString(12));
        task.setMeta(
                MetaInfoBuilder.builder()
                .setCurrentDate()
                .addTitles("Validation on algorithm: " + algorithmURI)
                .addComments("Validation task created")
                .addDescriptions("Validation task using algorithm " + algorithmURI + " and dataset " + datasetURI)
                .addCreators(securityContext.getUserPrincipal().getName())
                .build());
        task.setType(Task.Type.VALIDATION);
        task.setHttpStatus(202);
        task.setStatus(Task.Status.QUEUED);
        task.setVisible(Boolean.TRUE);
        Map<String, Object> options = new HashMap<>();
        options.put("taskId", task.getId());
        options.put("algorithm_uri", algorithmURI);
        options.put("dataset_uri", datasetURI);
        options.put("algorithm_params", algorithmParameters);
        options.put("prediction_feature", predictionFeature);
        options.put("folds", folds);
        options.put("stratify", stratify);
        options.put("seed", seed);
        options.put("creator", user.getId());
        options.put("subjectId", subjectId);

        Map<String, String> transformationAlgorithms = new LinkedHashMap<>();
        if (transformations != null && !transformations.isEmpty()) {
            transformationAlgorithms.put(uriInfo.getBaseUri().toString() + "algorithm/pmml",
                    "{\"transformations\" : \"" + transformations + "\"}");
        }
        if (scaling != null && !scaling.isEmpty()) {
            transformationAlgorithms.put(scaling, "");
        }
        if (!transformationAlgorithms.isEmpty()) {
            String transformationAlgorithmsString = serializer.write(transformationAlgorithms);
            LOG.log(Level.INFO, "Transformations:{0}", transformationAlgorithmsString);
            options.put("transformations", transformationAlgorithmsString);
        }


        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(crossValidationQueue, options);

        return Response.ok(task).build();
    }

    @POST
    @Path("/training_test_split")
    @ApiOperation(value = "Creates Validation Report",
            notes = "Creates Validation Report",
            response = Task.class
    )
    @org.jaqpot.core.service.annotations.Task
    public Response splitValidateAlgorithm(
            @FormParam("algorithm_uri") String algorithmURI,
            @FormParam("training_dataset_uri") String datasetURI,
            @FormParam("algorithm_params") String algorithmParameters,
            @FormParam("prediction_feature") String predictionFeature,
            @ApiParam(name = "transformations", defaultValue = DEFAULT_TRANSFORMATIONS) @FormParam("transformations") String transformations,
            @ApiParam(name = "scaling", defaultValue = STANDARIZATION) @FormParam("scaling") String scaling, //, allowableValues = SCALING + "," + STANDARIZATION          
            @ApiParam(name = "split_ratio",required = true) @FormParam("split_ratio") Double splitRatio,
            @FormParam("stratify") String stratify,
            @FormParam("seed") Integer seed,
            @HeaderParam("subjectId") String subjectId
    ) throws QuotaExceededException, JMSException, ParameterInvalidURIException, ParameterIsNullException {
        if (algorithmURI==null)
            throw new ParameterIsNullException("algorithmURI");
        if (datasetURI==null)
            throw new ParameterIsNullException("datasetURI");
        if (splitRatio==null)
            throw new ParameterIsNullException("splitRatio");

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long reportCount = reportHandler.countAllOfCreator(user.getId());
        int maxAllowedReports = new UserFacade(user).getMaxReports();

        if (reportCount > maxAllowedReports) {
            LOG.info(String.format("User %s has %d reports while maximum is %d",
                    user.getId(), reportCount, maxAllowedReports));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + reportCount + " reports. "
                    + "No more than " + maxAllowedReports + " are allowed with your subscription.");
        }

        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(algorithmURI)) {
            throw new ParameterInvalidURIException("Not valid algorithm URI.");
        }
        if (!urlValidator.isValid(datasetURI)) {
            throw new ParameterInvalidURIException("Not valid dataset URI.");
        }
        if (!urlValidator.isValid(predictionFeature)) {
            throw new ParameterInvalidURIException("Not valid prediction feature URI.");
        }
        if (transformations != null && !transformations.isEmpty() && !urlValidator.isValid(transformations)) {
            throw new ParameterInvalidURIException("Not valid transformation URI.");
        }
        if (scaling != null && !scaling.isEmpty() && !urlValidator.isValid(scaling)) {
            throw new ParameterInvalidURIException("Not valid scaling URI.");
        }
        if ((stratify != null && !stratify.isEmpty() && !stratify.equals("random") && !stratify.equals("normal"))) {
            throw new BadRequestException("Not valid stratify option - choose between random and normal");
        }

        Task task = new Task(new ROG(true).nextString(12));
        task.setMeta(
                MetaInfoBuilder.builder()
                .setCurrentDate()
                .addTitles("Validation on algorithm: " + algorithmURI)
                .addComments("Validation task created")
                .addDescriptions("Validation task using algorithm " + algorithmURI + " and dataset " + datasetURI)
                .addCreators(securityContext.getUserPrincipal().getName())
                .build());
        task.setType(Task.Type.VALIDATION);
        task.setHttpStatus(202);
        task.setStatus(Task.Status.QUEUED);
        task.setVisible(Boolean.TRUE);
        Map<String, Object> options = new HashMap<>();
        options.put("taskId", task.getId());
        options.put("algorithm_uri", algorithmURI);
        options.put("dataset_uri", datasetURI);
        options.put("algorithm_params", algorithmParameters);
        options.put("prediction_feature", predictionFeature);
        options.put("scaling", scaling);
        options.put("split_ratio", splitRatio);
        options.put("stratify", stratify);
        options.put("seed", seed);
        options.put("type", "SPLIT");
        options.put("subjectId", subjectId);

        Map<String, String> transformationAlgorithms = new LinkedHashMap<>();
        if (transformations != null && !transformations.isEmpty()) {
            transformationAlgorithms.put(uriInfo.getBaseUri().toString() + "algorithm/pmml",
                    "{\"transformations\" : \"" + transformations + "\"}");
        }
        if (scaling != null && !scaling.isEmpty()) {
            transformationAlgorithms.put(scaling, "");
        }
        if (!transformationAlgorithms.isEmpty()) {
            String transformationAlgorithmsString = serializer.write(transformationAlgorithms);
            LOG.log(Level.INFO, "Transformations:{0}", transformationAlgorithmsString);
            options.put("transformations", transformationAlgorithmsString);
        }

        taskHandler.create(task);
        System.out.println(splitValidationQueue.getTopicName());
        jmsContext.createProducer().setDeliveryDelay(1000).send(splitValidationQueue, options);
        return Response.ok(task).build();
    }

    @POST
    @Path("/test_set_validation")
    @ApiOperation(value = "Creates Validation Report",
            notes = "Creates Validation Report",
            response = Task.class
    )
    @org.jaqpot.core.service.annotations.Task
    public Response externalValidateAlgorithm(
            @FormParam("model_uri") String modelURI,
            @FormParam("test_dataset_uri") String datasetURI,
            @HeaderParam("subjectId") String subjectId
    ) throws QuotaExceededException, ParameterIsNullException, ParameterInvalidURIException {
        if (modelURI==null)
            throw new ParameterIsNullException("modelURI");
        if (datasetURI==null)
            throw new ParameterIsNullException("datasetURI");

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long reportCount = reportHandler.countAllOfCreator(user.getId());
        int maxAllowedReports = new UserFacade(user).getMaxReports();

        if (reportCount > maxAllowedReports) {
            LOG.info(String.format("User %s has %d algorithms while maximum is %d",
                    user.getId(), reportCount, maxAllowedReports));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + reportCount + " reports. "
                    + "No more than " + maxAllowedReports + " are allowed with your subscription.");
        }

        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(modelURI)) {
            throw new ParameterInvalidURIException("Not valid model URI.");
        }
        if (!urlValidator.isValid(datasetURI)) {
            throw new ParameterInvalidURIException("Not valid dataset URI.");
        }

        Task task = new Task(new ROG(true).nextString(12));
        task.setMeta(
                MetaInfoBuilder.builder()
                        .setCurrentDate()
                        .addTitles("Validation on model: " + modelURI)
                        .addComments("Validation task created")
                        .addDescriptions("Validation task using model " + modelURI + " and dataset " + datasetURI)
                        .addCreators(securityContext.getUserPrincipal().getName())
                        .build());
        task.setType(Task.Type.VALIDATION);
        task.setHttpStatus(202);
        task.setStatus(Task.Status.QUEUED);
        task.setVisible(Boolean.TRUE);
        Map<String, Object> options = new HashMap<>();
        options.put("taskId", task.getId());
        options.put("model_uri", modelURI);
        options.put("subjectId",subjectId);
        options.put("dataset_uri", datasetURI);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("type", "EXTERNAL");
        options.put("subjectId", subjectId);
        options.put("creator", user.getId());


        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(externalValidationQueue, options);

        return Response.ok(task).build();
    }

}