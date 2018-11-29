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

import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
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
import org.jaqpot.core.service.annotations.Secure;
import org.jaqpot.core.service.client.jpdi.JPDIClient;
import org.jaqpot.core.service.messaging.RabbitMQ;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

/**
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/training"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class TrainingProcedure extends AbstractJaqpotProcedure implements MessageListener {

    private static final Logger LOG = Logger.getLogger(TrainingProcedure.class.getName());

//    @Inject
//    RabbitMQ rabbitMQClient;

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

    public TrainingProcedure() {
        super(null);
    }

    @Inject
    public TrainingProcedure(TaskHandler taskHandler) {
        super(taskHandler);
    }

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
        String subjectId = (String) messageBody.get("subjectId");
        String baseURI = (String) messageBody.get("base_uri");
        String creator = (String) messageBody.get("creator");
        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.TRAINING);

            progress(5f, "Training Task is now running.");

            Algorithm algorithm = algorithmHandler.find(algorithmId);

            if (algorithm == null) {
                errNotFound("Algorithm with id:" + algorithmId + " was not found.");
                return;
            }
            progress(10f, "Algorithm retrieved successfully.");
            checkCancelled();

            Dataset dataset = null;
            if (dataset_uri != null && !dataset_uri.isEmpty()) {
                progress("Training dataset URI is:" + dataset_uri,
                        "Attempting to download dataset...");
                dataset = client.target(dataset_uri)
                        .request()
                        .header("Authorization", "Bearer "+subjectId)
                        .accept(MediaType.APPLICATION_JSON)
                        .get(Dataset.class);
                dataset.setDatasetURI(dataset_uri);
                progress("Dataset has been retrieved.");
            }
            progress(20f);
            checkCancelled();

            MetaInfo modelMeta = MetaInfoBuilder
                    .builder()
                    .addTitles(modelTitle)
                    .addCreators(creator)
                    .addSources(dataset != null ? dataset.getDatasetURI() : "")
                    .addComments("Created by task " + taskId)
                    .addDescriptions(modelDescription)
                    .build();

            List<Model> transformationModels = new ArrayList<>();
            List<Model> linkedModels = new ArrayList<>();

            List<Algorithm> transformationAlgorithms = new ArrayList<>();
            List<Algorithm> linkedAlgorithms = new ArrayList<>();
            LinkedHashMap<String, String> transformations = new LinkedHashMap<>();

            if (trans != null && !trans.isEmpty()) {
                progress("--", "Processing transformations...");

                transformations.putAll(serializer.parse(trans, LinkedHashMap.class));
                LinkedHashMap<String, String> newTransformations = new LinkedHashMap<>();
                transformations.keySet().stream().forEach((algUri) -> {
                    String algId = algUri.split("algorithm/")[1];
                    Algorithm transAlgorithm = algorithmHandler.find(algId);
                    if (transAlgorithm == null) {
                        errNotFound("Algorithm with id:" + algId + " was not found.");
                        return;
                    }
                    newTransformations.put(transAlgorithm.getId(), transformations.get(algUri));
                    if (transAlgorithm.getOntologicalClasses().contains("ot:Transformation")) {
                        transformationAlgorithms.add(transAlgorithm);
                    } else {
                        linkedAlgorithms.add(transAlgorithm);
                    }
                });
                transformations.putAll(newTransformations);
                for (Algorithm transAlgorithm : transformationAlgorithms) {
                    checkCancelled();
                    progress("-", "Starting training on transformation algorithm:" + transAlgorithm.getId());

                    Map<String, Object> parameterMap = null;
                    String transParameters = transformations.get(transAlgorithm.getId());
                    if (transParameters != null && !transParameters.isEmpty()) {
                        parameterMap = serializer.parse(transParameters, new HashMap<String, Object>().getClass());
                    }

                    Model transModel = jpdiClient.train(dataset, transAlgorithm, parameterMap, predictionFeature, modelMeta, taskId).get();
                    transformationModels.add(transModel);
                    dataset = jpdiClient.predict(dataset, transModel, dataset != null ? dataset.getMeta() : null, taskId).get();

                    addProgress(5f, "Transformation model created successfully:" + transModel.getId());

                }
                progress("Done processing transformations.", "--");
            }
            progress(50f);
            checkCancelled();

            Map<String, Object> parameterMap = null;
            if (parameters != null && !parameters.isEmpty()) {
                parameterMap = serializer.parse(parameters, new HashMap<String, Object>().getClass());
            }

            progress("Starting JPDI Training...");

            Future<Model> futureModel = jpdiClient.train(dataset, algorithm, parameterMap, predictionFeature, modelMeta, taskId);
            Model model = futureModel.get();
            progress("JPDI Training completed successfully.");

            progress(70f, "Model was built successfully.");
            checkCancelled();

            for (Algorithm linkedAlgorithm : linkedAlgorithms) {
                String transParameters = transformations.get(linkedAlgorithm.getId());
                if (transParameters != null && !transParameters.isEmpty()) {
                    parameterMap = serializer.parse(transParameters, new HashMap<String, Object>().getClass());
                }

                for (DataEntry de : dataset.getDataEntry()) {
                    de.getValues().keySet().retainAll(model.getIndependentFeatures());
                }
                Model linkedModel = jpdiClient.train(dataset, linkedAlgorithm, parameterMap, predictionFeature, modelMeta, taskId).get();
                linkedModels.add(linkedModel);
                addProgress(5f, "Linked model created successfully:" + linkedModel.getId());
                checkCancelled();
            }

            checkCancelled();
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
//            rabbitMQClient.sendMessage(creator,"Training procedure for model "+ modelTitle +" was completed successfully!");

        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "JPDI Training procedure interrupted", ex);
            errInternalServerError(ex, "JPDI Training procedure interrupted");
//            sendException(creator,"Error while creating model "+ modelTitle +". Interrupted.");
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
            errInternalServerError(ex.getCause(), "JPDI Training procedure error");
//            sendException(creator,"Error while creating model "+ modelTitle +". Internal Error. ");
        } catch (CancellationException ex) {
            LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
//            sendException(creator,"Error while creating model "+ modelTitle +". Task was cancelled");
            cancel();
        } catch (BadRequestException | IllegalArgumentException ex) {
            errBadRequest(ex, null);
//            sendException(creator,"Error while creating model "+ modelTitle +". Bad Request.");
        } catch (NotFoundException ex) {
            errNotFound(ex);
//            sendException(creator,"Error while creating model "+ modelTitle +". Not Found");

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "JPDI Training procedure unknown error", ex);
            errInternalServerError(ex, "JPDI Training procedure unknown error");
//            sendException(creator,"Error while creating model "+ modelTitle +". Unknown Error.");
        }

    }
//    private void sendException(String topic,String message) {
//        try {
//            rabbitMQClient.sendMessage(topic,message);
//        } catch (IOException | TimeoutException e) {
//            e.printStackTrace();
//        }
//    }

}
