package org.jaqpot.core.service.mdb;

import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.*;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.data.wrappers.DatasetLegacyWrapper;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.Secure;
import org.jaqpot.core.service.client.jpdi.JPDIClient;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Angelos Valsamis
 * @author Charalampos Chomenidis
 * @author Georgios Drakakis
 * @author Pantelis Sopasakis
 *
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/validationExternal"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class ExternalValidationProcedure extends AbstractJaqpotProcedure {

    private static final Logger LOG = Logger.getLogger(ExternalValidationProcedure.class.getName());

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ModelHandler modelHandler;

    @EJB
    ReportHandler reportHandler;

    @EJB
    DatasetLegacyWrapper datasetLegacyWrapper;

    @Inject
    @Jackson
    JSONSerializer serializer;

    @Inject
    JPDIClient jpdiClient;

    @Inject
    PropertyManager propertyManager;

    @Inject
    @Secure
    Client client;

    public ExternalValidationProcedure() {
        super(null);
//        throw new IllegalStateException("Cannot use empty constructor, instantiate with TaskHandler");
    }

    @Inject
    public ExternalValidationProcedure(TaskHandler taskHandler) {
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
        String model_uri = (String) messageBody.get("model_uri");
        String apiKey = (String) messageBody.get("api_key");
        String creator = (String) messageBody.get("creator");

        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.VALIDATION);

            progress(5f, "External Validation Task is now running.");

            Model model = modelHandler.find(model_uri.split("model/")[1]);
            if (model == null) {
                errNotFound("Model with id:" + model_uri.split("model/")[1] + " was not found.");
                return;
            }
            progress(10f, "Model retrieved successfully.");
            checkCancelled();

            Dataset dataset = null;
            if (dataset_uri != null && !dataset_uri.isEmpty()) {
                progress("Attempting to download dataset...");
                try{
                    dataset = client.target(dataset_uri)
                        .queryParam("dataEntries", true)
                        .request()
                        .header("Authorization", "Bearer " + apiKey)
                        .accept(MediaType.APPLICATION_JSON)
                        .get(Dataset.class);
                }catch(NotFoundException e){
                    String[] splitted = dataset_uri.split("/");
                    dataset = datasetLegacyWrapper.find(splitted[splitted.length -1]);
                    //dataset = datasetHandler.find(splitted[splitted.length -1]);
                }
                dataset.setDatasetURI(dataset_uri);
            }
            progress(10f, "Dataset retrieved successfully.");
            checkCancelled();

            if (model.getTransformationModels() != null && !model.getTransformationModels().isEmpty()) {
                progress("--", "Processing transformations...");
                for (String transModelURI : model.getTransformationModels()) {
                    checkCancelled();
                    Model transModel = modelHandler.find(transModelURI.split("model/")[1]);
                    if (transModel == null) {
                        errNotFound("Transformation modle with id:" + transModelURI + " was not found.");
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

            ValidationType validationType;
            if (model.getAlgorithm().getOntologicalClasses().contains("ot:Regression")) {
                validationType = ValidationType.REGRESSION;
            } else if (model.getAlgorithm().getOntologicalClasses().contains("ot:Classification")) {
                validationType = ValidationType.CLASSIFICATION;
            } else {
                throw new IllegalArgumentException("Selected Algorithm is neither Regression nor Classification.");
            }
            Integer indepFeatureSize = 0;
            indepFeatureSize = Math.max(indepFeatureSize, model.getIndependentFeatures().size());

            TrainingRequest reportRequest = new TrainingRequest();
            Map<String, Object> validationParameters = new HashMap<>();
            reportRequest.setDataset(dataset);
            reportRequest.setPredictionFeature(model.getDependentFeatures().get(0));
            validationParameters.put("predictionFeature", model.getDependentFeatures().get(0));
            validationParameters.put("predictedFeature", model.getPredictedFeatures().get(0));
            validationParameters.put("variables", indepFeatureSize);
            validationParameters.put("type", validationType);
            reportRequest.setParameters(validationParameters);

            progress(92f, "Validation info populated successfully");
            checkCancelled();

            Report report = client.target(propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_VALIDATION))
                    .request()
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(reportRequest), Report.class);

            progress(95f, "Done", "Saving report to database...");
            checkCancelled();

            ROG randomStringGenerator = new ROG(true);
            String reportId = randomStringGenerator.nextString(15);
            report.setId(reportId);
            report.setMeta(MetaInfoBuilder
                    .builder()
                    .addTitles("External validation report")
                    .addSources(dataset_uri, model_uri)
                    .addDescriptions("External validation with model:" + model_uri + " and dataset:" + dataset_uri)
                    .build());
            report.setVisible(Boolean.TRUE);
            reportHandler.create(report);
            complete("report/" + report.getId());

        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "JPDI Validation procedure interupted", ex);
            errInternalServerError(ex, "JPDI Validation procedure interupted");
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Validation procedure execution error", ex.getCause());
            errInternalServerError(ex.getCause(), "JPDI Validation procedure error");
        } catch (CancellationException ex) {
            LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
            cancel();
        } catch (BadRequestException | IllegalArgumentException ex) {
            errBadRequest(ex, null);
        } catch (NotFoundException ex) {
            errNotFound(ex);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "JPDI Validation procedure unknown error", ex);
            errInternalServerError(ex, "JPDI Validation procedure unknown error");

        }
    }
}
