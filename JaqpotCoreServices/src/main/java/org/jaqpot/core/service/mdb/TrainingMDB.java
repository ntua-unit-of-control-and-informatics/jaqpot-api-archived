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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.dto.jpdi.TrainingResponse;
import org.jaqpot.core.model.util.AlgorithmOntologicalTypes;
import org.jaqpot.core.model.util.ROG;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/training"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class TrainingMDB extends RunningTaskMDB {

    private static final Logger LOG = Logger.getLogger(TrainingMDB.class.getName());

    @EJB
    TaskHandler taskHandler;

    @EJB
    FeatureHandler featureHandler;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ModelHandler modelHandler;

    @Inject
    @UnSecure
    Client client;

    long DOA_TASK_MAX_WAITING_TIME = 45; // 45s

    @Override
    public void onMessage(Message msg) {
        Task task = null;
        ROG randomStringGenerator = new ROG(true);
        String modelId = randomStringGenerator.nextString(12);

        try {
            Map<String, Object> messageBody = msg.getBody(Map.class);
            task = taskHandler.find(messageBody.get("taskId"));

            if (task == null) {
                throw new NullPointerException("FATAL: Could not find task with id:" + messageBody.get("taskId"));
            }
            if (task.getMeta() == null) {
                task.setMeta(MetaInfoBuilder.builder().setCurrentDate().build());
            }
            if (task.getMeta().getComments() == null) {
                task.getMeta().setComments(new ArrayList<>());
            }

            init(task.getId());

            task.setHttpStatus(202);
            task.setStatus(Task.Status.RUNNING);
            task.setType(Task.Type.TRAINING);
            task.getMeta().getComments().add("Training Task is now running.");
            task.setPercentageCompleted(10.f);
            taskHandler.edit(task);

            task.getMeta().getComments().add("Attempting to download dataset...");
            task.setPercentageCompleted(12.f);
            taskHandler.edit(task);
            Dataset dataset = client.target((String) messageBody.get("dataset_uri"))
                    .request()
                    .header("subjectid", messageBody.get("subjectid"))
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);
            dataset.setDatasetURI((String) messageBody.get("dataset_uri"));

            task.getMeta().getComments().add("Dataset has been retrieved.");

            task.getMeta().getComments().add("Creating JPDI training request...");
            task.setPercentageCompleted(20.f);
            taskHandler.edit(task);

            TrainingRequest trainingRequest = new TrainingRequest();
            trainingRequest.setDataset(dataset);
            task.getMeta().getComments().add("Inserted dataset.");
            task.setPercentageCompleted(34.f);
            taskHandler.edit(task);

            trainingRequest.setPredictionFeature((String) messageBody.get("prediction_feature"));
            task.getMeta().getComments().add("Inserted prediction feature.");
            task.setPercentageCompleted(41.f);
            taskHandler.edit(task);

            String parameters = (String) messageBody.get("parameters");
            Map<String, Object> parameterMap = new HashMap<>();
            if (parameters != null) {
                String[] parameterArray = parameters.split(",");
                for (String parameter : parameterArray) {
                    String[] keyValuePair = parameter.split("=");
                    parameterMap.put(keyValuePair[0].trim(), keyValuePair[1].trim());
                }
            }
            trainingRequest.setParameters(parameterMap);

            task.getMeta().getComments().add("Inserted parameters.");
            task.setPercentageCompleted(53.f);
            taskHandler.edit(task);

            String algorithmId = (String) messageBody.get("algorithmId");
            Algorithm algorithm = algorithmHandler.find(algorithmId);

            task.getMeta().getComments().add("Inserted algorithm id.");
            task.setPercentageCompleted(55.f);
            taskHandler.edit(task);

            task.getMeta().getComments().add("Sending request to  algorithm service:" + algorithm.getTrainingService());
            task.setPercentageCompleted(64.f);
            taskHandler.edit(task);
            Response response = client.target(algorithm.getTrainingService())
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(trainingRequest));
            task.getMeta().getComments().add("Algorithm service responded with status:" + response.getStatus());
            task.setPercentageCompleted(69.f);
            taskHandler.edit(task);

            task.getMeta().getComments().add("Attempting to parse response...");
            task.setPercentageCompleted(71.f);
            taskHandler.edit(task);
            TrainingResponse trainingResponse = response.readEntity(TrainingResponse.class);
            response.close();
            task.getMeta().getComments().add("Response was parsed successfully");
            task.setPercentageCompleted(77.f);
            taskHandler.edit(task);

            task.getMeta().getComments().add("Building model...");
            task.setPercentageCompleted(84.f);
            taskHandler.edit(task);

            
            Model model = new Model(modelId);
            model.setActualModel(trainingResponse.getRawModel());
            model.setPmmlModel(trainingResponse.getPmmlModel());
            model.setAlgorithm(algorithm);
            model.setIndependentFeatures(trainingResponse.getIndependentFeatures());
            model.setDatasetUri((String) messageBody.get("dataset_uri"));
            model.setAdditionalInfo(trainingResponse.getAdditionalInfo());
            ArrayList<String> dependentFeatures = new ArrayList<>();
            dependentFeatures.add(trainingRequest.getPredictionFeature());
            model.setDependentFeatures(dependentFeatures);
            ArrayList<String> predictedFeatures = new ArrayList<>();
            String predFeatID = randomStringGenerator.nextString(12);
            predictedFeatures.add(/* messageBody.get("base_uri") + */"feature/" + predFeatID);
            model.setPredictedFeatures(predictedFeatures);

            task.getMeta().getComments().add("Model was built successfully");
            task.getMeta().getComments().add("Defining the prediction feature");
            task.getMeta().getComments().add("Creating the prediction feature resource with ID "
                    + predFeatID);
            task.setPercentageCompleted(85.f);
            taskHandler.edit(task);

            // Create the prediction features (POST /feature)
            Feature predictionFeatureResource = new Feature();
            predictionFeatureResource.setId(predFeatID);
            predictionFeatureResource
                    .setPredictorFor(dependentFeatures.stream().findFirst().orElse(null));
            predictionFeatureResource.setCreatedBy(task.getCreatedBy());
            predictionFeatureResource.setMeta(MetaInfoBuilder
                    .builder()
                    .addSources(messageBody.get("base_uri") + "model/" + model.getId())
                    .addComments("Feature predicted by model with ID " + model.getId())
                    .addTitles("Prediction feature for model " + model.getId())
                    .addSeeAlso(dependentFeatures.toArray(new String[dependentFeatures.size()]))
                    .build());

            /* Create feature */
            featureHandler.create(predictionFeatureResource);

            task.getMeta().getComments().add("Feature created with ID " + predFeatID);
            task.setPercentageCompleted(85.f);
            taskHandler.edit(task);

            model.setMeta(MetaInfoBuilder
                    .builder()
                    .addCreators(task.getCreatedBy())
                    .addSources(dataset.getDatasetURI())
                    .addComments("Created by task " + task.getId())
                    .addDescriptions("QSAR model by algorithm " + algorithmId)
                    .build());


            /* Create DoA model by POSTing to the leverages algorithm */
            if ((!algorithm.getOntologicalClasses().contains(AlgorithmOntologicalTypes.ApplicabilityDomain.toString()))
                 && (!algorithm.getOntologicalClasses().contains(AlgorithmOntologicalTypes.ApplicabilityDomain.getURI()))) 
            {
                task.getMeta().getComments().add("Constructing DoA for this model...");
                taskHandler.edit(task);

                Form form = new Form();
                form.param("dataset_uri", (String) messageBody.get("dataset_uri"));
                form.param("prediction_feature", (String) messageBody.get("prediction_feature"));
                Task leverageTask = client.target("http://localhost:8080/jaqpot/services/algorithm/leverage")
                        .request()
                        .header("subjectid", messageBody.get("subjectid"))
                        .post(Entity.form(form)).readEntity(Task.class);
                task.getMeta().getComments().add("DoA to be created by task: " + leverageTask.getId());
                taskHandler.edit(task);

                int i_leverage_check = 0;
                while ((Task.Status.RUNNING == leverageTask.getStatus() || Task.Status.QUEUED == leverageTask.getStatus()) 
                        && i_leverage_check < DOA_TASK_MAX_WAITING_TIME) {
                    leverageTask = taskHandler.find(leverageTask.getId());
                    Thread.sleep(1000);
                    i_leverage_check++;
                }
                if (Task.Status.RUNNING == leverageTask.getStatus()) {
                    task.getMeta().getComments().add("DoA task is still running - check out its progress at "
                            + messageBody.get("base_uri") + "task/" + leverageTask.getId());
                }
                if (Task.Status.COMPLETED == leverageTask.getStatus()) {
                    task.getMeta().getComments().add("DoA model created - ID : " + leverageTask.getResult());
                    model.setDoaModel(leverageTask.getResultUri());
                }
                taskHandler.edit(task);
                //TODO link model to DoA

            }

            task.getMeta().getComments().add("Model was built successfully. Now saving to database...");
            taskHandler.edit(task);
            modelHandler.create(model);

            task.setResult("model/" + model.getId());
            task.setHttpStatus(201);
            task.setPercentageCompleted(100.f);
            task.setDuration(System.currentTimeMillis() - task.getMeta().getDate().getTime()); // in ms
            task.setStatus(Task.Status.COMPLETED);
            task.getMeta().getComments().add("Task Completed Successfully.");
            taskHandler.edit(task);

        } catch (UnsupportedOperationException | IllegalStateException | IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.badRequest(ex.getMessage(), "")); // Operation not supported
        } catch (ResponseProcessingException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.remoteError("", null)); //  Process response failed
        } catch (ProcessingException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.remoteError("", null)); // Process response runtime error
        } catch (WebApplicationException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), "")); // Application runtime error
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), ""));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), "")); // rest
        } finally {
            if (task != null) {
                terminate(task.getId());
            }
            taskHandler.edit(task);
        }
    }

}
