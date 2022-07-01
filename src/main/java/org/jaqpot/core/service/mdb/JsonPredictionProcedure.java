/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jaqpot.core.service.mdb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.DoaHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.data.wrappers.DatasetLegacyWrapper;
import org.jaqpot.core.model.Doa;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.service.annotations.Secure;
import org.jaqpot.core.service.client.jpdi.JPDIClient;

/**
 *
 * @author pantelispanka
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/jsonprediction")
    ,
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class JsonPredictionProcedure extends AbstractJaqpotProcedure implements MessageListener {
    
    
    private static final Logger LOG = Logger.getLogger(JsonPredictionProcedure.class.getName());

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    DatasetLegacyWrapper datasetLegacyWrapper;
    @EJB
    ModelHandler modelHandler;

    @EJB
    DatasetHandler datasetHandler;

//    @EJB
//    DoaService doaService;
    @EJB
    DoaHandler doaHandler;

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

    public JsonPredictionProcedure() {
        super(null);
        //        throw new IllegalStateException("Cannot use empty constructor, instantiate with TaskHandler");
    }

    @Inject
    public JsonPredictionProcedure(TaskHandler taskHandler) {
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
        String dataset_string = (String) messageBody.get("dataset");
        String modelId = (String) messageBody.get("modelId");
        String apiKey = (String) messageBody.get("api_key");
        String creator = (String) messageBody.get("creator");
        String doa = (String) messageBody.get("doa");
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
                progress("Searching dataset...");
                String[] splitted = dataset_uri.split("/");
                dataset = datasetLegacyWrapper.find(splitted[splitted.length - 1]);
                dataset.setDatasetURI(dataset_uri);
                progress("Dataset has been retrieved.");
            } else {
                dataset = DatasetFactory.createEmpty(0);
            }
            progress(20f);
            checkCancelled();

            // if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
            //     progress("--", "Processing transformations...");
            //     for (String transModelURI : model.getTransformationModels()) {
            //         checkCancelled();
            //         Model transModel = modelHandler.find(transModelURI.split("model/")[1]);
            //         if (transModel == null) {
            //             errNotFound("Transformation model with id:" + transModelURI + " was not found.");
            //             return;
            //         }
            //         dataset = jpdiClient.predict(dataset, transModel, dataset != null ? dataset.getMeta() : null, taskId, null).get();
            //         addProgress(5f, "Transformed successfull by model:" + transModel.getId());
            //     }
            //     progress("Done processing transformations.", "--");
            // }
            // progress(50f);
            checkCancelled();

            MetaInfo datasetMeta = dataset.getMeta();
            HashSet<String> creators = new HashSet<>(Arrays.asList(creator));
            datasetMeta.setCreators(creators);

            Doa doaM = null;
            if (doa != null && doa.equals("true")) {
                doaM = doaHandler.findBySourcesWithDoaMatrix("model/" + model.getId());
            }

            progress("Starting Prediction...");

            dataset = jpdiClient.predict(dataset, model, datasetMeta, taskId, doaM).get();
            progress("Prediction completed successfully.");
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
                    Dataset linkedDataset = jpdiClient.predict(copyDataset, linkedModel, dataset != null ? dataset.getMeta() : null, taskId, null).get();
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
            datasetLegacyWrapper.create(dataset);
            //datasetHandler.create(dataset);

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
    
}
