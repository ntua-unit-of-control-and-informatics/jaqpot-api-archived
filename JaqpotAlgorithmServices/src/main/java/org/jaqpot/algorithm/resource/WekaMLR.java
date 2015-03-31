/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.algorithm.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.stream.StreamResult;
import org.dmg.pmml.Application;
import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.Header;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.NumericPredictor;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.RegressionModel;
import org.dmg.pmml.RegressionTable;
import org.dmg.pmml.Timestamp;
import org.jaqpot.algorithm.model.LeverageModel;
import org.jaqpot.algorithm.model.WekaModel;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.jpdi.PredictionRequest;
import org.jaqpot.core.model.dto.jpdi.PredictionResponse;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.dto.jpdi.TrainingResponse;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jpmml.model.JAXBUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author hampos
 */
@Path("mlr")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WekaMLR {

    @POST
    @Path("training")
    public Response training(TrainingRequest request) {

        try {
            List<Attribute> attributes = request.getDataset().getDataEntry()
                    .stream()
                    .findFirst()
                    .get()
                    .getValues()
                    .keySet()
                    .stream()
                    .map(feature -> {
                        return new Attribute(feature);
                    }).collect(Collectors.toList());

            Instances data = new Instances("TrainingDataset", new ArrayList<>(attributes), request.getDataset().getDataEntry().size());
            data.setClass(data.attribute(request.getPredictionFeature()));

            for (DataEntry dataEntry : request.getDataset().getDataEntry()) {
                Instance instance = new DenseInstance(dataEntry.getValues().size());
                dataEntry.getValues().entrySet().stream().forEach(entry -> {
                    instance.setValue(data.attribute(entry.getKey()), Double.parseDouble(entry.getValue().toString()));
                });
                data.add(instance);
            }

            LinearRegression linreg = new LinearRegression();
            String[] linRegOptions = {"-S", "1", "-C"};
            linreg.setOptions(linRegOptions);
            linreg.buildClassifier(data);

            RegressionModel regressionModel = new RegressionModel();
            regressionModel.setModelName("MLR Model");
            regressionModel.setFunctionName(MiningFunctionType.REGRESSION);
            regressionModel.setAlgorithmName("MLR");

            List<MiningField> miningFields = request.getDataset()
                    .getDataEntry()
                    .get(0)
                    .getValues()
                    .keySet()
                    .stream()
                    .map(feature -> {
                        MiningField miningField = new MiningField();
                        miningField.setName(new FieldName(feature));
                        if (feature.equals(request.getPredictionFeature())) {
                            miningField.setUsageType(FieldUsageType.PREDICTED);
                        } else {
                            miningField.setUsageType(FieldUsageType.ACTIVE);
                        }
                        return miningField;
                    })
                    .collect(Collectors.toList());
            regressionModel.setMiningSchema(new MiningSchema(miningFields));
            regressionModel.setOutput(new Output(Arrays.asList(new OutputField(new FieldName(request.getPredictionFeature() + " predicted")))));
            double[] coefficients = linreg.coefficients();
            System.out.println("Number of coefficients: " + linreg.numParameters());
            System.out.println(linreg.toString());
            RegressionTable regressionTable = new RegressionTable(coefficients[coefficients.length - 1]);
            List<FieldName> numericPredictorFields = request.getDataset()
                    .getDataEntry()
                    .get(0)
                    .getValues()
                    .keySet()
                    .stream()
//                    .filter(feature -> {
//                        return !feature.equals(request.getPredictionFeature());
//                    })
                    .map(feature -> {
                        return new FieldName(feature);
                    })
                    .collect(Collectors.toList());
            List<NumericPredictor> numericPredictors = IntStream.range(0, coefficients.length - 1).mapToObj(i -> {
                double coefficient = coefficients[i];
                return new NumericPredictor(numericPredictorFields.get(i), coefficient);
            }).collect(Collectors.toList());
            regressionTable.withNumericPredictors(numericPredictors);
            regressionModel.withRegressionTables(regressionTable);

            PMML pmml = new PMML();
            pmml.withModels(regressionModel);
            pmml.setVersion("4.2");
            Header header = new Header();
            header.setCopyright("NTUA Chemical Engineering Control Lab 2015");
            header.setDescription("MLR Model");
            header.setTimestamp(new Timestamp());
            header.setApplication(new Application("Jaqpot Quattro"));
            pmml.setHeader(header);

            List<DataField> dataFields = request.getDataset()
                    .getDataEntry()
                    .get(0)
                    .getValues()
                    .keySet()
                    .stream()
                    .map(feature -> {
                        DataField dataField = new DataField();
                        dataField.setName(new FieldName(feature));
                        dataField.setOpType(OpType.CONTINUOUS);
                        dataField.setDataType(DataType.DOUBLE);
                        return dataField;
                    })
                    .collect(Collectors.toList());
            DataDictionary dataDictionary = new DataDictionary(dataFields);
            pmml.setDataDictionary(dataDictionary);

            WekaModel model = new WekaModel();
            model.setClassifier(linreg);

            TrainingResponse response = new TrainingResponse();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(baos);
            out.writeObject(model);
            String base64Model = Base64.getEncoder().encodeToString(baos.toByteArray());
            response.setRawModel(base64Model);
            List<String> independentFeatures = request.getDataset()
                    .getDataEntry()
                    .get(0)
                    .getValues()
                    .keySet()
                    .stream()
                    .filter(feature -> !feature.equals(request.getPredictionFeature()))
                    .collect(Collectors.toList());
            response.setIndependentFeatures(independentFeatures);

            ByteArrayOutputStream pmmlBaos = new ByteArrayOutputStream();
            JAXBUtil.marshalPMML(pmml, new StreamResult(pmmlBaos));
            response.setPmmlModel(pmmlBaos.toString());
            return Response.ok(response).build();
        } catch (Exception ex) {
            Logger.getLogger(WekaMLR.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorReportFactory.internalServerError()).build();
        }
    }

    @POST
    @Path("prediction")
    public Response prediction(PredictionRequest request) {

        try {
            String base64Model = (String) request.getRawModel();
            byte[] modelBytes = Base64.getDecoder().decode(base64Model);
            ByteArrayInputStream bais = new ByteArrayInputStream(modelBytes);
            ObjectInput in = new ObjectInputStream(bais);
            WekaModel model = (WekaModel) in.readObject();

            Classifier classifier = model.getClassifier();

            List<Attribute> attributes = request.getDataset().getDataEntry()
                    .stream()
                    .findFirst()
                    .get()
                    .getValues()
                    .keySet()
                    .stream()
                    .map(feature -> {
                        return new Attribute(feature);
                    }).collect(Collectors.toList());

            Instances data = new Instances("PredictionDataset", new ArrayList<>(attributes), request.getDataset().getDataEntry().size());

            for (DataEntry dataEntry : request.getDataset().getDataEntry()) {
                Instance instance = new DenseInstance(dataEntry.getValues().size());
                dataEntry.getValues().entrySet().stream().forEach(entry -> {
                    instance.setValue(data.attribute(entry.getKey()), Double.parseDouble(entry.getValue().toString()));
                });
                data.add(instance);
            }

            List<Object> predictions = new ArrayList<>();

            data.stream().forEach(instance -> {
                try {
                    double prediction = classifier.classifyInstance(instance);
                    predictions.add(prediction);
                } catch (Exception ex) {
                    Logger.getLogger(WekaMLR.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            PredictionResponse response = new PredictionResponse();
            response.setPredictions(predictions);
            return Response.ok(response).build();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(WekaMLR.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorReportFactory.internalServerError())
                    .build();
        }
    }

}
