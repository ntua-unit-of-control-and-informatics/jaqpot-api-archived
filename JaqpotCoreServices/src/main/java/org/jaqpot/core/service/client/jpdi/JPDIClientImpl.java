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
package org.jaqpot.core.service.client.jpdi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Report;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.dto.jpdi.PredictionRequest;
import org.jaqpot.core.model.dto.jpdi.PredictionResponse;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.dto.jpdi.TrainingResponse;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.util.ROG;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
public class JPDIClientImpl implements JPDIClient {

    private static final Logger LOG = Logger.getLogger(JPDIClientImpl.class.getName());

//    private final Client client;
    private final CloseableHttpAsyncClient client;
    private final JSONSerializer serializer;
    private final FeatureHandler featureHandler;
    private final String baseURI;
    private final ROG randomStringGenerator;

    private final Map<String, Future> futureMap;

    public JPDIClientImpl(CloseableHttpAsyncClient client, JSONSerializer serializer, FeatureHandler featureHandler, String baseURI) {
        this.client = client;
        client.start();
        this.serializer = serializer;
        this.featureHandler = featureHandler;
        this.baseURI = baseURI;
        this.futureMap = new ConcurrentHashMap<>(20);
        this.randomStringGenerator = new ROG(true);
    }

    @Override
    public Future<Model> train(Dataset dataset, Algorithm algorithm, Map<String, Object> parameters, String predictionFeature, MetaInfo modelMeta, String taskId) {

        CompletableFuture<Model> futureModel = new CompletableFuture<>();

        TrainingRequest trainingRequest = new TrainingRequest();
        trainingRequest.setDataset(dataset);
        trainingRequest.setParameters(parameters);
        trainingRequest.setPredictionFeature(predictionFeature);
//        String trainingRequestString = serializer.write(trainingRequest);

        final HttpPost request = new HttpPost(algorithm.getTrainingService());
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");

        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in;
        try {
            in = new PipedInputStream(out);
        } catch (IOException ex) {
            futureModel.completeExceptionally(ex);
            return futureModel;
        }
        request.setEntity(new InputStreamEntity(in, ContentType.APPLICATION_JSON));

        Future futureResponse = client.execute(request, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(final HttpResponse response) {
                futureMap.remove(taskId);
                int status = response.getStatusLine().getStatusCode();
                try {
                    InputStream responseStream = response.getEntity().getContent();

                    switch (status) {
                        case 200:
                        case 201:
                            TrainingResponse trainingResponse = serializer.parse(responseStream, TrainingResponse.class);
                            Model model = new Model();
                            model.setId(randomStringGenerator.nextString(20));
                            model.setActualModel(trainingResponse.getRawModel());
                            model.setPmmlModel(trainingResponse.getPmmlModel());
                            model.setAdditionalInfo(trainingResponse.getAdditionalInfo());
                            model.setAlgorithm(algorithm);
                            model.setParameters(parameters);
//                            model.setPredictedFeatures(trainingResponse.getPredictedFeatures());
                            model.setIndependentFeatures(trainingResponse.getIndependentFeatures());
                            model.setDependentFeatures(Arrays.asList(predictionFeature));
                            model.setMeta(modelMeta);

                            List<String> predictedFeatures = new ArrayList<>();
                            for (String featureTitle : trainingResponse.getPredictedFeatures()) {
                                Feature predictionFeatureResource = featureHandler.findByTitleAndSource(featureTitle, "algorithm/" + algorithm.getId());
                                if (predictionFeatureResource == null) {
                                    // Create the prediction features (POST /feature)
                                    String predFeatID = randomStringGenerator.nextString(12);
                                    predictionFeatureResource = new Feature();
                                    predictionFeatureResource.setId(predFeatID);
                                    predictionFeatureResource
                                            .setPredictorFor(predictionFeature);
                                    predictionFeatureResource.setMeta(MetaInfoBuilder
                                            .builder()
                                            .addSources(/*messageBody.get("base_uri") + */"algorithm/" + algorithm.getId())
                                            .addComments("Feature created to hold predictions by algorithm with ID " + algorithm.getId())
                                            .addTitles(featureTitle)
                                            .addSeeAlso(predictionFeature)
                                            .addCreators(algorithm.getMeta().getCreators())
                                            .build());
                                    /* Create feature */
                                    featureHandler.create(predictionFeatureResource);
                                }
                                predictedFeatures.add(baseURI + "feature/" + predictionFeatureResource.getId());
                            }
                            model.setPredictedFeatures(predictedFeatures);
                            futureModel.complete(model);
                            break;
                        case 400:
                            String message = new BufferedReader(new InputStreamReader(responseStream))
                                    .lines().collect(Collectors.joining("\n"));
                            futureModel.completeExceptionally(new BadRequestException(message));
                            break;
                        case 500:
                            message = new BufferedReader(new InputStreamReader(responseStream))
                                    .lines().collect(Collectors.joining("\n"));
                            futureModel.completeExceptionally(new InternalServerErrorException(message));
                            break;
                        default:
                            message = new BufferedReader(new InputStreamReader(responseStream))
                                    .lines().collect(Collectors.joining("\n"));
                            futureModel.completeExceptionally(new InternalServerErrorException(message));
                    }
                } catch (IOException | UnsupportedOperationException ex) {
                    futureModel.completeExceptionally(ex);
                }
            }

            @Override
            public void failed(final Exception ex) {
                futureMap.remove(taskId);
                futureModel.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                futureMap.remove(taskId);
                futureModel.cancel(true);
            }

        });

        serializer.write(trainingRequest, out);
        try {
            out.close();
            in.close();
        } catch (IOException ex) {
            futureModel.completeExceptionally(ex);
        }

        futureMap.put(taskId, futureResponse);
        return futureModel;
    }

    @Override
    public Future<Dataset> predict(Dataset dataset, Model model, MetaInfo datasetMeta, String taskId) {

        CompletableFuture<Dataset> futureDataset = new CompletableFuture<>();

        PredictionRequest predictionRequest = new PredictionRequest();
        predictionRequest.setDataset(dataset);
        predictionRequest.setRawModel(model.getActualModel());
        predictionRequest.setAdditionalInfo(model.getAdditionalInfo());

        final HttpPost request = new HttpPost(model.getAlgorithm().getPredictionService());
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");

        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in;
        try {
            in = new PipedInputStream(out);
        } catch (IOException ex) {
            futureDataset.completeExceptionally(ex);
            return futureDataset;
        }
        request.setEntity(new InputStreamEntity(in, ContentType.APPLICATION_JSON));

        Future futureResponse = client.execute(request, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(final HttpResponse response) {
                futureMap.remove(taskId);
                int status = response.getStatusLine().getStatusCode();
                try {
                    InputStream responseStream = response.getEntity().getContent();

                    switch (status) {
                        case 200:
                        case 201:
                            PredictionResponse predictionResponse = serializer.parse(responseStream, PredictionResponse.class);

                            List<Map<String, Object>> predictions = predictionResponse.getPredictions();
                            if (dataset.getDataEntry().isEmpty()) {
                                DatasetFactory.addEmptyRows(dataset, predictions.size());
                            }
                            IntStream.range(0, dataset.getDataEntry().size())
                                    .parallel()
                                    .forEach(i -> {
                                        Map<String, Object> row = predictions.get(i);
                                        DataEntry dataEntry = dataset.getDataEntry().get(i);
                                        if (model.getAlgorithm().getOntologicalClasses().contains("ot:Scaling")
                                                || model.getAlgorithm().getOntologicalClasses().contains("ot:Transformation")) {
                                            dataEntry.getValues().clear();
                                            dataset.getFeatures().clear();
                                        }
                                        row.entrySet()
                                                .stream()
                                                .forEach(entry -> {
                                                    Feature feature = featureHandler.findByTitleAndSource(entry.getKey(), "algorithm/" + model.getAlgorithm().getId());
                                                    if (feature == null) {
                                                        return;
                                                    }
                                                    dataEntry.getValues().put(baseURI + "feature/" + feature.getId(), entry.getValue());
                                                    FeatureInfo featInfo = new FeatureInfo(baseURI + "feature/" + feature.getId(), feature.getMeta().getTitles().stream().findFirst().get());
                                                    featInfo.setCategory(Dataset.DescriptorCategory.PREDICTED);
                                                    dataset.getFeatures().add(featInfo);
                                                });
                                    });
                            futureDataset.complete(dataset);
                            break;
                        case 400:
                            String message = new BufferedReader(new InputStreamReader(responseStream))
                                    .lines().collect(Collectors.joining("\n"));
                            futureDataset.completeExceptionally(new BadRequestException(message));
                            break;
                        case 500:
                            message = new BufferedReader(new InputStreamReader(responseStream))
                                    .lines().collect(Collectors.joining("\n"));
                            futureDataset.completeExceptionally(new InternalServerErrorException(message));
                            break;
                        default:
                            message = new BufferedReader(new InputStreamReader(responseStream))
                                    .lines().collect(Collectors.joining("\n"));
                            futureDataset.completeExceptionally(new InternalServerErrorException(message));
                    }
                } catch (IOException | UnsupportedOperationException ex) {
                    futureDataset.completeExceptionally(ex);
                }
            }

            @Override
            public void failed(final Exception ex) {
                futureMap.remove(taskId);
                futureDataset.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                futureMap.remove(taskId);
                futureDataset.cancel(true);
                LOG.log(Level.INFO, "Task with id:{0} was cancelled.", taskId);
            }
        });
        serializer.write(predictionRequest, out);
        futureMap.put(taskId, futureResponse);
        return futureDataset;
    }

    @Override
    public Future<Dataset> transform(Dataset dataset, Algorithm algorithm, Map<String, Object> parameters, String predictionFeature, MetaInfo datasetMeta, String taskId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Future<Report> report(Dataset dataset, Algorithm algorithm, Map<String, Object> parameters, MetaInfo reportMeta, String taskId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean cancel(String taskId) {
        Future future = futureMap.get(taskId);
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
            return true;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

}
