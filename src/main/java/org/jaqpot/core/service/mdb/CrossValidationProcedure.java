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
import org.jaqpot.core.data.ReportHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.factory.DatasetFactory;
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
import javax.ws.rs.InternalServerErrorException;
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
 * @author Charalampos Chomenidis
 * @author Georgios Drakakis
 * @author Pantelis Sopasakis
 *
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/validationCross"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class CrossValidationProcedure extends AbstractJaqpotProcedure {

    private static final Logger LOG = Logger.getLogger(CrossValidationProcedure.class.getName());

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ReportHandler reportHandler;

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

    public CrossValidationProcedure() {
        super(null);
//        throw new IllegalStateException("Cannot use empty constructor, instantiate with TaskHandler");
    }

    @Inject
    public CrossValidationProcedure(TaskHandler taskHandler) {
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
        String algorithmURI = (String) messageBody.get("algorithm_uri");
        String datasetURI = (String) messageBody.get("dataset_uri");
        String predictionFeature = (String) messageBody.get("prediction_feature");
        String algorithmParams = (String) messageBody.get("algorithm_params");
        String trans = (String) messageBody.get("transformations");
        String creator = (String) messageBody.get("creator");

        Integer folds = (Integer) messageBody.get("folds");
        String stratify = (String) messageBody.get("stratify");
        Integer seed = (Integer) messageBody.get("seed");

        try {
            init(taskId);
            checkCancelled();
            start(Task.Type.VALIDATION);

            Algorithm algorithm = Optional.of(client.target(algorithmURI)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer "+ subjectId)
                    .get(Algorithm.class)).orElseThrow(() -> new NotFoundException("Algorithm with URI:" + algorithmURI + " was not found."));

            progress(5f, "Algorithm retrieved successfully.");
            checkCancelled();

            Dataset dataset = Optional.of(client.target(datasetURI)
                    .queryParam("stratify", stratify)
                    .queryParam("folds", folds)
                    .queryParam("seed", seed)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer "+subjectId)
                    .get(Dataset.class)).orElseThrow(() -> new NotFoundException("Dataset with URI:" + datasetURI + " was not found."));
            progress(10f, "Dataset retrieved successfully.");
            checkCancelled();

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
                    checkCancelled();
                    progress("-", "Starting transforming on algorithm:" + transAlgorithm.getId());

                    Map<String, Object> parameterMap = null;
                    String transParameters = transformations.get(transAlgorithm.getId());
                    if (transParameters != null && !transParameters.isEmpty()) {
                        parameterMap = serializer.parse(transParameters, new HashMap<String, Object>().getClass());
                    }
                    dataset = jpdiClient.transform(dataset, transAlgorithm, parameterMap, predictionFeature, dataset.getMeta(), taskId, subjectId).get();
                    addProgress(10f, "Done");
                }
                progress(30f, "Done processing transformations.", "--");
            }
            checkCancelled();

            Integer rows = dataset.getTotalRows();

            Integer minRows = rows / folds;
            Integer extras = rows % folds;

            Map<String, Object> parameterMap = null;
            if (algorithmParams != null && !algorithmParams.isEmpty()) {
                parameterMap = serializer.parse(algorithmParams, new HashMap<String, Object>().getClass());
            }

            Set<Dataset> partialDatasets = new HashSet<>();
            Dataset finalDataset = null;
            Integer i = 0, j = 0;
            while (i < rows) {
                Integer rowStart;
                Integer rowMax;
                if (j < extras) {
                    rowStart = i;
                    rowMax = minRows + 1;
                    i += rowMax;
                    j++;
                } else {
                    rowStart = i;
                    rowMax = minRows;
                    i += rowMax;
                }
                Dataset partialDataset = DatasetFactory.copy(dataset, rowStart, rowMax);
                partialDatasets.add(partialDataset);
            }
            progress(50f, "Created partial datasets.");
            checkCancelled();
            int p = 1;
            String predictedFeature = "";
            Integer indepFeatureSize = 0;
            for (Dataset predictionDataset : partialDatasets) {
                progress("Starting partial train and test:" + p++);
                Dataset trainingDataset = partialDatasets.stream()
                        .filter(d -> !d.getId().equals(predictionDataset.getId()))
                        .reduce(DatasetFactory.createEmpty(0),(a, b) -> DatasetFactory.mergeRows(a, b));
                        //.orElseThrow(() -> new InternalServerErrorException("Training dataset merging failed"));
                Model model = jpdiClient.train(trainingDataset, algorithm, parameterMap, predictionFeature, trainingDataset.getMeta(), taskId, subjectId).get();
                Dataset predictedDataset = jpdiClient.predict(predictionDataset, model, predictionDataset.getMeta(), taskId, subjectId).get();
                finalDataset = DatasetFactory.mergeRows(finalDataset, predictedDataset);
                predictedFeature = model.getPredictedFeatures().get(0);
                indepFeatureSize = Math.max(indepFeatureSize, model.getIndependentFeatures().size());
                addProgress(40f / folds, "Done");
                checkCancelled();
            }

            ValidationType validationType;
            if (algorithm.getOntologicalClasses().contains("ot:Regression")) {
                validationType = ValidationType.REGRESSION;
            } else if (algorithm.getOntologicalClasses().contains("ot:Classification")) {
                validationType = ValidationType.CLASSIFICATION;
            } else {
                throw new IllegalArgumentException("Selected Algorithm is neither Regression nor Classification.");
            }

            progress("Creating report...");
            checkCancelled();

            TrainingRequest reportRequest = new TrainingRequest();

            reportRequest.setDataset(finalDataset);
            reportRequest.setPredictionFeature(predictionFeature);
            Map<String, Object> validationParameters = new HashMap<>();
            validationParameters.put("predictionFeature", predictionFeature);
            validationParameters.put("predictedFeature", predictedFeature);
            validationParameters.put("variables", indepFeatureSize);
            validationParameters.put("type", validationType);
            reportRequest.setParameters(validationParameters);

            String validationBasePath = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_VALIDATION);
            Report report = client.target(validationBasePath)
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
                    .addTitles("Cross validation report")
                    .addCreators(creator)
                    .addSources(datasetURI, algorithmURI)
                    .addDescriptions(folds + " Fold cross validation on algorithm:" + algorithmURI + " with dataset:" + datasetURI)
                    .build());
            report.setVisible(Boolean.TRUE);
            reportHandler.create(report);
            complete("report/" + report.getId());

        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "Validation procedure interupted", ex);
            errInternalServerError(ex, "Validation procedure interupted");
        } catch (ExecutionException ex) {
            LOG.log(Level.SEVERE, "Validation procedure execution error", ex.getCause());
            errInternalServerError(ex.getCause(), "JPDI Training procedure error");
        } catch (CancellationException ex) {
            LOG.log(Level.INFO, "Task with id:{0} was cancelled - {1}", new Object[]{taskId, ex.getMessage()});
            cancel();
        } catch (NotFoundException ex) {
            errNotFound(ex);
        } catch (InternalServerErrorException ex) {
            LOG.log(Level.SEVERE, "Validation procedure execution error", ex.getCause());
            errInternalServerError(ex, "Validation procedure error");
        } catch (BadRequestException | IllegalArgumentException ex) {
            errBadRequest(ex, null);
        } catch (Exception ex) {
            errInternalServerError(ex, null);
        }
    }
}
