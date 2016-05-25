package org.jaqpot.core.service.mdb;

import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.*;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.util.ROG;
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
public class ExternalValidationProcedure extends AbstractJaqpotProcedure{
    private static final Logger LOG = Logger.getLogger(SplitValidationProcedure.class.getName());

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ModelHandler modelHandler;

    @EJB
    ReportHandler reportHandler;

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
        Report report = null;

        Map<String, Object> messageBody;
        try {
            messageBody = msg.getBody(Map.class);
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "JMS message could not be read", ex);
            return;
        }

        String taskId = (String) messageBody.get("taskId");
        String dataset_uri = (String) messageBody.get("dataset_uri");
        String predictionFeature = (String) messageBody.get("prediction_feature");

        String algorithmParams = (String) messageBody.get("algorithm_params");
        String subjectId = (String) messageBody.get("subjectId");

        String creator = (String) messageBody.get("creator");
        String trans = (String) messageBody.get("transformations");
        String algorithmURI = (String) messageBody.get("algorithm_uri");


        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.PREDICTION);

            progress(5f, " Task is now running.");


            init(taskId);
            checkCancelled();
            start(Task.Type.VALIDATION);

            Algorithm algorithm = Optional.of(client.target(algorithmURI)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("subjectId", subjectId)
                    .get(Algorithm.class)).orElseThrow(() -> new NotFoundException("Algorithm with URI:" + algorithmURI + " was not found."));

            progress(10f, "Algorithm retrieved successfully.");

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


            LinkedHashMap<String, String> transformations = new LinkedHashMap<>();
            List<Algorithm> transformationAlgorithms = new ArrayList<>();
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
                    }
                });
                transformations.putAll(newTransformations);
                for (Algorithm transAlgorithm : transformationAlgorithms) {
                    progress("-", "Starting transforming on algorithm:" + transAlgorithm.getId());

                    Map<String, Object> parameterMap = null;
                    String transParameters = transformations.get(transAlgorithm.getId());
                    if (transParameters != null && !transParameters.isEmpty()) {
                        parameterMap = serializer.parse(transParameters, new HashMap<String, Object>().getClass());
                    }
                    dataset = jpdiClient.transform(dataset, transAlgorithm, parameterMap, predictionFeature, dataset.getMeta(), taskId).get();
                    addProgress(10f, "Done");
                }
                progress(30f, "Done processing transformations.", "--");
            }
            if (dataset == null) return;

            progress(50f);

            MetaInfo datasetMeta = dataset.getMeta();
            HashSet<String> creators = new HashSet<>(Arrays.asList(creator));
            datasetMeta.setCreators(creators);

            progress("Starting JPDI Prediction...");

            Map<String, Object> parameterMap = null;
            if (algorithmParams != null && !algorithmParams.isEmpty()) {
                parameterMap = serializer.parse(algorithmParams, new HashMap<String, Object>().getClass());
            }

            Model model = jpdiClient.train(dataset, algorithm, parameterMap, predictionFeature, dataset.getMeta(), taskId).get();

            dataset = jpdiClient.predict(dataset, model, datasetMeta, taskId).get();
            progress("JPDI Prediction completed successfully.");
            progress(80f, "Dataset was built successfully.");

            progress(90f, "Now saving to database...");
            dataset.setVisible(Boolean.TRUE);
            dataset.setFeatured(Boolean.FALSE);
            datasetHandler.create(dataset);
            complete("dataset/" + dataset.getId());

            String predictedFeature = "";
            Integer indepFeatureSize = 0;
            indepFeatureSize = Math.max(indepFeatureSize, model.getIndependentFeatures().size());

            TrainingRequest reportRequest = new TrainingRequest();
            reportRequest.setDataset(dataset);
            reportRequest.setPredictionFeature(predictionFeature);
            Map<String, Object> validationParameters = new HashMap<>();
            validationParameters.put("predictionFeature", predictionFeature);
            predictedFeature = model.getPredictedFeatures().get(0);
            validationParameters.put("predictedFeature", predictedFeature);
            validationParameters.put("variables", indepFeatureSize);

            ValidationType validationType;
            if (algorithm.getOntologicalClasses().contains("ot:Regression")) {
                validationType = ValidationType.REGRESSION;
            } else if (algorithm.getOntologicalClasses().contains("ot:Classification")) {
                validationType = ValidationType.CLASSIFICATION;
            } else {
                throw new IllegalArgumentException("Selected Algorithm is neither Regression nor Classification.");
            }

            validationParameters.put("type", validationType);
            reportRequest.setParameters(validationParameters);

            report = client.target(ResourceBundle.getBundle("config").getString("ValidationBasePath"))
                    .request()
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(reportRequest), Report.class);
            progress(90f, "Done", "Saving report to database...");
            checkCancelled();

            ROG randomStringGenerator = new ROG(true);
            String reportId = randomStringGenerator.nextString(15);
            report.setId(reportId);
            report.setMeta(MetaInfoBuilder
                    .builder()
                    .addTitles("External validation report")
                    .addCreators(creator)
                    .addSources(dataset_uri, algorithmURI)
                    .addDescriptions("External validation on algorithm:" + algorithmURI + " with dataset:" + dataset_uri)
                    .build());
            report.setVisible(Boolean.TRUE);
            reportHandler.create(report);
            complete("report/" + report.getId());


        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "JPDI Training procedure interupted", ex);
            errInternalServerError(ex, "JPDI Training procedure interupted");
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
            errInternalServerError(ex.getCause(), "JPDI Training procedure error");
        } catch (CancellationException ex) {
            LOG.log(Level.INFO, "Task with id:{0} was cancelled", taskId);
            cancel();
        } catch (BadRequestException | IllegalArgumentException ex) {
            errBadRequest(ex, null);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "JPDI Prediction procedure unknown error", ex);
            errInternalServerError(ex, "JPDI Prediction procedure unknown error");
        }
    }
}
