/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.mdb;

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
import org.jaqpot.core.service.client.ClientUtils;
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

    @Override
    public void onMessage(Message msg) {
        Task task = null;
        try {
            Map<String, Object> messageBody = msg.getBody(Map.class);
            task = taskHandler.find(messageBody.get("taskId"));

            task.setStatus(Task.Status.RUNNING);
            taskHandler.edit(task);

            Client client = ClientUtils.buildUnsecureRestClient();
            Dataset dataset = client.target((String) messageBody.get("dataset_uri"))
                    .request()
                    .header("subjectid", messageBody.get("subjectid"))
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);
            dataset.setDatasetURI((String) messageBody.get("dataset_uri"));
            TrainingRequest trainingRequest = new TrainingRequest();
            trainingRequest.setDataset(dataset);
            trainingRequest.setPredictionFeature((String) messageBody.get("prediction_feature"));

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

            String algorithmId = (String) messageBody.get("algorithmId");
            Algorithm algorithm = algorithmHandler.find(algorithmId);

            Response response = client.target(algorithm.getTrainingService())
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(trainingRequest));

            TrainingResponse trainingResponse = response.readEntity(TrainingResponse.class);

            Model model = new Model(UUID.randomUUID().toString());
            model.setActualModel(trainingResponse.getRawModel());
            model.setPmmlModel(trainingResponse.getPmmlModel());
            model.setAlgorithm(algorithm);
            model.setIndependentFeatures(trainingResponse.getIndependentFeatures());
            model.setAdditionalInfo(trainingResponse.getAdditionalInfo());
            ArrayList<String> dependentFeatures = new ArrayList<>();
            dependentFeatures.add(trainingRequest.getPredictionFeature());
            model.setDependentFeatures(dependentFeatures);
            modelHandler.create(model);

            task.setResult(model.getId());
            task.setStatus(Task.Status.COMPLETED);
        } catch (JMSException | GeneralSecurityException ex) {
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
