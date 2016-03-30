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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
import javax.ws.rs.core.MediaType;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.Secure;
import org.jaqpot.core.service.client.jpdi.JPDIClient;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 *
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/prediction"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class PredictionProcedure implements MessageListener {
    
    private static final Logger LOG = Logger.getLogger(PredictionProcedure.class.getName());
    
    @EJB
    TaskHandler taskHandler;
    
    @EJB
    AlgorithmHandler algorithmHandler;
    
    @EJB
    ModelHandler modelHandler;
    
    @EJB
    DatasetHandler datasetHandler;
    
    @Inject
    @Jackson
    JSONSerializer serializer;
    
    @Inject
    JPDIClient jpdiClient;
    
    @Inject
    @Secure
    Client client;
    
    @Override
    public void onMessage(Message msg) {
        Map<String, Object> messageBody;
        try {
            messageBody = msg.getBody(Map.class);
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "JMS message could not be read", ex);
            return;
        }
        
        String taskId = (String) messageBody.get("taskId");
        String dataset_uri = (String) messageBody.get("dataset_uri");
        String modelId = (String) messageBody.get("modelId");
        String subjectId = (String) messageBody.get("subjectid");
        
        Task task = taskHandler.find(taskId);
        if (task == null) {
            LOG.log(Level.SEVERE, "Task with id:{0} could not be found in the database.", taskId);
            return;
        }
        
        task.setHttpStatus(202);
        task.setStatus(Task.Status.RUNNING);
        task.setType(Task.Type.PREDICTION);
        task.getMeta().getComments().add("Prediction Task is now running.");
        task.setPercentageCompleted(5f);
        taskHandler.edit(task);
        
        Model model = modelHandler.find(modelId);
        if (model == null) {
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.notFoundError("Model with id:"+modelId+" was not found."));
            taskHandler.edit(task);
            return;
        }
        
        task.getMeta().getComments().add("Model retrieved successfully.");
        task.setPercentageCompleted(10f);
        taskHandler.edit(task);
        
        Dataset dataset;
        if (dataset_uri != null && !dataset_uri.isEmpty()) {
            task.getMeta().getComments().add("Attempting to download dataset...");
            taskHandler.edit(task);
            dataset = client.target(dataset_uri)
                    .request()
                    .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);
            dataset.setDatasetURI(dataset_uri);
            task.getMeta().getComments().add("Dataset has been retrieved.");
        } else {
            dataset = DatasetFactory.createEmpty(0);
        }
        task.setPercentageCompleted(20f);
        taskHandler.edit(task);
        
        if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
            task.getMeta().getComments().add("--");
            task.getMeta().getComments().add("Processing transformations...");
            taskHandler.edit(task);
            
            task.getMeta().getComments().add("Done processing transformations.");
            task.getMeta().getComments().add("--");
        }
        task.setPercentageCompleted(50f);
        
        MetaInfo datasetMeta = dataset.getMeta();
        datasetMeta.setCreators(task.getMeta().getCreators());
        
        task.getMeta().getComments().add("Starting JPDI Prediction...");
        taskHandler.edit(task);
        
        Future<Dataset> futureDataset = jpdiClient.predict(dataset, model, datasetMeta, taskId);
        
        try {
            dataset = futureDataset.get();
            task.getMeta().getComments().add("JPDI Prediction completed successfully.");
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "JPDI Prediction procedure interupted", ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", "JPDI Prediction procedure interupted", ex.getMessage()));
            return;
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Prediction procedure execution error", ex.getCause());
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", "JPDI Prediction procedure error", ex.getMessage()));
            return;
        } catch (CancellationException ex) {
            LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
            task.setStatus(Task.Status.CANCELLED);
            task.getMeta().getComments().add("Task was cancelled by the user.");
            return;
        } finally {
            taskHandler.edit(task);
        }
        
        task.getMeta().getComments().add("Dataset was built successfully. Now saving to database...");
        task.setPercentageCompleted(80f);
        taskHandler.edit(task);
        dataset.setVisible(Boolean.TRUE);
        datasetHandler.create(dataset);
        
        task.setResult("dataset/" + dataset.getId());
        task.setHttpStatus(201);
        task.setPercentageCompleted(100f);
        task.setDuration(System.currentTimeMillis() - task.getMeta().getDate().getTime()); // in ms
        task.setStatus(Task.Status.COMPLETED);
        task.getMeta().getComments().add("Task Completed Successfully.");
        taskHandler.edit(task);
        
    }
    
}
