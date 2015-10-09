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
package org.jaqpot.core.service.mdb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.transaction.Transactional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.PredictionRequest;
import org.jaqpot.core.model.dto.jpdi.PredictionResponse;
import org.jaqpot.core.model.dto.study.Category;
import org.jaqpot.core.model.dto.study.Effect;
import org.jaqpot.core.model.dto.study.Owner;
import org.jaqpot.core.model.dto.study.Protocol;
import org.jaqpot.core.model.dto.study.Result;
import org.jaqpot.core.model.dto.study.Studies;
import org.jaqpot.core.model.dto.study.Study;
import org.jaqpot.core.model.dto.study.Substance;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.exceptions.JaqpotWebException;

/**
 *
 * @author hampos
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/prediction"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class PredictionMDB extends RunningTaskMDB {

    private static final Logger LOG = Logger.getLogger(PredictionMDB.class.getName());

    @EJB
    TaskHandler taskHandler;

    @EJB
    ModelHandler modelHandler;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    FeatureHandler featureHandler;

    @Inject
    @UnSecure
    Client client;

    @Inject
    @Jackson
    JSONSerializer jsonSerializer;

    @Override
    public void onMessage(Message msg) {
        Task task = null;
        try {
            Map<String, Object> messageBody = msg.getBody(Map.class);
            task = taskHandler.find(messageBody.get("taskId"));

            if (task == null) {
                throw new NullPointerException("FATAL: Could not find task with id:" + messageBody.get("taskId"));
            }

            init(task.getId());

            task.setHttpStatus(202);
            task.setStatus(Task.Status.RUNNING);
            task.setType(Task.Type.PREDICTION);
            task.getMeta().getComments().add("Prediction Task is now running.");
            task.setPercentageCompleted(1.0f);
            taskHandler.edit(task);

            task.getMeta().getComments().add("Attempting to find model in database...");
            task.setPercentageCompleted(1.5f);
            taskHandler.edit(task);
            Model model = modelHandler.find(messageBody.get("modelId"));
            if (model == null) {
                task.setStatus(Task.Status.ERROR);
                task.setErrorReport(ErrorReportFactory.notFoundError((String) messageBody.get("modelId")));
                taskHandler.edit(task);
                return;
            }
            task.getMeta().getComments().add("Model retrieved successfully.");
            task.setPercentageCompleted(5.f);
            taskHandler.edit(task);

            String dataset_uri = (String) messageBody.get("dataset_uri");
            if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
                task.getMeta().getComments().add("--");
                task.getMeta().getComments().add("Processing transformations...");
                taskHandler.edit(task);
                for (String transformationModel : model.getTransformationModels()) {
                    MultivaluedMap<String, String> formMap = new MultivaluedHashMap<>();
                    formMap.put("dataset_uri", Arrays.asList(dataset_uri));
                    Task predictionTask = client.target(transformationModel)
                            .request()
                            .header("subjectid", messageBody.get("subjectid"))
                            .accept(MediaType.APPLICATION_JSON)
                            .post(Entity.form(formMap), Task.class);
                    String predictionTaskURI = transformationModel.split("model")[0] + "task/" + predictionTask.getId();
                    task.getMeta().getComments().add("Transformation task created:" + predictionTaskURI);
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

                task.getMeta().getComments().add("--");
                taskHandler.edit(task);
            }

            List<String> predictionDatasets = new ArrayList<>();
            if (model.getLinkedModels() != null && !model.getLinkedModels().isEmpty()) {
                task.getMeta().getComments().add("--");
                task.getMeta().getComments().add("Processing linked models...");
                taskHandler.edit(task);
                final String transformedDataset = dataset_uri;
                for (String linkedModel : model.getLinkedModels()) {
                    MultivaluedMap<String, String> formMap = new MultivaluedHashMap<>();
                    formMap.put("dataset_uri", Arrays.asList(transformedDataset));
                    Task predictionTask = client.target(linkedModel)
                            .request()
                            .header("subjectid", messageBody.get("subjectid"))
                            .accept(MediaType.APPLICATION_JSON)
                            .post(Entity.form(formMap), Task.class);
                    String predictionTaskURI = linkedModel.split("model")[0] + "task/" + predictionTask.getId();
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
                        String predictionDataset = predictionTask.getResultUri();
                        predictionDatasets.add(predictionDataset);
                        task.getMeta().getComments().add("Prediction dataset created successfully:" + predictionTask.getResultUri());
                        taskHandler.edit(task);
                    } else {
                        task.getMeta().getComments().add("Prediction task failed.");
                    }
                }

                task.getMeta().getComments().add("--");
                taskHandler.edit(task);
            }

            task.getMeta().getComments().add("Attempting to download dataset...");
            taskHandler.edit(task);
            Dataset dataset = client.target(dataset_uri)
                    .request()
                    .header("subjectid", messageBody.get("subjectid"))
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);
            dataset.setDatasetURI(dataset_uri);
            task.getMeta().getComments().add("Dataset has been retrieved.");

            Dataset predFeatureDataset = DatasetFactory.copy(dataset, new HashSet<>(model.getDependentFeatures()));

            dataset.getDataEntry().parallelStream()
                    .forEach(dataEntry -> {
                        dataEntry.getValues().keySet().retainAll(model.getIndependentFeatures());
                    });
            task.getMeta().getComments().add("Dataset has been cleaned from unused values.");

            task.setPercentageCompleted(15.f);
            task.getMeta().getComments().add("Creating JPDI prediction request...");
            taskHandler.edit(task);
            PredictionRequest predictionRequest = new PredictionRequest();
            predictionRequest.setDataset(dataset);
            predictionRequest.setRawModel(model.getActualModel());
            predictionRequest.setAdditionalInfo(model.getAdditionalInfo());

            task.getMeta().getComments().add("Sending request to algorithm service:" + model.getAlgorithm().getPredictionService());
            task.setPercentageCompleted(17.f);
            taskHandler.edit(task);
            Response response = client.target(model.getAlgorithm().getPredictionService())
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(predictionRequest));
            task.getMeta().getComments().add("Algorithm service responded with status:" + response.getStatus());
            taskHandler.edit(task);

            String responseString = response.readEntity(String.class);
            if (response.getStatus() != 200 && response.getStatus() != 201 && response.getStatus() != 202) {
                if (response.getStatus() == 400) {
                    throw new JaqpotWebException(ErrorReportFactory.badRequest(responseString, responseString));
                } else {
                    throw new JaqpotWebException(ErrorReportFactory.internalServerError("500", responseString, responseString));
                }
            }
            System.out.println(responseString);

            task.getMeta().getComments().add("Attempting to parse response...");
            task.setPercentageCompleted(18.f);
            taskHandler.edit(task);
            PredictionResponse predictionResponse = jsonSerializer.parse(responseString, PredictionResponse.class);
            response.close();
            task.getMeta().getComments().add("Response was parsed successfully.");

            task.getMeta().getComments().add("Creating new Dataset for predictions...");
            task.setPercentageCompleted(22.f);
            taskHandler.edit(task);
            List<Map<String, Object>> predictions = predictionResponse.getPredictions();
            for (int i = 0; i < dataset.getDataEntry().size(); i++) {
                Map<String, Object> row = predictions.get(i);
                DataEntry dataEntry = dataset.getDataEntry().get(i);
                if (model.getAlgorithm().getOntologicalClasses().contains("ot:Scaling")
                        || model.getAlgorithm().getOntologicalClasses().contains("ot:Transformation")) {
                    dataEntry.getValues().clear();
                    dataset.getFeatures().clear();
                }
                for (Entry<String, Object> entry : row.entrySet()) {
//                row.entrySet().stream().forEach(entry -> {
                    Feature feature = featureHandler.findByTitleAndSource(entry.getKey(), "algorithm/" + model.getAlgorithm().getId());
                    dataEntry.getValues().put(messageBody.get("base_uri") + "feature/" + feature.getId(), entry.getValue());
                    dataset.getFeatures().add(new org.jaqpot.core.model.dto.dataset.FeatureInfo(messageBody.get("base_uri") + "feature/" + feature.getId(), feature.getMeta().getTitles().stream().findFirst().get()));
                }
            }
//            System.out.println(jsonSerializer.write(dataset));
            dataset = DatasetFactory.mergeColumns(dataset, predFeatureDataset);
            for (String predictionDatasetURI : predictionDatasets) {
                Dataset predictionDataset = client.target(predictionDatasetURI)
                        .request()
                        .header("subjectid", messageBody.get("subjectid"))
                        .accept(MediaType.APPLICATION_JSON)
                        .get(Dataset.class);
                dataset = DatasetFactory.mergeColumns(dataset, predictionDataset);
            }
            ROG randomStringGenerator = new ROG(true);
            dataset.setId(randomStringGenerator.nextString(14));
            task.getMeta().getComments().add("Dataset ready.");
            task.getMeta().getComments().add("Saving to database...");
            task.setPercentageCompleted(30.f);
            taskHandler.edit(task);
            datasetHandler.create(dataset);
            task.getMeta().getComments().add("Dataset saved...");
            taskHandler.edit(task);
//            System.out.println(jsonSerializer.write(dataset));
            if (messageBody.containsKey("bundle_uri") && messageBody.get("bundle_uri") != null) {
                task.getMeta().getComments().add("A bundle associated with these predictions was found.");
                task.getMeta().getComments().add("We will attempt to upload results into the bundle.");
                task.setPercentageCompleted(35.f);
                taskHandler.edit(task);

                String bundleUri = (String) messageBody.get("bundle_uri");

                List<String> substances = dataset.getDataEntry().stream().map(de -> {
                    return de.getCompound().getURI();
                }).collect(Collectors.toList());
                String predictionFeature = model.getPredictedFeatures().stream().findFirst().get();
                Feature feature = featureHandler.find(predictionFeature.split("/")[1]);
                String predictedProperty = feature.getMeta().getSeeAlso().stream().findFirst().get();
                String studyJSON = createStudyJSON(predictedProperty, model.getId(), predictions, substances);

                task.getMeta().getComments().add("Creating a working matrix in the bundle...");
                task.setPercentageCompleted(45.f);
                taskHandler.edit(task);
                MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
                formData.putSingle("deletematrix", "true");
                client.target(bundleUri + "/matrix/working")
                        .request()
                        .post(Entity.form(formData)).close();

                task.getMeta().getComments().add("Now putting results in the bundle...");
                task.setPercentageCompleted(50.f);
                taskHandler.edit(task);
                Response taskResponse = client.target(bundleUri + "/matrix")
                        .request()
                        .header("Content-type", MediaType.APPLICATION_JSON)
                        .accept("text/uri-list")
                        .put(Entity.entity(studyJSON, MediaType.APPLICATION_JSON));

                String taskUri = taskResponse.readEntity(String.class);
                taskResponse.close();

                task.getMeta().getComments().add("An upload task has been started with URI:" + taskUri.trim());
                task.setPercentageCompleted(85.f);
                taskHandler.edit(task);
            }

            task.setStatus(Task.Status.COMPLETED);
            task.setPercentageCompleted(100.f);
            task.setHttpStatus(201);
            task.setResult("dataset/" + dataset.getId());
            task.getMeta().getComments().add("Task Completed Successfully.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setHttpStatus(500);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), ""));
        } catch (JaqpotWebException ex) {
            LOG.log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setHttpStatus(ex.getError().getHttpStatus());
            task.setErrorReport(ex.getError());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setHttpStatus(500);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), ""));
        } finally {
            if (task != null) {
                terminate(task.getId());
            }
            taskHandler.edit(task);
        }
    }

    private String createStudyJSON(
            String predictedProperty,
            String modelId,
            List<Map<String, Object>> predictions,
            List<String> substances)
            throws UnsupportedEncodingException, JsonProcessingException {
        Studies studies = new Studies();
        List<Study> studyList = new ArrayList<>();

        for (int i = 0; i < predictions.size(); i++) {
            Object value = predictions.get(i).values().stream().findFirst().get();
            Study study = new Study();
            Owner owner = new Owner();
            Substance substance = new Substance();
            substance.setUuid(substances.get(i));
            owner.setSubstance(substance);
            study.setOwner(owner);

            String[] parts = predictedProperty.split("/");

            Protocol protocol = new Protocol();
            protocol.setTopcategory(parts[1]);
            Category category = new Category();
            category.setCode(parts[2]);
            protocol.setCategory(category);

            String endpoint = URLDecoder.decode(parts[3], "UTF-8");
            protocol.setEndpoint(endpoint);
            protocol.setGuideline(Arrays.asList("Predicted Property by Model " + modelId));
            study.setProtocol(protocol);
            Map<String, Object> reliability = new HashMap<>();
            reliability.put("r_studyResultType", "(Q)SAR");
            study.setReliability(reliability);

            Effect effect = new Effect();
            effect.setEndpoint(endpoint);
            Result result = new Result();
            result.setLoValue((Number) value);
            result.setLoQualifier("mean");
            effect.setResult(result);

            TreeMap<String, Object> conditions = new TreeMap<>();
            conditions.put("Model", modelId);
            effect.setConditions(conditions);
            study.setEffects(Arrays.asList(effect));
            study.setParameters(new HashMap<>());

            studyList.add(study);
        }
        studies.setStudy(studyList);
        String studyJSON = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(studies);
        return studyJSON;
    }

}
