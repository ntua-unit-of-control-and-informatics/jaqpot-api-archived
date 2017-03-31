package org.jaqpot.core.service.mdb;

/**
 * Created by Angelos Valsamis on 31/3/2017.
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
import org.jaqpot.core.service.data.*;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.*;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Created by root on 30/6/2016.
 */

import org.jaqpot.core.service.data.AAService;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
/*@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",
                propertyValue = "java:jboss/exported/jms/topic/descriptorspreparation"),
        @ActivationConfigProperty(propertyName = "destinationType",
                propertyValue = "javax.jms.Topic")
})*/
public class DescriptorCalculationProcedure extends AbstractJaqpotProcedure implements MessageListener {

    private static final Logger LOG = Logger.getLogger(PreparationProcedure.class.getName());
    @Resource(lookup = "java:jboss/exported/jms/topic/training")
    private Topic trainingQueue;

    @Resource(lookup = "java:jboss/exported/jms/topic/prediction")
    private Topic predictionQueue;

    @Inject
    private JMSContext jmsContext;

    @EJB
    CalculationService smilesService;

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
    public DescriptorCalculationProcedure() {
        super(null);
    }

    @Inject
    public DescriptorCalculationProcedure(TaskHandler taskHandler) {
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
        String subjectId = (String) messageBody.get("subjectId");
        Byte[] file = (Byte[]) messageBody.get("bytes");

        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.PREPARATION);

            progress(5f, "Preparation Procedure is now running with ID " + Thread.currentThread().getName());


            //Set descriptorSet = serializer.parse(descriptors, Set.class);

            progress(10f, "Starting Dataset preparation...");
            checkCancelled();

            Dataset dataset = smilesService.prepareDataset(file);

            progress(50f, "Dataset ready.");
            progress("Saving to database...");

            checkCancelled();

            MetaInfo datasetMeta = MetaInfoBuilder.builder()
                    .addTitles((String) messageBody.get("title"))
                    .addDescriptions((String) messageBody.get("description"))
                    .addComments("Created by task " + taskId)
                    .addCreators(aaService.getUserFromSSO(subjectId).getId())
                    .build();
            dataset.setMeta(datasetMeta);
            dataset.setVisible(Boolean.TRUE);

            datasetHandler.create(dataset);

            progress(100f, "Dataset saved successfully.");

            checkCancelled();

            progress("Preparation Task is now completed.");
            complete("dataset/" + dataset.getId());

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
