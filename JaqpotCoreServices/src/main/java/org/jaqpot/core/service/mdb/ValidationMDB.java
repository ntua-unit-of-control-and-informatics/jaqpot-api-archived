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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.data.ValidationService;
import org.jaqpot.core.service.exceptions.JaqpotWebException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/validation"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class ValidationMDB extends RunningTaskMDB {

    private static final Logger LOG = Logger.getLogger(ValidationMDB.class.getName());

    @EJB
    TaskHandler taskHandler;

    @EJB
    ValidationService validationService;

    @Inject
    @UnSecure
    Client client;

    @Override
    public void onMessage(Message msg) {
        Task task = new Task();
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
            task.getMeta().getComments().add("Validation Task is now running.");
            task.setPercentageCompleted(10.f);
            taskHandler.edit(task);

            String type = (String) messageBody.get("type");
            String subjectId = (String) messageBody.get("subjectId");
            String algorithmURI = (String) messageBody.get("algorithm_uri");
            String datasetURI = (String) messageBody.get("dataset_uri");
            String predictionFeature = (String) messageBody.get("prediction_feature");
            String algorithmParams = (String) messageBody.get("algorithm_params");

            switch (type) {
                case "SPLIT":
                    task.getMeta().getComments().add("Validation mode is SPLIT.");
                    taskHandler.edit(task);

                    Double splitRatio = (Double) messageBody.get("split_ratio");
                    Dataset dataset = client.target(datasetURI)
                            .request()
                            .accept(MediaType.APPLICATION_JSON)
                            .header("subjectId", subjectId)
                            .get(Dataset.class);
                    Integer rows = dataset.getTotalRows();

                    Long split = Math.round(rows * splitRatio);
                    String trainDatasetURI = datasetURI + "?rowStart=0&rowMax=" + split;
                    String testDatasetURI = datasetURI + "?rowStart=" + split + "&rowMax=" + (rows - split);

                    String finalDatasetURI = validationService.trainAndTest(algorithmURI, trainDatasetURI, testDatasetURI, predictionFeature, algorithmParams, subjectId);

                    task.getMeta().getComments().add("Final dataset is:" + finalDatasetURI);
                    taskHandler.edit(task);

                    break;
                case "CROSS":

                    task.getMeta().getComments().add("Validation mode is CROSS.");
                    taskHandler.edit(task);

                    Integer folds = (Integer) messageBody.get("folds");
                    String stratify = (String) messageBody.get("stratify");
                    Integer seed = (Integer) messageBody.get("seed");
                    dataset = client.target(datasetURI)
                            .request()
                            .accept(MediaType.APPLICATION_JSON)
                            .header("subjectId", subjectId)
                            .get(Dataset.class);
                    rows = dataset.getTotalRows();
                    Integer foldSize = Math.round((rows + folds - 1) / folds);
                    List<String> partialDatasets = new ArrayList<>();
                    for (int i = 0; i < folds; i++) {
                        Integer rowStart = i * foldSize;
                        Integer rowMax = foldSize;
                        if (rowStart + rowMax > rows) {
                            rowMax = rows - rowStart;
                            String partialDatasetURI = datasetURI + "?rowStart=" + rowStart + "&rowMax=" + rowMax + (stratify != null ? "&stratify=" + stratify : "") + (folds != null ? "&folds=" + folds.toString() : "") + (seed != null ? "&seed=" + seed.toString() : "") + "&target_feature=" + URLEncoder.encode(predictionFeature, "UTF-8");
                            partialDatasets.add(partialDatasetURI);
                            break;
                        }
                        String partialDatasetURI = datasetURI + "?rowStart=" + rowStart + "&rowMax=" + rowMax + (stratify != null ? "&stratify=" + stratify : "") + (folds != null ? "&folds=" + folds.toString() : "") + (seed != null ? "&seed=" + seed.toString() : "") + "&target_feature=" + URLEncoder.encode(predictionFeature, "UTF-8");
                        partialDatasets.add(partialDatasetURI);
                    }
                    List<String> finalDatasets = new ArrayList<>();
                    for (String testDataset : partialDatasets) {
                        String trainDatasets = partialDatasets.stream()
                                .filter(d -> !d.equals(testDataset))
                                .collect(Collectors.joining(","));
                        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
                        params.add("dataset_uris", trainDatasets);
                        String trainDataset = client.target(datasetURI.split("dataset")[0] + "dataset/merge")
                                .request()
                                .accept("text/uri-list")
                                .header("subjectId", subjectId)
                                .post(Entity.form(params), String.class);

                        String finalSubDataset = validationService.trainAndTest(algorithmURI, trainDataset, testDataset, predictionFeature, algorithmParams, subjectId);
                        finalDatasets.add(finalSubDataset);
                    }

                    MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
                    params.add("dataset_uris", finalDatasets.stream().collect(Collectors.joining(",")));
                    String finalDataset = client.target(datasetURI.split("dataset")[0] + "dataset/merge")
                            .request()
                            .accept("text/uri-list")
                            .header("subjectId", subjectId)
                            .post(Entity.form(params), String.class);

                    task.getMeta().getComments().add("Final dataset is:" + finalDataset);
                    taskHandler.edit(task);

                    break;

                default:
                    break;
            }

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
        } catch (NullPointerException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "", ex.getMessage(), "")); // rest
        } catch (JaqpotWebException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ex.getError());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ValidationMDB.class.getName()).log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError("", ex.getMessage(), ""));
        } finally {
            if (task != null) {
                terminate(task.getId());
            }
            taskHandler.edit(task);
        }

    }

}
