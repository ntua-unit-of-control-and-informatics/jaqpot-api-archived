package org.jaqpot.core.service.mdb;

/**
 * Created by root on 30/6/2016.
 */

import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;

import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.data.ConjoinerService;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",
                propertyValue = "java:jboss/exported/jms/topic/preparation"),
        @ActivationConfigProperty(propertyName = "destinationType",
                propertyValue = "javax.jms.Topic")
})
public class PreparationProcedure extends AbstractJaqpotProcedure implements MessageListener {

    private static final Logger LOG = Logger.getLogger(PreparationProcedure.class.getName());
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
    public PreparationProcedure() {
        super(null);
    }

    @Inject
    public PreparationProcedure(TaskHandler taskHandler) {
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

        String substanceOwner= (String) messageBody.get("substance_owner");
        String taskId = (String) messageBody.get("taskId");
        String bundleUri = (String) messageBody.get("bundle_uri");
        String subjectId = (String) messageBody.get("subjectid");
        String substances = (String) messageBody.get("substances");
        String properties = (String) messageBody.get("properties");
        String descriptors = (String) messageBody.get("descriptors");
        Boolean intersectColumns = (Boolean) messageBody.get("intersect_columns");
        Boolean retainNullValues = (Boolean) messageBody.get("retain_null_values");

        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.PREPARATION);

            progress(5f, "Preparation Procedure is now running with ID " + Thread.currentThread().getName());


            Set descriptorSet = serializer.parse(descriptors, Set.class);
            Set substancesSet = serializer.parse(substances,Set.class);
            Set propertiesSet = serializer.parse(properties,Set.class);
            progress(10f, "Starting Dataset preparation...");
            checkCancelled();

            Dataset dataset = conjoinerService.prepareDataset(substanceOwner, substancesSet, subjectId, descriptorSet, propertiesSet, intersectColumns, retainNullValues);

            progress(50f, "Dataset ready.");
            progress("Saving to database...");

            checkCancelled();

            MetaInfo datasetMeta = MetaInfoBuilder.builder()
                    .addSources(bundleUri)
                    .addTitles((String) messageBody.get("title"))
                    .addDescriptions((String) messageBody.get("description"))
                    .addComments("Created by task " + taskId)
                    .addCreators(aaService.getUserFromSSO(subjectId).getId())
                    .build();
            dataset.setMeta(datasetMeta);
            dataset.setVisible(Boolean.TRUE);

            if (dataset.getDataEntry() == null || dataset.getDataEntry().isEmpty()
            || dataset.getDataEntry().get(0).getValues()==null || dataset.getDataEntry().get(0).getValues().isEmpty() ) {
            throw new IllegalArgumentException("Resulting dataset is empty");
            }

            datasetHandler.create(dataset);

            progress(80f, "Dataset saved successfully.");

            checkCancelled();

            String mode = (String) messageBody.get("mode");
            String baseUri = (String) messageBody.get("base_uri");
            String datasetUri = baseUri + dataset.getClass().getSimpleName().toLowerCase() + "/" + dataset.getId();

            switch (mode) {
                case "TRAINING":
                    progress(90f, "Preparation Task is now completed.");
                    complete("Initiating Training Task...");
                    messageBody.put("dataset_uri", datasetUri);
                    jmsContext.createProducer().setDeliveryDelay(1000).send(trainingQueue, messageBody);

                    break;
                case "PREDICTION":
                    progress(91f, "Preparation Task is now completed.");
                    complete("Initiating Prediction Task...");
                    messageBody.put("dataset_uri", datasetUri);
                    jmsContext.createProducer().setDeliveryDelay(1000).send(predictionQueue, messageBody);
                    break;

                default:
                    progress("Preparation Task is now completed.");
                    complete("dataset/" + dataset.getId());
                    break;
            }
        } catch (BadRequestException ex) {
            errBadRequest(ex, "Error while processing input.");
            LOG.log(Level.SEVERE, null, ex);
        }
        catch (Exception ex) {
            LOG.log(Level.SEVERE, "JPDI Validation procedure unknown error", ex);
            errInternalServerError(ex, "JPDI Validation procedure unknown error");
        }
    }
}
