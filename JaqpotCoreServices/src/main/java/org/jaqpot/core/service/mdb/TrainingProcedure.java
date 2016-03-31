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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
import javax.ws.rs.core.MediaType;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
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
            propertyValue = "java:jboss/exported/jms/topic/training"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class TrainingProcedure implements MessageListener {
    
    private static final Logger LOG = Logger.getLogger(TrainingProcedure.class.getName());
    
    @EJB
    TaskHandler taskHandler;
    
    @EJB
    AlgorithmHandler algorithmHandler;
    
    @EJB
    ModelHandler modelHandler;
    
    @Inject
    @Jackson
    JSONSerializer serializer;
    
    @Inject
    JPDIClient jpdiClient;
    
    @Inject
    @Secure
    Client client;
    
    Task task;
    
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
        String trans = (String) messageBody.get("transformations");
        String predictionFeature = (String) messageBody.get("prediction_feature");
        String parameters = (String) messageBody.get("parameters");
        String algorithmId = (String) messageBody.get("algorithmId");
        String modelTitle = (String) messageBody.get("title");
        String modelDescription = (String) messageBody.get("description");
        String subjectId = (String) messageBody.get("subjectid");
        String baseURI = (String) messageBody.get("base_uri");
        
        task = taskHandler.find(taskId);
        if (task == null) {
            LOG.log(Level.SEVERE, "Task with id:{0} could not be found in the database.", taskId);
            return;
        }
        if (task.getStatus().equals(Task.Status.CANCELLED)) {
            LOG.log(Level.INFO, "Task with id:{0} was already cancelled.", taskId);
            return;
        }
        
        task.setHttpStatus(202);
        task.setStatus(Task.Status.RUNNING);
        task.setType(Task.Type.TRAINING);
        progress(5f, "Training Task is now running.");
        
        Algorithm algorithm = algorithmHandler.find(algorithmId);
        
        if (algorithm == null) {
            errNotFound("Algorithm with id:" + algorithmId + " was not found.");
            return;
        }
        progress(10f, "Algorithm retrieved successfully.");
        
        Dataset dataset = null;
        if (dataset_uri != null && !dataset_uri.isEmpty()) {
            progress("Training dataset URI is:" + dataset_uri,
                    "Attempting to download dataset...");
            dataset = client.target(dataset_uri)
                    .request()
                    .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);
            dataset.setDatasetURI(dataset_uri);
            progress("Dataset has been retrieved.");
        }
        progress(20f);
        
        MetaInfo modelMeta = MetaInfoBuilder
                .builder()
                .addTitles(modelTitle)
                .addCreators(task.getMeta().getCreators())
                .addSources(dataset != null ? dataset.getDatasetURI() : "")
                .addComments("Created by task " + task.getId())
                .addDescriptions(modelDescription)
                .build();
        
        List<Model> transformationModels = new ArrayList<>();
        List<Model> linkedModels = new ArrayList<>();
        
        List<Algorithm> transformationAlgorithms = new ArrayList<>();
        List<Algorithm> linkedAlgorithms = new ArrayList<>();
        LinkedHashMap<String, String> transformations = null;
        
        if (trans != null && !trans.isEmpty()) {
            progress("--", "Processing transformations...");
            
            transformations = serializer.parse(trans, LinkedHashMap.class);
            
            for (String algUri : transformations.keySet()) {
                String algId = algUri.split("algorithm/")[1];
                Algorithm transAlgorithm = algorithmHandler.find(algId);
                if (transAlgorithm == null) {
                    errNotFound("Algorithm with id:" + algId + " was not found.");
                    return;
                }
                transformations.put(transAlgorithm.getId(), transformations.get(algUri));
                transformations.remove(algUri);
                if (transAlgorithm.getOntologicalClasses().contains("ot:Transformation")) {
                    transformationAlgorithms.add(transAlgorithm);
                } else {
                    linkedAlgorithms.add(transAlgorithm);
                }
            }
            for (Algorithm transAlgorithm : transformationAlgorithms) {
                progress("-", "Starting training on transformation algorithm:" + transAlgorithm.getId());
                
                Map<String, Object> parameterMap = null;
                String transParameters = transformations.get(transAlgorithm.getId());
                if (transParameters != null && !transParameters.isEmpty()) {
                    parameterMap = serializer.parse(transParameters, new HashMap<String, Object>().getClass());
                }
                try {
                    Model transModel = jpdiClient.train(dataset, transAlgorithm, parameterMap, predictionFeature, modelMeta, taskId).get();
                    transformationModels.add(transModel);
                    dataset = jpdiClient.predict(dataset, transModel, dataset != null ? dataset.getMeta() : null, taskId).get();
                    
                    progress(task.getPercentageCompleted() + 5f, "Transformation model created successfully:" + transModel.getId());
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, "JPDI Training procedure interupted", ex);
                    errorInternalServerError(ex, "JPDI Training procedure interupted");
                    return;
                } catch (ExecutionException ex) {
                    LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
                    errorInternalServerError(ex.getCause(), "JPDI Training procedure error");
                    return;
                } catch (CancellationException ex) {
                    LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
                    cancel();
                    return;
                }
            }
            progress("Done processing transformations.", "--");
        }
        progress(50f);
        
        Map<String, Object> parameterMap = null;
        if (parameters != null && !parameters.isEmpty()) {
            parameterMap = serializer.parse(parameters, new HashMap<String, Object>().getClass());
        }
        
        progress("Starting JPDI Prediction...");
        
        Future<Model> futureModel = jpdiClient.train(dataset, algorithm, parameterMap, predictionFeature, modelMeta, taskId);
        
        Model model = null;
        try {
            model = futureModel.get();
            progress("JPDI Training completed successfully.");
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "JPDI Training procedure interupted", ex);
            errorInternalServerError(ex, "JPDI Training procedure interupted");
            return;
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
            errorInternalServerError(ex, "JPDI Training procedure error");
            return;
        } catch (CancellationException ex) {
            LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
            cancel();
            return;
        } finally {
            taskHandler.edit(task);
        }
        
        progress(70f, "Model was built successfully.");
        
        for (Algorithm linkedAlgorithm : linkedAlgorithms) {
            String transParameters = transformations.get(linkedAlgorithm.getId());
            if (transParameters != null && !transParameters.isEmpty()) {
                parameterMap = serializer.parse(transParameters, new HashMap<String, Object>().getClass());
            }
            try {
                for (DataEntry de : dataset.getDataEntry()) {
                    de.getValues().keySet().retainAll(model.getIndependentFeatures());
                }
                Model linkedModel = jpdiClient.train(dataset, linkedAlgorithm, parameterMap, predictionFeature, modelMeta, taskId).get();
                linkedModels.add(linkedModel);
                progress(task.getPercentageCompleted() + 5f, "Linked model created successfully:" + linkedModel.getId());
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, "JPDI Training procedure interupted", ex);
                errorInternalServerError(ex, "JPDI Training procedure interupted");
                return;
            } catch (ExecutionException ex) {
                LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
                errorInternalServerError(ex, "JPDI Training procedure error");
                return;
            } catch (CancellationException ex) {
                LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
                cancel();
                return;
            } finally {
                taskHandler.edit(task);
            }
        }
        
        progress("Saving transformation models.");
        for (Model transModel : transformationModels) {
            transModel.setVisible(Boolean.FALSE);
            modelHandler.create(transModel);
        }
        
        progress(80f, "Saving linked models.");
        for (Model linkedModel : linkedModels) {
            linkedModel.setVisible(Boolean.FALSE);
            modelHandler.create(linkedModel);
        }
        
        progress(90f, "Saving main model.");
        
        model.setVisible(Boolean.TRUE);
        model.setTransformationModels(transformationModels.stream()
                .map(tm -> baseURI + "model/" + tm.getId())
                .collect(Collectors.toList())
        );
        model.setLinkedModels(linkedModels.stream()
                .map(lm -> baseURI + "model/" + lm.getId())
                .collect(Collectors.toList())
        );
        modelHandler.create(model);
        
        complete("model/" + model.getId());
    }
    
    private void progress(Float percentage, String... messages) {
        task.getMeta().getComments().addAll(Arrays.asList(messages));
        task.setPercentageCompleted(percentage);
        taskHandler.edit(task);
    }
    
    private void progress(Float percentage) {
        task.setPercentageCompleted(percentage);
        taskHandler.edit(task);
    }
    
    private void progress(String... messages) {
        task.getMeta().getComments().addAll(Arrays.asList(messages));
        taskHandler.edit(task);
    }
    
    private void cancel() {
        task.setStatus(Task.Status.CANCELLED);
        task.getMeta().getComments().add("Task was cancelled by the user.");
        taskHandler.edit(task);
    }
    
    private void complete(String result) {
        task.setResult(result);
        task.setHttpStatus(201);
        task.setPercentageCompleted(100.f);
        task.setDuration(System.currentTimeMillis() - task.getMeta().getDate().getTime()); // in ms
        task.setStatus(Task.Status.COMPLETED);
        task.getMeta().getComments().add("Task Completed Successfully.");
        taskHandler.edit(task);
    }
    
    private void errNotFound(String message) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(404);
        task.setErrorReport(ErrorReportFactory.notFoundError(message));
        taskHandler.edit(task);
    }
    
    private void errorInternalServerError(String message) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(500);
        task.setErrorReport(ErrorReportFactory.internalServerError(message, null));
        taskHandler.edit(task);
    }
    
    private void errorInternalServerError(Throwable t, String details) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(500);
        task.setErrorReport(ErrorReportFactory.internalServerError(t, details));
        taskHandler.edit(task);
    }
    
    private void error(Exception ex) {
        
    }
    
}
