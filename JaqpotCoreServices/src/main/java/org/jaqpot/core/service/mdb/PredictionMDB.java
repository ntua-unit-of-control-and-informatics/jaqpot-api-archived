/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.mdb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
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
import org.jaqpot.core.service.annotations.UnSecure;

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
public class PredictionMDB implements MessageListener {

    private static final Logger LOG = Logger.getLogger(PredictionMDB.class.getName());

    @EJB
    TaskHandler taskHandler;

    @EJB
    ModelHandler modelHandler;

    @EJB
    DatasetHandler datasetHandler;

    @Inject
    @UnSecure
    Client client;

    @Override
    public void onMessage(Message msg) {
        Task task = null;
        try {
            Map<String, Object> messageBody = msg.getBody(Map.class);
            task = taskHandler.find(messageBody.get("taskId"));

            if (task == null) {
                throw new NullPointerException("FATAL: Could not find task with id:" + messageBody.get("taskId"));
            }

            task.setStatus(Task.Status.RUNNING);
            task.setType(Task.Type.PREDICTION);
            task.getMeta().getComments().add("Prediction Task is now running.");
            taskHandler.edit(task);

            task.getMeta().getComments().add("Attempting to find model in database...");
            taskHandler.edit(task);
            Model model = modelHandler.find(messageBody.get("modelId"));
            if (model == null) {
                task.setStatus(Task.Status.ERROR);
                task.setErrorReport(ErrorReportFactory.notFoundError((String) messageBody.get("modelId")));
                taskHandler.edit(task);
                return;
            }
            task.getMeta().getComments().add("Model retrieved successfully.");

            task.getMeta().getComments().add("Attempting to download dataset...");
            taskHandler.edit(task);
            Dataset dataset = client.target((String) messageBody.get("dataset_uri"))
                    .request()
                    .header("subjectid", messageBody.get("subjectid"))
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);
            dataset.setDatasetURI((String) messageBody.get("dataset_uri"));
            task.getMeta().getComments().add("Dataset has been retrieved.");

            task.getMeta().getComments().add("Creating JPDI prediction request...");
            taskHandler.edit(task);
            PredictionRequest predictionRequest = new PredictionRequest();
            predictionRequest.setDataset(dataset);
            predictionRequest.setRawModel(model.getActualModel());
            predictionRequest.setAdditionalInfo(model.getAdditionalInfo());

            task.getMeta().getComments().add("Sending request to  algorithm service:" + model.getAlgorithm().getPredictionService());
            taskHandler.edit(task);
            Response response = client.target(model.getAlgorithm().getPredictionService())
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(predictionRequest));
            task.getMeta().getComments().add("Algorithm service responded with status:" + response.getStatus());
            taskHandler.edit(task);

            task.getMeta().getComments().add("Attempting to parse response...");
            taskHandler.edit(task);
            PredictionResponse predictionResponse = response.readEntity(PredictionResponse.class);
            response.close();
            task.getMeta().getComments().add("Response was parsed successfully.");

            task.getMeta().getComments().add("Creating new Dataset for predictions...");
            taskHandler.edit(task);
            List<Object> predictions = predictionResponse.getPredictions();
            for (int i = 0; i < dataset.getDataEntry().size(); i++) {
                dataset.getDataEntry().get(i).getValues().put(model.getPredictedFeatures().stream().findFirst().orElse("property/predicted"), predictions.get(i));
            }
            dataset.setId(UUID.randomUUID().toString());
            task.getMeta().getComments().add("Dataset ready.");
            task.getMeta().getComments().add("Saving to database...");
            taskHandler.edit(task);
            datasetHandler.create(dataset);
            task.getMeta().getComments().add("Dataset saved...");
            taskHandler.edit(task);

            if (messageBody.containsKey("bundle_uri") && messageBody.get("bundle_uri") != null) {
                task.getMeta().getComments().add("A bundle associated with these predictions was found.");
                task.getMeta().getComments().add("We will attempt to upload results into the bundle.");
                taskHandler.edit(task);

                String bundleUri = (String) messageBody.get("bundle_uri");

                List<String> substances = dataset.getDataEntry().stream().map(de -> {
                    return de.getCompound().getURI();
                }).collect(Collectors.toList());
                String studyJSON = createStudyJSON(model.getPredictedFeatures().stream().findFirst().get(), model.getId(), predictions, substances);

                task.getMeta().getComments().add("Creating a working matrix in the bundle...");
                taskHandler.edit(task);
                MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
                formData.putSingle("deletematrix", "true");
                client.target(bundleUri + "/matrix/working")
                        .request()
                        .post(Entity.form(formData)).close();

                task.getMeta().getComments().add("Now putting results in the bundle...");
                taskHandler.edit(task);
                Response taskResponse = client.target(bundleUri + "/matrix")
                        .request()
                        .header("Content-type", MediaType.APPLICATION_JSON)
                        .accept("text/uri-list")
                        .put(Entity.entity(studyJSON, MediaType.APPLICATION_JSON));

                String taskUri = taskResponse.readEntity(String.class);
                taskResponse.close();

                task.getMeta().getComments().add("An upload task has been started with URI:" + taskUri.trim());
                taskHandler.edit(task);
            }
            task.setStatus(Task.Status.COMPLETED);
            task.setResult("dataset/" + dataset.getId());
            task.getMeta().getComments().add("Task Completed Successfully.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), ""));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), ""));
        } finally {
            taskHandler.edit(task);
        }
    }

    private String createStudyJSON(String predictedProperty, String modelId, List<Object> predictions, List<String> substances) throws UnsupportedEncodingException, JsonProcessingException {
        Studies studies = new Studies();
        List<Study> studyList = new ArrayList<>();

        for (int i = 0; i < predictions.size(); i++) {
            Object value = predictions.get(i);
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
