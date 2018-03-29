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
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.DatasetFactory;
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
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * @author Angelos Valsamis
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/prediction")
    ,
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class PredictionProcedure extends AbstractJaqpotProcedure implements MessageListener {

    private static final Logger LOG = Logger.getLogger(PredictionProcedure.class.getName());

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ModelHandler modelHandler;

    @EJB
    DatasetHandler datasetHandler;

//    @Inject
//    RabbitMQ rabbitMQClient;

    @Inject
    @Jackson
    JSONSerializer serializer;

    @Inject
    JPDIClient jpdiClient;

    @Inject
    @Secure
    Client client;

    public PredictionProcedure() {
        super(null);
        //        throw new IllegalStateException("Cannot use empty constructor, instantiate with TaskHandler");
    }

    @Inject
    public PredictionProcedure(TaskHandler taskHandler) {
        super(taskHandler);
    }

    @Override
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
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
        String creator = (String) messageBody.get("creator");
        Model model = null;
        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.PREDICTION);

            progress(5f, "Prediction Task is now running.");

            model = modelHandler.find(modelId);
            if (model == null) {
                errNotFound("Model with id:" + modelId + " was not found.");
                return;
            }
            progress(10f, "Model retrieved successfully.");
            checkCancelled();

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
            checkCancelled();

            if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
                progress("--", "Processing transformations...");
                for (String transModelURI : model.getTransformationModels()) {
                    checkCancelled();
                    Model transModel = modelHandler.find(transModelURI.split("model/")[1]);
                    if (transModel == null) {
                        errNotFound("Transformation model with id:" + transModelURI + " was not found.");
                        return;
                    }
                    dataset = jpdiClient.predict(dataset, transModel, dataset != null ? dataset.getMeta() : null, taskId).get();
                    addProgress(5f, "Transformed successfull by model:" + transModel.getId());
                }
                progress("Done processing transformations.", "--");
            }
            progress(50f);
            checkCancelled();

            MetaInfo datasetMeta = dataset.getMeta();
            HashSet<String> creators = new HashSet<>(Arrays.asList(creator));
            datasetMeta.setCreators(creators);

            progress("Starting JPDI Prediction...");

            dataset = jpdiClient.predict(dataset, model, datasetMeta, taskId).get();
            progress("JPDI Prediction completed successfully.");
            progress(80f, "Dataset was built successfully.");
            checkCancelled();

            if (model.getLinkedModels() != null & !model.getLinkedModels().isEmpty()) {
                progress("--", "Processing linked models...");
                Dataset copyDataset = DatasetFactory.copy(dataset);
                for (String linkedModelURI : model.getLinkedModels()) {
                    checkCancelled();
                    Model linkedModel = modelHandler.find(linkedModelURI.split("model/")[1]);
                    if (linkedModel == null) {
                        errNotFound("Transformation model with id:" + linkedModelURI + " was not found.");
                        return;
                    }
                    Dataset linkedDataset = jpdiClient.predict(copyDataset, linkedModel, dataset != null ? dataset.getMeta() : null, taskId).get();
                    dataset = DatasetFactory.mergeColumns(dataset, linkedDataset);
                    addProgress(5f, "Prediction successfull by model:" + linkedModel.getId());

                }
                progress("Done processing linked models.", "--");
            }
            checkCancelled();
            progress(90f, "Now saving to database...");
            dataset.setVisible(Boolean.TRUE);
            dataset.setFeatured(Boolean.FALSE);
            dataset.setByModel(model.getId());
            datasetHandler.create(dataset);

            complete("dataset/" + dataset.getId());
//            rabbitMQClient.sendMessage(creator,"Prediction:"+dataset.getId()+":"+model.getMeta().getTitles().iterator().next());

        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "JPDI Prediction procedure interupted", ex);
            errInternalServerError(ex, "JPDI Prediction procedure interupted");
//            sendException(creator,"Error while applying model "+ model.getMeta().getTitles().iterator().next() +". Interrupted Error.");
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Prediction procedure execution error", ex.getCause());
            errInternalServerError(ex.getCause(), "JPDI Training procedure error");
//            sendException(creator,"Error while applying model "+ model.getMeta().getTitles().iterator().next() +". Execution Error.");
        } catch (CancellationException ex) {
            LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
//            sendException(creator,"Error while applying model "+ model.getMeta().getTitles().iterator().next() +". Cancel Error.");
            cancel();
        } catch (BadRequestException | IllegalArgumentException ex) {
            errBadRequest(ex, "null");
        } catch (EJBTransactionRolledbackException ex) {
            LOG.log(Level.SEVERE, "Task with id:{0} was canceled due to Ejb rollback exception", taskId);
            errInternalServerError(ex.getCause(), "JPDI could not create dataset");
            errBadRequest(ex, null);
//            sendException(creator,"Error while applying model "+ model.getMeta().getTitles().iterator().next() +". Bad Request.");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "JPDI Prediction procedure unknown error", ex);
            errInternalServerError(ex, "JPDI Prediction procedure unknown error");
//            sendException(creator,"Error while applying model "+ model.getMeta().getTitles().iterator().next() +". Unknown Error.");
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
