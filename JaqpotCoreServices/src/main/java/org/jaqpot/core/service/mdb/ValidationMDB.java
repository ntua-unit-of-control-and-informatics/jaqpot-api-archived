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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import org.jaqpot.core.data.ReportHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Report;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.ValidationType;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.client.ClientFactory;
import org.jaqpot.core.service.data.ValidationService;
import org.jaqpot.core.service.exceptions.JaqpotWebException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/validation"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class ValidationMDB extends RunningTaskMDB {

    private static final Logger LOG = Logger.getLogger(ValidationMDB.class.getName());

    @EJB
    TaskHandler taskHandler;

    @EJB
    ValidationService validationService;

    @EJB
    ReportHandler reportHandler;

    @Inject
    @UnSecure
    Client client;

    private final Set<String> intermediateResources = new HashSet<>();

    private String subjectId;

    @Override
    public void onMessage(Message msg) {
        final Task task;
        Map<String, Object> messageBody = null;
        try {
            messageBody = msg.getBody(Map.class);
        } catch (JMSException ex) {
            Logger.getLogger(ValidationMDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        task = taskHandler.find(messageBody.get("taskId"));
        try {

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
            task.setType(Task.Type.VALIDATION);
            task.getMeta().getComments().add("Validation Task is now running.");
            task.setPercentageCompleted(10.f);
            taskHandler.edit(task);

            String type = (String) messageBody.get("type");
            subjectId = (String) messageBody.get("subjectId");
            String modelURI = (String) messageBody.get("model_uri");
            String algorithmURI = (String) messageBody.get("algorithm_uri");
            String datasetURI = (String) messageBody.get("dataset_uri");
            String predictionFeature = (String) messageBody.get("prediction_feature");
            String algorithmParams = (String) messageBody.get("algorithm_params");
            String transformations = (String) messageBody.get("transformations");
            String scaling = (String) messageBody.get("scaling");
            String baseUri = (String) messageBody.get("base_uri");

            ValidationType validationType;
            if (algorithmURI != null && !algorithmURI.isEmpty()) {
                Algorithm algorithm = client.target(algorithmURI)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("subjectId", subjectId)
                        .get(Algorithm.class);

                if (algorithm.getOntologicalClasses().contains("ot:Regression")) {
                    validationType = ValidationType.REGRESSION;
                } else if (algorithm.getOntologicalClasses().contains("ot:Classification")) {
                    validationType = ValidationType.CLASSIFICATION;
                } else {
                    throw new IllegalArgumentException("Selected Algorithm is neither Regression nor Classification.");
                }
            } else if (modelURI != null && !modelURI.isEmpty()) {
                Model model = client.target(modelURI)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("subjectId", subjectId)
                        .get(Model.class);
                Algorithm algorithm = client.target(baseUri + "algorithm/" + model.getAlgorithm().getId())
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("subjectId", subjectId)
                        .get(Algorithm.class);
                if (algorithm.getOntologicalClasses().contains("ot:Regression")) {
                    validationType = ValidationType.REGRESSION;
                } else if (algorithm.getOntologicalClasses().contains("ot:Classification")) {
                    validationType = ValidationType.CLASSIFICATION;
                } else {
                    throw new IllegalArgumentException("Selected Algorithm is neither Regression nor Classification.");
                }
            } else {
                throw new IllegalArgumentException("Bad Algorithm provided.");
            }

            Report report = null;

            if (type.equals("SPLIT")) {

                task.getMeta().getComments().add("Validation mode is SPLIT.");
                taskHandler.edit(task);

                Double splitRatio = (Double) messageBody.get("split_ratio");
                Dataset dataset = client.target(datasetURI)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("subjectId", subjectId)
                        .get(Dataset.class);
                Integer rows = dataset.getTotalRows();

                Long split = Math.round(rows * splitRatio);
                String trainDatasetURI = datasetURI + "?rowStart=0&rowMax=" + split;
                String testDatasetURI = datasetURI + "?rowStart=" + split + "&rowMax=" + (rows - split);

                task.getMeta().getComments().add("Starting train and test with train_dataset:" + trainDatasetURI + " test_dataset:" + testDatasetURI);
                taskHandler.edit(task);
                Object[] results = validationService.trainAndTest(algorithmURI, trainDatasetURI, testDatasetURI, predictionFeature, algorithmParams, transformations, scaling, subjectId);
                task.getMeta().getComments().add("Finished train and test with train_dataset:" + trainDatasetURI + " test_dataset:" + testDatasetURI);
                taskHandler.edit(task);

                String finalDatasetURI = (String) results[0];
                String predictedFeature = (String) results[2];
                Integer indepFeatureSize = (Integer) results[3];

                intermediateResources.add((String) results[0]);
                intermediateResources.add((String) results[4]);

                task.getMeta().getComments().add("Final dataset is:" + finalDatasetURI);
                taskHandler.edit(task);

                TrainingRequest reportRequest = new TrainingRequest();
                Dataset finalDS = client.target(finalDatasetURI)
                        .request()
                        .header("subjectid", subjectId)
                        .accept(MediaType.APPLICATION_JSON)
                        .get(Dataset.class);
                reportRequest.setDataset(finalDS);
                reportRequest.setPredictionFeature(predictionFeature);
                Map<String, Object> validationParameters = new HashMap<>();
                validationParameters.put("predictionFeature", predictionFeature);
                validationParameters.put("predictedFeature", predictedFeature);
                validationParameters.put("variables", indepFeatureSize);
                validationParameters.put("type", validationType);
                reportRequest.setParameters(validationParameters);

                try {
                    System.out.println(new ObjectMapper().writeValueAsString(reportRequest));
                } catch (JsonProcessingException ex) {
                    throw new UnsupportedOperationException(ex);
                }

                report = client.target(ResourceBundle.getBundle("config").getString("ValidationBasePath"))
                        .request()
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.json(reportRequest), Report.class);

            } else if (type.equals("CROSS")) {

                task.getMeta().getComments().add("Validation mode is CROSS.");
                taskHandler.edit(task);

                Integer folds = (Integer) messageBody.get("folds");
                String stratify = (String) messageBody.get("stratify");
                Integer seed = (Integer) messageBody.get("seed");
                Dataset dataset = client.target(datasetURI)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("subjectId", subjectId)
                        .get(Dataset.class);
                Integer rows = dataset.getTotalRows();
                List<String> partialDatasets = new ArrayList<>();

                Integer minRows = rows / folds;
                Integer extras = rows % folds;

                Integer i = 0, j = 0;
                while (i < rows) {
                    Integer rowStart;
                    Integer rowMax;
                    if (j < extras) {
                        rowStart = i;
                        rowMax = minRows + 1;
                        i += rowMax;
                        j++;
                    } else {
                        rowStart = i;
                        rowMax = minRows;
                        i += rowMax;
                    }
                    String partialDatasetURI = datasetURI + "?rowStart=" + rowStart + "&rowMax=" + rowMax + (stratify != null ? "&stratify=" + stratify : "") + (folds != null ? "&folds=" + folds.toString() : "") + (seed != null ? "&seed=" + seed.toString() : "") + "&target_feature=" + URLEncoder.encode(predictionFeature, "UTF-8");
                    partialDatasets.add(partialDatasetURI);
                }

                List<String> finalDatasets = new ArrayList<>();

                Map<String, Object[]> resultMap = new HashMap<>();
                partialDatasets.parallelStream().forEach(testDataset -> {
                    try {
                        String trainDatasets = partialDatasets.stream()
                                .filter(d -> !d.equals(testDataset))
                                .collect(Collectors.joining(","));
                        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
                        params.add("dataset_uris", trainDatasets);
                        Client c = new ClientFactory().getUnsecureRestClient();
                        String trainDataset = c.target(datasetURI.split("dataset")[0] + "dataset/merge")
                                .request()
                                .accept("text/uri-list")
                                .header("subjectId", subjectId)
                                .post(Entity.form(params), String.class);
                        c.close();

                        task.getMeta().getComments().add("Starting train and test with train_dataset:" + trainDataset + " test_dataset:" + testDataset);
                        taskHandler.edit(task);
                        Object[] crossResults = validationService.trainAndTest(algorithmURI, trainDataset, testDataset, predictionFeature, algorithmParams, transformations, scaling, subjectId);
                        task.getMeta().getComments().add("Finished train and test with train_dataset:" + trainDataset + " test_dataset:" + testDataset);
                        taskHandler.edit(task);
                        
                        String finalSubDataset = (String) crossResults[0];
                        resultMap.put(finalSubDataset, crossResults);
                        finalDatasets.add(finalSubDataset);

                        intermediateResources.add(trainDataset);
                        intermediateResources.add((String) crossResults[0]);
                        intermediateResources.add((String) crossResults[4]);
                    } catch (JaqpotWebException ex) {
                        Logger.getLogger(ValidationMDB.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });

                MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
                params.add("dataset_uris", finalDatasets.stream().collect(Collectors.joining(",")));
                String finalDataset = client.target(datasetURI.split("dataset")[0] + "dataset/merge")
                        .request()
                        .accept("text/uri-list")
                        .header("subjectId", subjectId)
                        .post(Entity.form(params), String.class);

                task.getMeta().getComments().add("Final dataset is:" + finalDataset);
                taskHandler.edit(task);
                intermediateResources.add(finalDataset);

                Object[] firstResult = resultMap.values().stream().findFirst().get();
                String predictedFeature = (String) firstResult[2];
                Integer indepFeatureSize = (Integer) firstResult[3];

                TrainingRequest reportRequest = new TrainingRequest();
                Dataset finalDS = client.target(finalDataset)
                        .request()
                        .header("subjectid", subjectId)
                        .accept(MediaType.APPLICATION_JSON)
                        .get(Dataset.class);
                reportRequest.setDataset(finalDS);
                reportRequest.setPredictionFeature(predictionFeature);
                Map<String, Object> validationParameters = new HashMap<>();
                validationParameters.put("predictionFeature", predictionFeature);
                validationParameters.put("predictedFeature", predictedFeature);
                validationParameters.put("variables", indepFeatureSize);
                validationParameters.put("type", validationType);
                reportRequest.setParameters(validationParameters);

//                try {
//                    System.out.println(new ObjectMapper().writeValueAsString(reportRequest));
//                } catch (JsonProcessingException ex) {
//                    throw new UnsupportedOperationException(ex);
//                }
                report = client.target("http://147.102.82.32:8092/pws/validation")
                        .request()
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.json(reportRequest), Report.class);

            } else if (type.equals("EXTERNAL")) {
                task.getMeta().getComments().add("Validation mode is EXTERNAL.");
                taskHandler.edit(task);
                MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
                params.add("dataset_uri", datasetURI);
                Task predictionTask = client.target(modelURI)
                        .request()
                        .header("subjectid", subjectId)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.form(params), Task.class);
                String predictionTaskURI = modelURI.split("model")[0] + "task/" + predictionTask.getId();
                while (predictionTask.getStatus().equals(Task.Status.RUNNING)
                        || predictionTask.getStatus().equals(Task.Status.QUEUED)) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {

                    }
                    predictionTask = client.target(predictionTaskURI)
                            .request()
                            .header("subjectid", subjectId)
                            .accept(MediaType.APPLICATION_JSON)
                            .get(Task.class);
                }
                if (!predictionTask.getStatus().equals(Task.Status.COMPLETED)) {
                    throw new JaqpotWebException(predictionTask.getErrorReport());
                }
                Model model = client.target(modelURI)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("subjectId", subjectId)
                        .get(Model.class);

                TrainingRequest reportRequest = new TrainingRequest();
                Dataset finalDS = client.target(predictionTask.getResultUri())
                        .request()
                        .header("subjectid", subjectId)
                        .accept(MediaType.APPLICATION_JSON)
                        .get(Dataset.class);
                intermediateResources.add(predictionTask.getResultUri());
                reportRequest.setDataset(finalDS);
                reportRequest.setPredictionFeature(model.getDependentFeatures().get(0));
                Map<String, Object> validationParameters = new HashMap<>();
                validationParameters.put("predictionFeature", model.getDependentFeatures().get(0));
                validationParameters.put("predictedFeature", model.getPredictedFeatures().get(0));
                validationParameters.put("variables", model.getIndependentFeatures().size());
                validationParameters.put("type", validationType);
                reportRequest.setParameters(validationParameters);

                report = client.target("http://147.102.82.32:8092/pws/validation")
                        .request()
                        .header("Content-Type", MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.json(reportRequest), Report.class);

            } else {
                throw new UnsupportedOperationException("Operation " + type + " not supported");
            }

            ROG randomStringGenerator = new ROG(true);
            String reportId = randomStringGenerator.nextString(15);
            report.setId(reportId);
            report.setMeta(MetaInfoBuilder
                    .builder()
                    // .addTitles((String) messageBody.get("title"))
                    .addCreators(task.getMeta().getCreators())
                    .addSources(datasetURI, algorithmURI, modelURI)
                    .addComments("Created by task " + task.getId())
                    //                    .addDescriptions((String) messageBody.get("description"))
                    .build());
            report.setVisible(Boolean.TRUE);
            reportHandler.create(report);
            task.setResult("report/" + report.getId());
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
        } catch (NullPointerException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), "")); // rest
        } catch (JaqpotWebException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ex.getError());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ValidationMDB.class.getName()).log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError("", ex.getMessage(), ""));
        } finally {
            task.getMeta().getComments().add("Performing cleanup.");
            for (String intermediateResource : intermediateResources) {
                client.target(intermediateResource)
                        .request()
                        .header("subjectid", subjectId)
                        .delete()
                        .close();
            }
            if (task != null) {
                terminate(task.getId());
            }
            taskHandler.edit(task);
        }

    }

}
