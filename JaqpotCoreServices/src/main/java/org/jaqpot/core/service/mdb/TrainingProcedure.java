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
import java.util.HashMap;
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
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.Secure;
import org.jaqpot.core.service.client.jpdi.JPDIClient;

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

        Task task = taskHandler.find(taskId);
        if (task == null) {
            LOG.log(Level.SEVERE, "Task with id:{0} could not be found in the database.", taskId);
            return;
        }

        task.setHttpStatus(202);
        task.setStatus(Task.Status.RUNNING);
        task.setType(Task.Type.TRAINING);
        task.getMeta().getComments().add("Training Task is now running.");
        task.setPercentageCompleted(5f);
        taskHandler.edit(task);

        if (trans != null && !trans.isEmpty()) {
            task.getMeta().getComments().add("--");
            task.getMeta().getComments().add("Processing transformations...");
            taskHandler.edit(task);

            String transformationsString = (String) messageBody.get("transformations");
            LinkedHashMap<String, String> transformations = serializer.parse(transformationsString, LinkedHashMap.class);
            List<Algorithm> transformationAlgorithms = new ArrayList<>();
            List<Algorithm> linkedAlgorithms = new ArrayList<>();
        }

        Dataset dataset = client.target(dataset_uri)
                .request()
                .header("subjectid", subjectId)
                .accept(MediaType.APPLICATION_JSON)
                .get(Dataset.class);
        Algorithm algorithm = algorithmHandler.find(algorithmId);

        Map<String, Object> parameterMap = null;
        if (parameters != null && !parameters.isEmpty()) {
            parameterMap = serializer.parse(parameters, new HashMap<String, Object>().getClass());
        }

        MetaInfo modelMeta = MetaInfoBuilder
                .builder()
                .addTitles(modelTitle)
                .addCreators(task.getMeta().getCreators())
                .addSources(dataset != null ? dataset.getDatasetURI() : "")
                .addComments("Created by task " + task.getId())
                .addDescriptions(modelDescription)
                .build();

        Future<Model> futureModel = jpdiClient.train(dataset, algorithm, parameterMap, predictionFeature, modelMeta, taskId);

        Model model = null;
        try {
            model = futureModel.get();
            task.getMeta().getComments().add("JPDI Training completed successfully.");
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "JPDI Training procedure interupted", ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", "JPDI Training procedure interupted", ex.getMessage()));
            return;
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", "JPDI Training procedure error", ex.getMessage()));
            return;
        } catch (CancellationException ex) {
            LOG.log(Level.SEVERE, "Task with id:" + taskId + " was cancelled", ex);
            task.setStatus(Task.Status.CANCELLED);
            task.getMeta().getComments().add("Task was cancelled by the user.");
            return;
        } finally {
            taskHandler.edit(task);
        }

        task.getMeta().getComments().add("Model was built successfully. Now saving to database...");
        taskHandler.edit(task);
        model.setVisible(Boolean.TRUE);
        modelHandler.create(model);

        task.setResult("model/" + model.getId());
        task.setHttpStatus(201);
        task.setPercentageCompleted(100.f);
        task.setDuration(System.currentTimeMillis() - task.getMeta().getDate().getTime()); // in ms
        task.setStatus(Task.Status.COMPLETED);
        task.getMeta().getComments().add("Task Completed Successfully.");
        taskHandler.edit(task);

    }

}
