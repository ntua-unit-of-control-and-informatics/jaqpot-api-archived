/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.mdb;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.data.ConjoinerService;

/**
 *
 * @author hampos
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/preparation"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class PreparationMDB implements MessageListener {

    private static final Logger LOG = Logger.getLogger(PreparationMDB.class.getName());

    @Resource(lookup = "java:jboss/exported/jms/topic/training")
    private Topic trainingQueue;

    @Resource(lookup = "java:jboss/exported/jms/topic/prediction")
    private Topic predictionQueue;

    @Inject
    private JMSContext jmsContext;

    @EJB
    ConjoinerService conjoinerService;

    @EJB
    TaskHandler taskHandler;

    @EJB
    DatasetHandler datasetHandler;

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
            task.getMeta().getComments().add("Preparation Task is now running.");
            taskHandler.edit(task);

            String bundleUri = (String) messageBody.get("bundle_uri");
            String subjectId = (String) messageBody.get("subjectid");

            task.getMeta().getComments().add("Strarting Dataset preparation...");
            taskHandler.edit(task);
            Dataset dataset = conjoinerService.prepareDataset(bundleUri, subjectId);

            task.getMeta().getComments().add("Dataset ready.");
            task.getMeta().getComments().add("Saving to database...");
            taskHandler.edit(task);
            datasetHandler.create(dataset);

            task.getMeta().getComments().add("Dataset saved successfully.");
            taskHandler.edit(task);

            String mode = (String) messageBody.get("mode");
            String baseUri = (String) messageBody.get("base_uri");
            String datasetUri = baseUri + dataset.getClass().getSimpleName().toLowerCase() + "/" + dataset.getId();

            switch (mode) {
                case "TRAINING":
                    task.getMeta().getComments().add("Preparation Task is now completed.");
                    task.getMeta().getComments().add("Initiating Training Task...");
                    taskHandler.edit(task);
                    messageBody.put("dataset_uri", datasetUri);
                    jmsContext.createProducer().setDeliveryDelay(1000).send(trainingQueue, messageBody);
                    break;
                case "PREDICTION":
                    task.getMeta().getComments().add("Preparation Task is now completed.");
                    task.getMeta().getComments().add("Initiating Prediction Task...");
                    taskHandler.edit(task);
                    messageBody.put("dataset_uri", datasetUri);
                    jmsContext.createProducer().setDeliveryDelay(1000).send(predictionQueue, messageBody);
                    break;

                default:
                    task.setStatus(Task.Status.COMPLETED);
                    task.getMeta().getComments().add("Preparation Task is now completed.");
                    task.setResult(dataset.getId());
                    taskHandler.edit(task);
                    break;
            }

        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "JMS", "Error Accessing JMS asynchronous queues.", ex.getMessage()));
        } finally {
            taskHandler.edit(task);
        }
    }

}
