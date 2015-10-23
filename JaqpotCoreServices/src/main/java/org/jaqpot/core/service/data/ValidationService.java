/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
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
package org.jaqpot.core.service.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.ValidationReport;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.exceptions.JaqpotWebException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Stateless
public class ValidationService {

    @EJB
    TaskHandler taskHandler;

    @EJB
    ModelHandler modelHandler;

    @Inject
    @UnSecure
    Client client;

    public Object[] trainAndTest(String algorithmURI, String trainingDataset, String testingDataset, String predictionFeature, String algorithmParameters, String subjectId) throws JaqpotWebException {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.add("dataset_uri", trainingDataset);
        params.add("prediction_feature", predictionFeature);
        params.add("parameters", algorithmParameters);
        Task trainTask = client.target(algorithmURI)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("subjectid", subjectId)
                .post(Entity.form(params), Task.class);
        String trainTaskURI = algorithmURI.split("algorithm")[0] + "task/" + trainTask.getId();
        while (trainTask.getStatus().equals(Task.Status.RUNNING)
                || trainTask.getStatus().equals(Task.Status.QUEUED)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {

            }
            trainTask = client.target(trainTaskURI)
                    .request()
                    .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Task.class);
        }
        if (!trainTask.getStatus().equals(Task.Status.COMPLETED)) {
            throw new JaqpotWebException(trainTask.getErrorReport());
        }
        String modelURI = trainTask.getResultUri();
        params.clear();
        params.add("dataset_uri", testingDataset);
        Task predictionTask = client.target(trainTask.getResultUri())
                .request()
                .header("subjectid", subjectId)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.form(params), Task.class);
        String predictionTaskURI = modelURI.split("model")[0] + "task/" + predictionTask.getId();
        while (predictionTask.getStatus().equals(Task.Status.RUNNING)
                || predictionTask.getStatus().equals(Task.Status.QUEUED)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {

            }
            predictionTask = client.target(predictionTaskURI)
                    .request()
                    .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Task.class);
        }
        if (!predictionTask.getStatus().equals(Task.Status.COMPLETED)) {
            throw new JaqpotWebException(predictionTask.getErrorReport());

        }
        Model model = client.target(modelURI)
                .request()
                .header("subjectid", subjectId)
                .accept(MediaType.APPLICATION_JSON)
                .get(Model.class);
        Object[] result = new Object[5];
        result[0] = predictionTask.getResultUri();
        result[1] = predictionFeature;
        result[2] = model.getPredictedFeatures().get(0);
        result[3] = model.getIndependentFeatures().size();
        return result;
    }

    public ValidationReport createValidationReport(String datasetURI, String predictionFeature, String predictedFeature, Integer indepFeaturesSize, ValidationReport.Type type, String subjectId) {
        Dataset dataset = client.target(datasetURI)
                .request()
                .header("subjectid", subjectId)
                .accept(MediaType.APPLICATION_JSON)
                .get(Dataset.class);

        List<Double> original = dataset.getDataEntry().stream().map(de -> Double.parseDouble(de.getValues().get(predictionFeature).toString())).collect(Collectors.toList());
        List<Double> predictions = dataset.getDataEntry().stream().map(de -> Double.parseDouble(de.getValues().get(predictedFeature).toString())).collect(Collectors.toList());

        Double mean = original.stream().collect(Collectors.averagingDouble(Double::doubleValue));
        Double predictedMean = predictions.stream().collect(Collectors.averagingDouble(Double::doubleValue));
        Double n = ((Integer) dataset.getDataEntry().size()).doubleValue();
        Double p = indepFeaturesSize.doubleValue();

        Double SSx = original.stream().mapToDouble(y -> {
            return Math.pow(y - mean, 2);
        }).sum(); //collect(Collectors.summingDouble(Double::doubleValue));

        Double SSy = original.stream().mapToDouble(y -> {
            return Math.pow(y - predictedMean, 2);
        }).sum(); //collect(Collectors.summingDouble(Double::doubleValue));

        Double SSxy = IntStream.range(0, original.size())
                .mapToDouble(i -> {
                    Double yObs = original.get(i);
                    Double yCalc = predictions.get(i);
                    return (yObs - mean) * (yCalc - predictedMean);
                }).sum();//.collect(Collectors.summingDouble(Double::doubleValue));

        Double R2;

        if (SSx == 0 || SSy == 0) {
            R2 = 0.0;
        } else {
            R2 = Math.pow(SSxy, 2) / (SSx * SSy);
        }

//        Double SSt = original.stream().mapToDouble(y -> {
//            return Math.pow(y - mean, 2);
//        }).sum(); //collect(Collectors.summingDouble(Double::doubleValue));
//
//        Double SSreg = predictions.stream().mapToDouble(y -> {
//            return Math.pow(y - mean, 2);
//        }).sum();//.collect(Collectors.summingDouble(Double::doubleValue));
//
//        Double SSres = IntStream.range(0, original.size())
//                .mapToDouble(i -> {
//                    Double yObs = original.get(i);
//                    Double yCalc = predictions.get(i);
//                    return Math.pow(yObs - yCalc, 2);
//                }).sum();//.collect(Collectors.summingDouble(Double::doubleValue));
//
//        Double R2 = 1 - (SSres / SSt);
//
        Double R2Adj = 1 - ((1 - R2) * ((n - 1) / (n - p - 1)));
//
//        Double stdErrorEstimate = Math.sqrt(SSres / (n - p - 1));
//
//        Double EMS = SSreg / p;
//        Double RMS = SSres / (n - p - 1);
//        Double fValue = EMS / RMS;
        ValidationReport report = new ValidationReport();
        Map<String, Object> calculations = new HashMap<>();
        calculations.put("R^2", R2);
        calculations.put("Adjusted R^2", R2Adj);
//        calculations.put("Standard error of estimate", stdErrorEstimate);
//        calculations.put("F-value", fValue);

        report.setCalculations(calculations);
        report.setType(type);
        return report;
    }
}
