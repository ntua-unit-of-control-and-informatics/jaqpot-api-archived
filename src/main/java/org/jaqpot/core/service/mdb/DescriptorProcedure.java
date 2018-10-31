package org.jaqpot.core.service.mdb;

import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.DescriptorHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.data.wrappers.DatasetLegacyWrapper;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.FeatureBuilder;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.Secure;
import org.jaqpot.core.service.authentication.AAService;
import org.jaqpot.core.service.client.jpdi.JPDIClient;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",
                propertyValue = "java:jboss/exported/jms/topic/descriptor"),
        @ActivationConfigProperty(propertyName = "destinationType",
                propertyValue = "javax.jms.Topic")
})
public class DescriptorProcedure extends AbstractJaqpotProcedure implements MessageListener {

    private static final Logger LOG = Logger.getLogger(PreparationProcedure.class.getName());

    @Inject
    JPDIClient jpdiClient;

    @EJB
    DescriptorHandler descriptorHandler;

    @EJB
    DatasetLegacyWrapper datasetLegacyWrapper;

    @EJB
    AAService aaService;

    @Inject
    @Jackson
    JSONSerializer serializer;

    @EJB
    FeatureHandler featureHandler;

    @Inject
    PropertyManager propertyManager;

    @Inject
    @Secure
    Client client;

    public DescriptorProcedure() {
        super(null);
    }

    @Inject
    public DescriptorProcedure(TaskHandler taskHandler) {
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
        String apiKey = (String) messageBody.get("api_key");
        String parameters = (String) messageBody.get("parameters");
        String features = (String) messageBody.get("featureURIs");

        String descriptorId = (String) messageBody.get("descriptorId");
        String datasetURI = (String) messageBody.get("datasetURI");

        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.PREPARATION);

            progress(5f, "Descriptor Procedure is now running with ID " + Thread.currentThread().getName());
            progress(10f, "Starting descriptor calculation...");
            checkCancelled();

            HashMap parameterMap = null;
            if (parameters != null && !parameters.isEmpty()) {
                parameterMap = serializer.parse(parameters, HashMap.class);
            }

            Set<String> featureURIs = null;
            if (features != null && !features.isEmpty()) {
                featureURIs = serializer.parse(features, Set.class);
            }


            Descriptor descriptor = descriptorHandler.find(descriptorId);

            if (descriptor == null) {
                errNotFound("Descriptor with id:" + descriptorId + " was not found.");
                return;
            }

            checkCancelled();
            Dataset initialDataset;
            try{
                initialDataset = client.target(datasetURI)
                        .queryParam("dataEntries", true)
                        .request()
                        .header("Authorization", "Bearer " + apiKey)
                        .accept(MediaType.APPLICATION_JSON)
                        .get(Dataset.class);
            }catch(NotFoundException e){
                String[] splitted = datasetURI.split("/");
                initialDataset = datasetLegacyWrapper.find(splitted[splitted.length -1]);
            }

            if (initialDataset == null) {
                errNotFound("Dataset with id:" + Arrays.toString(datasetURI.split("/")) + " was not found.");
                return;
            }
            Dataset subDataset = null;

            //Create Dataset with features URIs that we want to extract descriptors from
            subDataset = DatasetFactory.copy(initialDataset,featureURIs);

            subDataset.setMeta(null);
            Future<Dataset> futureDataset = jpdiClient.descriptor(subDataset,descriptor,parameterMap,taskId);

            Dataset dataset = futureDataset.get();
            dataset.setId(new ROG(true).nextString(12));
            String newDatasetURI = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE)+"dataset/" + dataset.getId();

            copyFeatures(initialDataset,newDatasetURI);
            populateFeatures(dataset, newDatasetURI);
            dataset = DatasetFactory.mergeColumns(dataset,initialDataset);

            progress("JPDI Descriptor calculation procedure completed successfully.");
            progress(50f, "Dataset ready.");
            progress("Saving to database...");
            checkCancelled();

            MetaInfo datasetMeta = MetaInfoBuilder.builder()
                    .addTitles((String) messageBody.get("title"))
                    .addDescriptions((String) messageBody.get("description"))
                    .addComments("Created by task " + taskId)
                    .addCreators(aaService.getUserFromSSO(apiKey).getId())
                    .addSources(datasetURI)
                    .build();
            dataset.setMeta(datasetMeta);
            dataset.setVisible(Boolean.TRUE);
            dataset.setExistence(Dataset.DatasetExistence.DESCRIPTORSADDED);
            if (dataset.getDataEntry() == null || dataset.getDataEntry().isEmpty())
                throw new IllegalArgumentException("Resulting dataset is empty");
            else
                datasetLegacyWrapper.create(dataset);
            progress(100f, "Dataset saved successfully.");
            checkCancelled();
            progress("Calculation Task is now completed.");
            complete("dataset/" + dataset.getId());
        }
        catch (IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, "Preparation procedure execution error", ex);
            errInternalServerError(ex, "JPDI Preparation procedure error");
        }
        catch (BadRequestException ex) {
            errBadRequest(ex, "Error while processing input.");
            LOG.log(Level.SEVERE, null, ex);
        }
        catch (Exception ex) {
            LOG.log(Level.SEVERE, "JPDI Preparation procedure unknown error", ex);
            errInternalServerError(ex, "JPDI Preparation procedure unknown error");
        }
    }

    //Copies Features that were in the initial Dataset
    private void copyFeatures(@NotNull Dataset dataset, String datasetURI) throws JaqpotDocumentSizeExceededException {
        for (FeatureInfo featureInfo : dataset.getFeatures()) {
            Feature feature = featureHandler.find(featureInfo.getURI().split("feature/")[1]);
            if (feature==null)
                throw new NullPointerException("Feature with URI "+ featureInfo.getURI()+" was not found in the system");
            Feature f = FeatureBuilder.builder(feature)
                    .addDescriptions("Copy of "+feature.getId()+" feature")
                    .addSources(datasetURI).build();
            f.setId(new ROG(true).nextString(12));
            featureHandler.create(f);
            String featureURI = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE)+"feature/" + f.getId();

            //Update FeatureURIs in Data Entries
            for (DataEntry dataentry : dataset.getDataEntry()) {
                Object value = dataentry.getValues().remove(featureInfo.getURI());
                dataentry.getValues().put(featureURI,value);
            }
            featureInfo.setURI(featureURI);
        }
    }

    //Creates Features that were appended to the Dataset
    private void populateFeatures(@NotNull Dataset dataset, String datasetURI) throws JaqpotDocumentSizeExceededException {
        for (FeatureInfo featureInfo : dataset.getFeatures()) {
            String featureURI = null;
            Feature f = FeatureBuilder.builder(new ROG(true).nextString(12))
                    .addTitles(featureInfo.getURI())
                    .addIdentifiers(String.valueOf(featureInfo.getConditions().values()))
                    .addDescriptions(featureInfo.getName())
                    .addSources(datasetURI)
                    .build();
            featureHandler.create(f);
            featureURI = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE)+"feature/" + f.getId();

            //Update FeatureURIs in Data Entries
            for (DataEntry dataentry : dataset.getDataEntry()) {
                Object value = dataentry.getValues().remove(featureInfo.getURI());
                dataentry.getValues().put(featureURI,value);
            }
            //Update FeatureURI in Feature Info
            featureInfo.setConditions(null);
            featureInfo.setName(featureInfo.getURI());
            featureInfo.setURI(featureURI);
        }
    }
}
