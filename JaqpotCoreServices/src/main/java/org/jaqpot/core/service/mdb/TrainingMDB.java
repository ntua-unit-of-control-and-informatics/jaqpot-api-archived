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
package org.jaqpot.core.service.mdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.dto.jpdi.TrainingResponse;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.exceptions.JaqpotWebException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/training"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class TrainingMDB extends RunningTaskMDB {

    private static final Logger LOG = Logger.getLogger(TrainingMDB.class.getName());

    @EJB
    TaskHandler taskHandler;

    @EJB
    FeatureHandler featureHandler;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ModelHandler modelHandler;

    @Inject
    @UnSecure
    Client client;

    @Inject
    @Jackson
    JSONSerializer jsonSerializer;

    long DOA_TASK_MAX_WAITING_TIME = 45; // 45s

    @Override
    public void onMessage(Message msg) {
        Task task = new Task();
        ROG randomStringGenerator = new ROG(true);
        String modelId = randomStringGenerator.nextString(12);

        try {
            Map<String, Object> messageBody = msg.getBody(Map.class);
            task = taskHandler.find(messageBody.get("taskId"));

            if (task == null) {
                throw new NullPointerException("FATAL: Could not find task with id:" + messageBody.get("taskId"));
            }
            if (task.getMeta() == null) {
                task.setMeta(MetaInfoBuilder.builder().setCurrentDate().build());
            }
            if (task.getMeta().getComments() == null) {
                task.getMeta().setComments(new ArrayList<>());
            }

            init(task.getId());

            task.setHttpStatus(202);
            task.setStatus(Task.Status.RUNNING);
            task.setType(Task.Type.TRAINING);
            task.getMeta().getComments().add("Training Task is now running.");
            task.setPercentageCompleted(10.f);
            taskHandler.edit(task);

            String dataset_uri = (String) messageBody.get("dataset_uri");
            List<String> transformationModels = new ArrayList<>();
            List<String> linkedModels = new ArrayList<>();

            if (messageBody.containsKey("transformations")) {
                task.getMeta().getComments().add("--");
                task.getMeta().getComments().add("Processing transformations...");
                taskHandler.edit(task);

                String transformationsString = (String) messageBody.get("transformations");
                LinkedHashMap<String, String> transformations = jsonSerializer.parse(transformationsString, LinkedHashMap.class);
                List<Algorithm> transformationAlgorithms = new ArrayList<>();
                List<Algorithm> linkedAlgorithms = new ArrayList<>();

                for (String transformationAlgorithmURI : transformations.keySet()) {
                    Algorithm algorithm = client.target(transformationAlgorithmURI)
                            .request()
                            .header("subjectid", messageBody.get("subjectid"))
                            .accept(MediaType.APPLICATION_JSON)
                            .get(Algorithm.class);
                    algorithm.setId(transformationAlgorithmURI);
                    if (algorithm.getOntologicalClasses().contains("ot:Transformation")) {
                        transformationAlgorithms.add(algorithm);
                    } else {
                        linkedAlgorithms.add(algorithm);
                    }
                }

                for (Algorithm algorithm : transformationAlgorithms) {
                    task.getMeta().getComments().add("-");
                    task.getMeta().getComments().add("Starting training on transformation algorithm:" + algorithm.getId());
                    taskHandler.edit(task);

                    MultivaluedMap<String, String> formMap = new MultivaluedHashMap<>();
                    messageBody.entrySet().stream()
                            .filter(e -> !e.getKey().equals("visible"))
                            .filter(e -> e.getValue() != null)
                            .forEach(entry -> {
                                formMap.put(entry.getKey(), Arrays.asList(entry.getValue().toString()));
                            });
                    formMap.remove("transformations");
                    formMap.put("parameters", Arrays.asList(transformations.get(algorithm.getId())));
                    formMap.put("dataset_uri", Arrays.asList(dataset_uri));
                    Task trainTask = client.target(algorithm.getId())
                            .request()
                            .header("subjectid", messageBody.get("subjectid"))
                            .accept(MediaType.APPLICATION_JSON)
                            .post(Entity.form(formMap), Task.class);
                    String trainTaskURI = algorithm.getId().split("algorithm")[0] + "task/" + trainTask.getId();
                    task.getMeta().getComments().add("Training task created:" + trainTaskURI);
                    taskHandler.edit(task);
                    while (trainTask.getStatus().equals(Task.Status.RUNNING)
                            || trainTask.getStatus().equals(Task.Status.QUEUED)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {

                        }
                        trainTask = client.target(trainTaskURI)
                                .request()
                                .header("subjectid", messageBody.get("subjectid"))
                                .accept(MediaType.APPLICATION_JSON)
                                .get(Task.class);
                    }
                    if (trainTask.getStatus().equals(Task.Status.COMPLETED)) {
                        transformationModels.add(trainTask.getResultUri());
                        task.getMeta().getComments().add("Transformation model created successfully:" + trainTask.getResultUri());
                        taskHandler.edit(task);
                    } else {
                        task.getMeta().getComments().add("Transformation task failed.");
                        throw new JaqpotWebException(trainTask.getErrorReport());
                    }
                    formMap.clear();
                    formMap.put("dataset_uri", Arrays.asList(dataset_uri));
                    Task predictionTask = client.target(trainTask.getResultUri())
                            .request()
                            .header("subjectid", messageBody.get("subjectid"))
                            .accept(MediaType.APPLICATION_JSON)
                            .post(Entity.form(formMap), Task.class);
                    String predictionTaskURI = trainTask.getResultUri().split("model")[0] + "task/" + predictionTask.getId();
                    task.getMeta().getComments().add("Prediction task created:" + predictionTaskURI);
                    taskHandler.edit(task);
                    while (predictionTask.getStatus().equals(Task.Status.RUNNING)
                            || predictionTask.getStatus().equals(Task.Status.QUEUED)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {

                        }
                        predictionTask = client.target(predictionTaskURI)
                                .request()
                                .header("subjectid", messageBody.get("subjectid"))
                                .accept(MediaType.APPLICATION_JSON)
                                .get(Task.class);
                    }
                    if (predictionTask.getStatus().equals(Task.Status.COMPLETED)) {
                        dataset_uri = predictionTask.getResultUri();
                        task.getMeta().getComments().add("Transformed dataset created successfully:" + predictionTask.getResultUri());
                        taskHandler.edit(task);
                    } else {
                        task.getMeta().getComments().add("Transformation task failed.");
                        throw new JaqpotWebException(predictionTask.getErrorReport());
                    }

                }

                for (Algorithm algorithm : linkedAlgorithms) {
                    task.getMeta().getComments().add("-");
                    task.getMeta().getComments().add("Starting training on linked algorithm:" + algorithm.getId());
                    taskHandler.edit(task);

                    MultivaluedMap<String, String> formMap = new MultivaluedHashMap<>();
                    messageBody.entrySet().stream()
                            .filter(e -> !e.getKey().equals("visible"))
                            .filter(e -> e.getValue() != null)
                            .forEach(entry -> {
                                formMap.put(entry.getKey(), Arrays.asList(entry.getValue().toString()));
                            });
                    formMap.remove("transformations");
                    formMap.put("parameters", Arrays.asList(transformations.get(algorithm.getId())));
                    formMap.put("dataset_uri", Arrays.asList(dataset_uri));
                    Task trainTask = client.target(algorithm.getId())
                            .request()
                            .header("subjectid", messageBody.get("subjectid"))
                            .accept(MediaType.APPLICATION_JSON)
                            .post(Entity.form(formMap), Task.class);
                    String trainTaskURI = algorithm.getId().split("algorithm")[0] + "task/" + trainTask.getId();
                    task.getMeta().getComments().add("Training task created:" + trainTaskURI);
                    taskHandler.edit(task);
                    while (trainTask.getStatus().equals(Task.Status.RUNNING)
                            || trainTask.getStatus().equals(Task.Status.QUEUED)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {

                        }
                        trainTask = client.target(trainTaskURI)
                                .request()
                                .header("subjectid", messageBody.get("subjectid"))
                                .accept(MediaType.APPLICATION_JSON)
                                .get(Task.class);
                    }
                    if (trainTask.getStatus().equals(Task.Status.COMPLETED)) {
                        linkedModels.add(trainTask.getResultUri());
                        task.getMeta().getComments().add("Linked model created successfully:" + trainTask.getResultUri());
                        taskHandler.edit(task);
                    } else {
                        task.getMeta().getComments().add("Linked Training task failed.");
                        throw new JaqpotWebException(trainTask.getErrorReport());
                    }
                }
            }
            task.getMeta().getComments().add("--");
            Dataset dataset = null;
            if (dataset_uri != null && !dataset_uri.isEmpty()) {
                task.getMeta().getComments().add("Training dataset URI is:" + dataset_uri);
                task.getMeta().getComments().add("Attempting to download dataset...");
                task.setPercentageCompleted(12.f);
                taskHandler.edit(task);
                dataset = client.target(dataset_uri)
                        .request()
                        .header("subjectid", messageBody.get("subjectid"))
                        .accept(MediaType.APPLICATION_JSON)
                        .get(Dataset.class);
                dataset.setDatasetURI(dataset_uri);

                task.getMeta().getComments().add("Dataset has been retrieved.");
            }
            task.getMeta().getComments().add("Creating JPDI training request...");
            task.setPercentageCompleted(20.f);
            taskHandler.edit(task);

            TrainingRequest trainingRequest = new TrainingRequest();
            trainingRequest.setDataset(dataset);
            task.getMeta().getComments().add("Inserted dataset.");
            task.setPercentageCompleted(34.f);
            taskHandler.edit(task);

            String predictionFeature = (String) messageBody.get("prediction_feature");
            if (predictionFeature != null && !predictionFeature.isEmpty()) {
                trainingRequest.setPredictionFeature(predictionFeature);
                task.getMeta().getComments().add("Inserted prediction feature.");
            } else {
                task.getMeta().getComments().add("Prediction feature not present.");
            }

            task.setPercentageCompleted(41.f);
            taskHandler.edit(task);

            String parameters = (String) messageBody.get("parameters");
            if (parameters != null && !parameters.isEmpty()) {
                HashMap<String, Object> parameterMap = jsonSerializer.parse(parameters, new HashMap<String, Object>().getClass());
                trainingRequest.setParameters(parameterMap);
            }

            task.getMeta().getComments().add("Inserted parameters.");
            task.setPercentageCompleted(53.f);
            taskHandler.edit(task);

            String algorithmId = (String) messageBody.get("algorithmId");
            Algorithm algorithm = algorithmHandler.find(algorithmId);

            task.getMeta().getComments().add("Inserted algorithm id.");
            task.setPercentageCompleted(55.f);
            taskHandler.edit(task);

            task.getMeta().getComments().add("Sending request to  algorithm service:" + algorithm.getTrainingService());
            task.setPercentageCompleted(64.f);
            taskHandler.edit(task);
            Response response = client.target(algorithm.getTrainingService())
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(trainingRequest));
            task.getMeta().getComments().add("Algorithm service responded with status:" + response.getStatus());
            task.setPercentageCompleted(69.f);
            taskHandler.edit(task);

            String responseString = response.readEntity(String.class);
            if (response.getStatus() != 200 && response.getStatus() != 201 && response.getStatus() != 202) {
                if (response.getStatus() == 400) {
                    throw new JaqpotWebException(ErrorReportFactory.badRequest(responseString, responseString));
                } else {
                    throw new JaqpotWebException(ErrorReportFactory.internalServerError("500", responseString, responseString));
                }
            }
//            System.out.println(responseString);

            task.getMeta().getComments().add("Attempting to parse response...");
            task.setPercentageCompleted(71.f);
            taskHandler.edit(task);
            TrainingResponse trainingResponse = jsonSerializer.parse(responseString, TrainingResponse.class);
            task.getMeta().getComments().add("Response was parsed successfully");
            task.setPercentageCompleted(77.f);
            taskHandler.edit(task);

            response.close();

            task.getMeta().getComments().add("Building model...");
            task.setPercentageCompleted(84.f);
            taskHandler.edit(task);

            Model model = new Model(modelId);
            model.setActualModel(trainingResponse.getRawModel());
            model.setPmmlModel(trainingResponse.getPmmlModel());
            model.setAlgorithm(algorithm);
            model.setIndependentFeatures(trainingResponse.getIndependentFeatures());
            model.setDatasetUri((String) messageBody.get("dataset_uri"));
            model.setAdditionalInfo(trainingResponse.getAdditionalInfo());
            model.setParameters(trainingRequest.getParameters());
            ArrayList<String> dependentFeatures = new ArrayList<>();
            if (predictionFeature != null) {
                dependentFeatures.add(predictionFeature);
            }
            model.setDependentFeatures(dependentFeatures);
            model.setTransformationModels(transformationModels);
            model.setLinkedModels(linkedModels);
            task.getMeta().getComments().add("Defining the prediction features");
            ArrayList<String> predictedFeatures = new ArrayList<>();
            for (String featureTitle : trainingResponse.getPredictedFeatures()) {
                Feature predictionFeatureResource = featureHandler.findByTitleAndSource(featureTitle, "algorithm/" + algorithmId);
                if (predictionFeatureResource == null) {
                    // Create the prediction features (POST /feature)
                    String predFeatID = randomStringGenerator.nextString(12);
                    predictionFeatureResource = new Feature();
                    predictionFeatureResource.setId(predFeatID);
                    predictionFeatureResource
                            .setPredictorFor(dependentFeatures.stream().findFirst().orElse(null));
                    predictionFeatureResource.setCreatedBy(task.getCreatedBy());
                    predictionFeatureResource.setMeta(MetaInfoBuilder
                            .builder()
                            .addSources(/*messageBody.get("base_uri") + */"algorithm/" + algorithmId)
                            .addComments("Feature created to hold predictions by algorithm with ID " + algorithmId)
                            .addTitles(featureTitle)
                            .addSeeAlso(dependentFeatures.toArray(new String[dependentFeatures.size()]))
                            .build());
                    /* Create feature */
                    featureHandler.create(predictionFeatureResource);
                    task.getMeta().getComments().add("Feature created with ID " + predFeatID);
                    taskHandler.edit(task);
                }
                predictedFeatures.add(messageBody.get("base_uri") + "feature/" + predictionFeatureResource.getId());
            }
            model.setPredictedFeatures(predictedFeatures);

            task.getMeta().getComments().add("Model was built successfully");

            task.setPercentageCompleted(85.f);
            taskHandler.edit(task);

            model.setMeta(MetaInfoBuilder
                    .builder()
                    .addTitles((String) messageBody.get("title"))
                    .addCreators(task.getCreatedBy())
                    .addSources(dataset != null ? dataset.getDatasetURI() : "")
                    .addComments("Created by task " + task.getId())
                    .addDescriptions((String) messageBody.get("description"))
                    .build());

            task.getMeta().getComments().add("Model was built successfully. Now saving to database...");
            taskHandler.edit(task);
            if ((Boolean) messageBody.get("visible")) {
                model.setVisible(Boolean.TRUE);
            } else {
                model.setVisible(Boolean.FALSE);
            }
            modelHandler.create(model);

            task.setResult("model/" + model.getId());
            task.setHttpStatus(201);
            task.setPercentageCompleted(100.f);
            task.setDuration(System.currentTimeMillis() - task.getMeta().getDate().getTime()); // in ms
            task.setStatus(Task.Status.COMPLETED);
            task.getMeta().getComments().add("Task Completed Successfully.");
            taskHandler.edit(task);

        } catch (UnsupportedOperationException | IllegalStateException | IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.badRequest(ex.getMessage(), "")); // Operation not supported
        } catch (ResponseProcessingException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.remoteError("", null, ex)); //  Process response failed
        } catch (ProcessingException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.remoteError("", null, ex)); // Process response runtime error
        } catch (WebApplicationException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), "")); // Application runtime error
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), ""));
        } catch (NullPointerException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), "")); // rest
        } catch (JaqpotWebException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ex.getError());
        } finally {
            if (task != null) {
                terminate(task.getId());
            }
            taskHandler.edit(task);
        }
    }

}
