/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.mdb;

import java.security.GeneralSecurityException;
import java.util.List;
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
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.client.ClientFactory;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.PredictionRequest;
import org.jaqpot.core.model.dto.jpdi.PredictionResponse;
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

            task.setStatus(Task.Status.RUNNING);
            taskHandler.edit(task);

            Model model = modelHandler.find(messageBody.get("modelId"));

            Dataset dataset = client.target((String) messageBody.get("dataset_uri"))
                    .request()
                    .header("subjectid", messageBody.get("subjectid"))
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);

            dataset.setDatasetURI((String) messageBody.get("dataset_uri"));

            PredictionRequest predictionRequest = new PredictionRequest();
            predictionRequest.setDataset(dataset);
            predictionRequest.setRawModel(model.getActualModel());
            predictionRequest.setAdditionalInfo(model.getAdditionalInfo());

            Response response = client.target(model.getAlgorithm().getPredictionService())
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(predictionRequest));

            PredictionResponse predictionResponse = response.readEntity(PredictionResponse.class);
            List<Object> predictions = predictionResponse.getPredictions();
            for (int i = 0; i < dataset.getDataEntry().size(); i++) {
                dataset.getDataEntry().get(i).getValues().put(model.getPredictedFeatures().stream().findFirst().orElse("property/predicted"), predictions.get(i));
            }
            dataset.setId(UUID.randomUUID().toString());
            datasetHandler.create(dataset);

            task.setStatus(Task.Status.COMPLETED);
            task.setResult(dataset.getId());
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

}
