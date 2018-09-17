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

import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.bundle.BundleData;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.data.ConjoinerService;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
/*@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/preparation"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})*/
public class PreparationMDB extends RunningTaskMDB {

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

    @EJB
    AAService aaService;

    @Inject
    @UnSecure
    Client client;

    @Inject
    @Jackson
    JSONSerializer serializer;

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
            task.setStatus(Task.Status.RUNNING);

            task.getMeta().getComments().add("Preparation Task is now running with ID " + Thread.currentThread().getName());
            task.setPercentageCompleted(1.0f);
            taskHandler.edit(task);

            String substanceOwner= (String) messageBody.get("substance_owner");
            List<String> substances = (List<String>) messageBody.get("substances");
            Map<String, List<String>> properties = (Map<String, List<String>>) messageBody.get("properties");            //String bundleUri = (String) messageBody.get("bundle_uri");
            String subjectId = (String) messageBody.get("subjectid");
            String descriptors = (String) messageBody.get("descriptors");
            Boolean intersectColumns = (Boolean) messageBody.get("intersect_columns");
            Boolean retainNullValues = (Boolean) messageBody.get("retain_null_values");

            Set descriptorSet = serializer.parse(descriptors, Set.class);

            task.getMeta().getComments().add("Starting Dataset preparation...");
            task.setPercentageCompleted(6.0f);
            taskHandler.edit(task);
            Dataset dataset = new Dataset();
            //Dataset dataset = conjoinerService.prepareDataset(substanceOwner, subjectId, descriptorSet, intersectColumns, retainNullValues);

            task.getMeta().getComments().add("Dataset ready.");
            task.getMeta().getComments().add("Saving to database...");
            task.setPercentageCompleted(55.0f);
            taskHandler.edit(task);
            MetaInfo datasetMeta = MetaInfoBuilder.builder()
                    .addSources(substanceOwner)
                    .addTitles((String) messageBody.get("title"))
                    .addDescriptions((String) messageBody.get("description"))
                    .addComments("Created by task " + task.getId())
                    .addCreators(aaService.getUserFromSSO(subjectId).getId())
                    .build();
            dataset.setMeta(datasetMeta);
            dataset.setVisible(Boolean.TRUE);

            datasetHandler.create(dataset);

            task.getMeta().getComments().add("Dataset saved successfully.");
            task.setPercentageCompleted(80.0f);
            taskHandler.edit(task);

            String mode = (String) messageBody.get("mode");
            String baseUri = (String) messageBody.get("base_uri");
            String datasetUri = baseUri + dataset.getClass().getSimpleName().toLowerCase() + "/" + dataset.getId();

            switch (mode) {
                case "TRAINING":
                    task.getMeta().getComments().add("Preparation Task is now completed.");
                    task.getMeta().getComments().add("Initiating Training Task...");
                    task.setPercentageCompleted(90.0f);
                    taskHandler.edit(task);
                    messageBody.put("dataset_uri", datasetUri);
                    jmsContext.createProducer().setDeliveryDelay(1000).send(trainingQueue, messageBody);
                    break;
                case "PREDICTION":
                    task.getMeta().getComments().add("Preparation Task is now completed.");
                    task.getMeta().getComments().add("Initiating Prediction Task...");
                    task.setPercentageCompleted(91.0f);
                    taskHandler.edit(task);
                    messageBody.put("dataset_uri", datasetUri);
                    jmsContext.createProducer().setDeliveryDelay(1000).send(predictionQueue, messageBody);
                    break;

                default:
                    task.setStatus(Task.Status.COMPLETED);
                    task.getMeta().getComments().add("Preparation Task is now completed.");
                    task.setResult("dataset/" + dataset.getId());
                    task.setHttpStatus(200);
                    task.setPercentageCompleted(92.0f);
                    taskHandler.edit(task);
                    break;
            }
            task.setHttpStatus(200);
            task.setPercentageCompleted(100.0f);
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "Error Accessing JMS asynchronous queues."));
        } catch (BadRequestException ex) {
            LOG.log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.badRequest("Error while processing input.", ex.getMessage()));
        } catch (JaqpotDocumentSizeExceededException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.badRequest("Error creating Dataset resource, exceeding maximum dataset limit of 16 mb.", e.getMessage()));
            e.printStackTrace();
        } finally {
            if (task != null && task.getId() != null) {
                terminate(task.getId());
            }
            taskHandler.edit(task);
        }
    }

}
