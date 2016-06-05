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
package org.jaqpot.algorithm.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import libsvm.svm_model;
import libsvm.svm_node;
import org.jaqpot.algorithm.model.WekaModel;
import org.jaqpot.algorithm.pmml.PmmlUtils;
import org.jaqpot.algorithm.weka.InstanceUtils;
import org.jaqpot.core.model.dto.jpdi.PredictionRequest;
import org.jaqpot.core.model.dto.jpdi.PredictionResponse;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.dto.jpdi.TrainingResponse;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@Path("svm")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WekaSVM {

    private final Double _gamma = 1.50,
            _cost = 100.0,
            _epsilon = 0.100,
            _coeff0 = 0.0,
            _nu = 0.5,
            _loss = 0.1;
    private final Integer _cacheSize = 250007,
            _degree = 3;
    private final String _kernel = "RBF";
    private final String _type = "NU_SVR";

    @POST
    @Path("training")
    public Response training(TrainingRequest request) {
        try {
            if (request.getDataset().getDataEntry().isEmpty() || request.getDataset().getDataEntry().get(0).getValues().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorReportFactory.badRequest("Dataset is empty", "Cannot train model on empty dataset"))
                        .build();
            }
            List<String> features = request.getDataset()
                    .getDataEntry()
                    .stream()
                    .findFirst()
                    .get()
                    .getValues()
                    .keySet()
                    .stream()
                    .collect(Collectors.toList());

            Instances data = InstanceUtils.createFromDataset(request.getDataset(), request.getPredictionFeature());
            Map<String, Object> parameters = request.getParameters() != null ? request.getParameters() : new HashMap<>();

            LibSVM regressor = new LibSVM();
            Double epsilon = Double.parseDouble(parameters.getOrDefault("epsilon", _epsilon).toString());
            Double cacheSize = Double.parseDouble(parameters.getOrDefault("cacheSize", _cacheSize).toString());
            Double gamma = Double.parseDouble(parameters.getOrDefault("gamma", _gamma).toString());
            Double coeff0 = Double.parseDouble(parameters.getOrDefault("coeff0", _coeff0).toString());
            Double cost = Double.parseDouble(parameters.getOrDefault("cost", _cost).toString());
            Double nu = Double.parseDouble(parameters.getOrDefault("nu", _nu).toString());
            Double loss = Double.parseDouble(parameters.getOrDefault("loss", _loss).toString());
            Integer degree = Integer.parseInt(parameters.getOrDefault("degree", _degree).toString());

            regressor.setEps(epsilon);
            regressor.setCacheSize(cacheSize);
            regressor.setDegree(degree);
            regressor.setCost(cost);
            regressor.setGamma(gamma);
            regressor.setCoef0(coeff0);
            regressor.setNu(nu);
            regressor.setLoss(loss);

            Integer svm_kernel = null;
            String kernel = parameters.getOrDefault("kernel", _kernel).toString();
            if (kernel.equalsIgnoreCase("rbf")) {
                svm_kernel = LibSVM.KERNELTYPE_RBF;
            } else if (kernel.equalsIgnoreCase("polynomial")) {
                svm_kernel = LibSVM.KERNELTYPE_POLYNOMIAL;
            } else if (kernel.equalsIgnoreCase("linear")) {
                svm_kernel = LibSVM.KERNELTYPE_LINEAR;
            } else if (kernel.equalsIgnoreCase("sigmoid")) {
                svm_kernel = LibSVM.KERNELTYPE_SIGMOID;
            }
            regressor.setKernelType(new SelectedTag(svm_kernel, LibSVM.TAGS_KERNELTYPE));

            Integer svm_type = null;
            String type = parameters.getOrDefault("type", _type).toString();
            if (type.equalsIgnoreCase("NU_SVR")) {
                svm_type = LibSVM.SVMTYPE_NU_SVR;
            } else if (type.equalsIgnoreCase("NU_SVC")) {
                svm_type = LibSVM.SVMTYPE_NU_SVC;
            } else if (type.equalsIgnoreCase("C_SVC")) {
                svm_type = LibSVM.SVMTYPE_C_SVC;
            } else if (type.equalsIgnoreCase("EPSILON_SVR")) {
                svm_type = LibSVM.SVMTYPE_EPSILON_SVR;
            } else if (type.equalsIgnoreCase("ONE_CLASS_SVM")) {
                svm_type = LibSVM.SVMTYPE_ONE_CLASS_SVM;
            }
            regressor.setSVMType(new SelectedTag(svm_type, LibSVM.TAGS_SVMTYPE));

            regressor.buildClassifier(data);

            WekaModel model = new WekaModel();
            model.setClassifier(regressor);

            Map<String, Double> options = new HashMap<>();
            options.put("gamma", gamma);
            options.put("coeff0", coeff0);
            options.put("degree", new Double(degree.toString()));

            Field modelField = LibSVM.class.getDeclaredField("m_Model");
            modelField.setAccessible(true);
            svm_model svmModel = (svm_model) modelField.get(regressor);
            double[][] coefs = svmModel.sv_coef;
            List<Double> coefsList = IntStream.range(0, coefs[0].length)
                    .mapToObj(i -> coefs[0][i])
                    .collect(Collectors.toList());

            svm_node[][] nodes = svmModel.SV;

            List<Map<Integer, Double>> vectors = IntStream.range(0, nodes.length)
                    .mapToObj(i -> {
                        Map<Integer, Double> node = new TreeMap<>();
                        Arrays.stream(nodes[i])
                                .forEach(n -> node.put(n.index, n.value));
                        return node;
                    })
                    .collect(Collectors.toList());

            String pmml = PmmlUtils.createSVMModel(features, request.getPredictionFeature(), "SVM", kernel, svm_type, options, coefsList, vectors);
            TrainingResponse response = new TrainingResponse();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(baos);
            out.writeObject(model);
            String base64Model = Base64.getEncoder().encodeToString(baos.toByteArray());
            response.setRawModel(base64Model);
            List<String> independentFeatures = features
                    .stream()
                    .filter(feature -> !feature.equals(request.getPredictionFeature()))
                    .collect(Collectors.toList());
            response.setIndependentFeatures(independentFeatures);
            response.setPmmlModel(pmml);
            response.setAdditionalInfo(request.getPredictionFeature());
            response.setPredictedFeatures(Arrays.asList("Weka SVM prediction of " + request.getPredictionFeature()));

            return Response.ok(response).build();
        } catch (Exception ex) {
            Logger.getLogger(WekaSVM.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("prediction")
    public Response prediction(PredictionRequest request) {
        try {
            if (request.getDataset().getDataEntry().isEmpty() || request.getDataset().getDataEntry().get(0).getValues().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorReportFactory.badRequest("Dataset is empty", "Cannot make predictions on empty dataset"))
                        .build();
            }

            String base64Model = (String) request.getRawModel();
            byte[] modelBytes = Base64.getDecoder().decode(base64Model);
            ByteArrayInputStream bais = new ByteArrayInputStream(modelBytes);
            ObjectInput in = new ObjectInputStream(bais);
            WekaModel model = (WekaModel) in.readObject();

            Classifier classifier = model.getClassifier();
            Instances data = InstanceUtils.createFromDataset(request.getDataset());
            String dependentFeature = (String) request.getAdditionalInfo();
            data.insertAttributeAt(new Attribute(dependentFeature), data.numAttributes());
            data.setClass(data.attribute(dependentFeature));
            List<LinkedHashMap<String, Object>> predictions = new ArrayList<>();
//            data.stream().forEach(instance -> {
//                try {
//                    double prediction = classifier.classifyInstance(instance);
//                    Map<String, Object> predictionMap = new HashMap<>();
//                    predictionMap.put("Weka SVM prediction of " + dependentFeature, prediction);
//                    predictions.add(predictionMap);
//                } catch (Exception ex) {
//                    Logger.getLogger(WekaSVM.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            });

            for (int i = 0; i < data.numInstances(); i++) {
                Instance instance = data.instance(i);
                try {
                    double prediction = classifier.classifyInstance(instance);
                    LinkedHashMap<String, Object> predictionMap = new LinkedHashMap<>();
                    predictionMap.put("Weka SVM prediction of " + dependentFeature, prediction);
                    predictions.add(predictionMap);
                } catch (Exception ex) {
                    Logger.getLogger(WekaMLR.class.getName()).log(Level.SEVERE, null, ex);
                    return Response.status(Response.Status.BAD_REQUEST).entity(ErrorReportFactory.badRequest("Error while gettting predictions.", ex.getMessage())).build();
                }
            }

            PredictionResponse response = new PredictionResponse();
            response.setPredictions(predictions);
            return Response.ok(response).build();
        } catch (Exception ex) {
            Logger.getLogger(WekaSVM.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ex.getMessage())
                    .build();
        }
    }
}
