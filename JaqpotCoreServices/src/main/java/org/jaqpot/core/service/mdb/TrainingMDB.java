/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.mdb;

import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.ws.rs.core.Response;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.ErrorReportBuilder;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.client.ClientFactory;
import org.jaqpot.core.service.dto.dataset.Dataset;
import org.jaqpot.core.service.dto.jpdi.TrainingRequest;
import org.jaqpot.core.service.dto.jpdi.TrainingResponse;

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
public class TrainingMDB implements MessageListener {

    private static final Logger LOG = Logger.getLogger(TrainingMDB.class.getName());

    @EJB
    TaskHandler taskHandler;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ModelHandler modelHandler;

    @Inject
    @UnSecure
    Client client;

    @Override
    public void onMessage(Message msg) {
        Task task = null;
        try {
            Map<String, Object> messageBody = msg.getBody(Map.class);
            task = taskHandler.find(messageBody.get("taskId"));

            task.setStatus(Task.Status.RUNNING);
            task.getMeta().getComments().add("Task is now running.");
            taskHandler.edit(task);

            task.getMeta().getComments().add("Attempting to download dataset...");
            taskHandler.edit(task);
            Dataset dataset = client.target((String) messageBody.get("dataset_uri"))
                    .request()
                    .header("subjectid", messageBody.get("subjectid"))
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);
            dataset.setDatasetURI((String) messageBody.get("dataset_uri"));

            task.getMeta().getComments().add("Dataset has been retrieved.");
            
            task.getMeta().getComments().add("Creating JPDI training request...");
            taskHandler.edit(task);
            
            TrainingRequest trainingRequest = new TrainingRequest();
            trainingRequest.setDataset(dataset);            
            task.getMeta().getComments().add("Inserted dataset.");
            taskHandler.edit(task);
            
            trainingRequest.setPredictionFeature((String) messageBody.get("prediction_feature"));
            task.getMeta().getComments().add("Inserted prediction feature.");
            taskHandler.edit(task);

            String parameters = (String) messageBody.get("parameters");
            Map<String, Object> parameterMap = new HashMap<>();
            if (parameters != null) {
                String[] parameterArray = parameters.split(",");
                for (String parameter : parameterArray) {
                    String[] keyValuePair = parameter.split("=");
                    parameterMap.put(keyValuePair[0].trim(), keyValuePair[1].trim());
                }
            }
            trainingRequest.setParameters(parameterMap);
            
            task.getMeta().getComments().add("Inserted parameters.");
            taskHandler.edit(task);

            String algorithmId = (String) messageBody.get("algorithmId");
            Algorithm algorithm = algorithmHandler.find(algorithmId);

            task.getMeta().getComments().add("Inserted algorithm id.");
            taskHandler.edit(task);

            task.getMeta().getComments().add("Sending request to  algorithm service:"+algorithm.getTrainingService());
            taskHandler.edit(task);
            Response response = client.target(algorithm.getTrainingService())
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(trainingRequest));
            task.getMeta().getComments().add("Algorithm service responded with status:"+response.getStatus());
            taskHandler.edit(task);
            
            task.getMeta().getComments().add("Attempting to parse response...");
            taskHandler.edit(task);
            TrainingResponse trainingResponse = response.readEntity(TrainingResponse.class);
            task.getMeta().getComments().add("Response was parsed successfully");
            taskHandler.edit(task);
            

            task.getMeta().getComments().add("Building model.");
            taskHandler.edit(task);

            Model model = new Model(UUID.randomUUID().toString());
            model.setActualModel(trainingResponse.getRawModel());
            model.setPmmlModel(trainingResponse.getPmmlModel());
            model.setAlgorithm(algorithm);
            model.setIndependentFeatures(trainingResponse.getIndependentFeatures());
            model.setAdditionalInfo(trainingResponse.getAdditionalInfo());
            ArrayList<String> dependentFeatures = new ArrayList<>();
            dependentFeatures.add(trainingRequest.getPredictionFeature());
            model.setDependentFeatures(dependentFeatures);
            ArrayList<String> predictedFeatures = new ArrayList<>();
            predictedFeatures.add(trainingRequest.getPredictionFeature() + "/" + URLEncoder.encode("Predicted By Model " + model.getId(), "UTF-8"));
            model.setPredictedFeatures(predictedFeatures);
            modelHandler.create(model);

            task.setResult(model.getId());
            task.setStatus(Task.Status.COMPLETED);

            task.getMeta().getComments().add("Task Completed Successfully.");
            taskHandler.edit(task);
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), ""));
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.badRequest(ex.getMessage(), ""));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), ""));
        } finally {
            taskHandler.edit(task);
        }
    }

}
