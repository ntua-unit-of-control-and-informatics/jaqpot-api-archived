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
        String modelId = (String) messageBody.get("modelId");
        String subjectId = (String) messageBody.get("subjectid");

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
        task.setType(Task.Type.PREDICTION);
        progress(5f, "Prediction Task is now running.");

        Model model = modelHandler.find(modelId);
        if (model == null) {
            errNotFound("Model with id:" + modelId + " was not found.");
            return;
        }
        progress(10f, "Model retrieved successfully.");

        Dataset dataset;
        if (dataset_uri != null && !dataset_uri.isEmpty()) {
            progress("Attempting to download dataset...");
            dataset = client.target(dataset_uri)
                    .request()
                    .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);
            dataset.setDatasetURI(dataset_uri);
            progress("Dataset has been retrieved.");
        } else {
            dataset = DatasetFactory.createEmpty(0);
        }
        progress(20f);

        if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
            progress("--", "Processing transformations...");
            for (String transModelURI : model.getTransformationModels()) {
                Model transModel = modelHandler.find(transModelURI.split("model/")[1]);
                if (transModel == null) {
                    errNotFound("Transformation modle with id:" + transModelURI + " was not found.");
                    return;
                }
                try {
                    dataset = jpdiClient.predict(dataset, transModel, dataset != null ? dataset.getMeta() : null, taskId).get();
                    progress(task.getPercentageCompleted() + 5f, "Transformed successfull by model:" + transModel.getId());
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

        MetaInfo datasetMeta = dataset.getMeta();
        datasetMeta.setCreators(task.getMeta().getCreators());

        progress("Starting JPDI Prediction...");

        Future<Dataset> futureDataset = jpdiClient.predict(dataset, model, datasetMeta, taskId);

        try {
            dataset = futureDataset.get();
            task.getMeta().getComments().add("JPDI Prediction completed successfully.");
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "JPDI Prediction procedure interupted", ex);
            errorInternalServerError(ex, "JPDI Prediction procedure interupted");
            return;
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Prediction procedure execution error", ex.getCause());
            errorInternalServerError(ex, "JPDI Prediction procedure error");
            return;
        } catch (CancellationException ex) {
            LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
            cancel();
            return;
        } finally {
            taskHandler.edit(task);
        }
        progress(80f, "Dataset was built successfully.");

        if (model.getLinkedModels() != null & !model.getLinkedModels().isEmpty()) {
            progress("--", "Processing linked models...");
            Dataset copyDataset = DatasetFactory.copy(dataset);
            for (String linkedModelURI : model.getLinkedModels()) {
                Model linkedModel = modelHandler.find(linkedModelURI.split("model/")[1]);
                if (linkedModel == null) {
                    errNotFound("Transformation modle with id:" + linkedModelURI + " was not found.");
                    return;
                }
                try {
                    Dataset linkedDataset = jpdiClient.predict(copyDataset, linkedModel, dataset != null ? dataset.getMeta() : null, taskId).get();
                    dataset = DatasetFactory.mergeColumns(dataset, linkedDataset);
                    progress(task.getPercentageCompleted() + 5f, "Prediction successfull by model:" + linkedModel.getId());
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
            progress("Done processing linked models.", "--");
        }
        progress(90f, "Now saving to database...");
        dataset.setVisible(Boolean.TRUE);
        datasetHandler.create(dataset);

        complete("dataset/" + dataset.getId());
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

}
