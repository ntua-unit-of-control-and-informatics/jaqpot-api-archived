package org.jaqpot.core.service.mdb;

/**
 * Created by Angelos Valsamis on 31/3/2017.
 */

import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.FeatureBuilder;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.client.jpdi.JPDIClient;
//import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.ws.rs.BadRequestException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jaqpot.core.service.authentication.AAService;
/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",
                propertyValue = "java:jboss/exported/jms/topic/descriptorspreparation"),
        @ActivationConfigProperty(propertyName = "destinationType",
                propertyValue = "javax.jms.Topic")
})
public class DescriptorCalculationProcedure extends AbstractJaqpotProcedure implements MessageListener {

    private static final Logger LOG = Logger.getLogger(PreparationProcedure.class.getName());

    @Inject
    JPDIClient jpdiClient;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    TaskHandler taskHandler;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    AAService aaService;

    @EJB
    FeatureHandler featureHandler;

    @Inject
    PropertyManager propertyManager;

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
        byte[] file = (byte[]) messageBody.get("file");
        String parameters = (String) messageBody.get("parameters");
        String algorithmId = (String) messageBody.get("algorithmId");

        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.PREPARATION);

            progress(5f, "Calculation Procedure is now running with ID " + Thread.currentThread().getName());
            progress(10f, "Starting calculation...");
            checkCancelled();

            Map<String, Object> parameterMap = null;
            if (parameters != null && !parameters.isEmpty()) {
                parameterMap = serializer.parse(parameters, new HashMap<String, Object>().getClass());
            }

            Algorithm algorithm = algorithmHandler.find(algorithmId);

            if (algorithm == null) {
                errNotFound("Algorithm with id:" + algorithmId + " was not found.");
                return;
            }

            progress("Starting JPDI Training...");
            checkCancelled();

            Future<Dataset> futureDataset = jpdiClient.calculate(file, algorithm, parameterMap, taskId, subjectId);
            Dataset dataset = futureDataset.get();

            dataset = populateFeatures(dataset);

            progress("JPDI Training completed successfully.");
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

            if (dataset.getDataEntry() == null || dataset.getDataEntry().isEmpty())
                throw new IllegalArgumentException("Resulting dataset is empty");
            else
                datasetHandler.create(dataset);

            progress(100f, "Dataset saved successfully.");
            checkCancelled();
            progress("Calculation Task is now completed.");
            complete("dataset/" + dataset.getId());
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, "Preparation procedure execution error", ex);
            errInternalServerError(ex, "JPDI Preparation procedure error");
        } catch (BadRequestException ex) {
            errBadRequest(ex, "Error while processing input.");
            LOG.log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "JPDI Preparation procedure unknown error", ex);
            errInternalServerError(ex, "JPDI Preparation procedure unknown error");
        }
    }

    private Dataset populateFeatures(Dataset dataset) throws JaqpotDocumentSizeExceededException {
        for (FeatureInfo featureInfo : dataset.getFeatures()) {
            String featureURI = null;
            /*if (featureInfo.getCategory().equals(Dataset.DescriptorCategory.CDK)) {
                Feature f = featureHandler.findByTitleAndIdentifier(featureInfo.getName(), String.valueOf(featureInfo.getConditions().get("Implementation Title")));
                if (f == null) {
                    f = FeatureBuilder.builder(featureInfo.getName() + "_" + new ROG(true).nextString(12))
                            .addTitles(featureInfo.getName())
                            .addIdentifiers(String.valueOf(featureInfo.getConditions().get("Implementation Title"))).build();
                    featureHandler.create(f);
                }
                featureURI = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE)+"feature/" + f.getId();

            } else {*/
            Feature f = FeatureBuilder.builder(featureInfo.getName().replaceAll(" ","_")  + "_" + new ROG(true).nextString(12))
                    .addTitles(featureInfo.getName()).build();
            featureHandler.create(f);
            featureURI = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE)+"feature/" + f.getId();
            //}

            //Update FeatureURIS in Data Entries
            for (DataEntry dataentry : dataset.getDataEntry()) {
                Object value = dataentry.getValues().remove(featureInfo.getURI());
                dataentry.getValues().put(featureURI,value);
            }
            //Update FeatureURI in Feature Info
            featureInfo.setURI(featureURI);
        }
        return dataset;
    }
}
