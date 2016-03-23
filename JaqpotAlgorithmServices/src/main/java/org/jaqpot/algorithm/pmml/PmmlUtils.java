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
package org.jaqpot.algorithm.pmml;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import org.dmg.pmml.Application;
import org.dmg.pmml.Coefficient;
import org.dmg.pmml.Coefficients;
import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Extension;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.Header;
import org.dmg.pmml.LinearKernelType;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.NumericPredictor;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.PolynomialKernelType;
import org.dmg.pmml.RadialBasisKernelType;
import org.dmg.pmml.RealSparseArray;
import org.dmg.pmml.RegressionModel;
import org.dmg.pmml.RegressionTable;
import org.dmg.pmml.SupportVector;
import org.dmg.pmml.SupportVectorMachine;
import org.dmg.pmml.SupportVectorMachineModel;
import org.dmg.pmml.SupportVectors;
import org.dmg.pmml.SvmRepresentationType;
import org.dmg.pmml.Timestamp;
import org.dmg.pmml.VectorDictionary;
import org.dmg.pmml.VectorFields;
import org.dmg.pmml.VectorInstance;
import org.jpmml.model.JAXBUtil;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
public class PmmlUtils {

    public static String createRegressionModel(List<String> features, String predictionFeature, double[] coefficients, String algorithmName) {

        try {
            RegressionModel regressionModel = new RegressionModel();
            regressionModel.setModelName(algorithmName + " Model");
            regressionModel.setFunctionName(MiningFunctionType.REGRESSION);
            regressionModel.setAlgorithmName(algorithmName);

            List<MiningField> miningFields = features
                    .stream()
                    .map(feature -> {
                        MiningField miningField = new MiningField();
                        miningField.setName(new FieldName(feature));
                        if (feature.equals(predictionFeature)) {
                            miningField.setUsageType(FieldUsageType.PREDICTED);
                        } else {
                            miningField.setUsageType(FieldUsageType.ACTIVE);
                        }
                        return miningField;
                    })
                    .collect(Collectors.toList());
            regressionModel.setMiningSchema(new MiningSchema(miningFields));
            regressionModel.setOutput(new Output(Arrays.asList(new OutputField(new FieldName(predictionFeature + " predicted")))));

            RegressionTable regressionTable = new RegressionTable(coefficients[coefficients.length - 1]);
            List<FieldName> numericPredictorFields = features
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
            header.setDescription(algorithmName + " Model");
            header.setTimestamp(new Timestamp());
            header.setApplication(new Application("Jaqpot Quattro"));
            pmml.setHeader(header);

            List<DataField> dataFields = features
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

            ByteArrayOutputStream pmmlBaos = new ByteArrayOutputStream();
            JAXBUtil.marshalPMML(pmml, new StreamResult(pmmlBaos));
            return pmmlBaos.toString();
        } catch (JAXBException ex) {
            Logger.getLogger(PmmlUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static String createSVMModel(List<String> features, String predictionFeature, String algorithmName, String kernel, Integer type, Map<String, Double> options, List<Double> coefficients, List<Map<Integer, Double>> vectors) {
        try {
            SupportVectorMachineModel svmModel = new SupportVectorMachineModel();

            svmModel.setModelName(algorithmName + " Model");
            svmModel.setFunctionName(type < 3 ? MiningFunctionType.CLASSIFICATION : MiningFunctionType.REGRESSION);
            svmModel.setAlgorithmName(algorithmName);

            List<MiningField> miningFields = features
                    .stream()
                    .map(feature -> {
                        MiningField miningField = new MiningField();
                        miningField.setName(new FieldName(feature));
                        if (feature.equals(predictionFeature)) {
                            miningField.setUsageType(FieldUsageType.PREDICTED);
                        } else {
                            miningField.setUsageType(FieldUsageType.ACTIVE);
                        }
                        return miningField;
                    })
                    .collect(Collectors.toList());
            svmModel.setMiningSchema(new MiningSchema(miningFields));
            svmModel.setOutput(new Output(Arrays.asList(new OutputField(new FieldName(predictionFeature + " predicted")))));

            List<String> independentFeatures = features.stream()
                    .filter(feature -> !feature.equals(predictionFeature))
                    .collect(Collectors.toList());

            switch (kernel.toLowerCase()) {
                case "rbf":
                    svmModel.setSvmRepresentation(SvmRepresentationType.SUPPORT_VECTORS);
                    RadialBasisKernelType rbfKernel = new RadialBasisKernelType();
                    rbfKernel.setDescription("Radial basis kernel");
                    rbfKernel.setGamma(options.get("gamma"));
                    svmModel.setKernelType(rbfKernel);

                    VectorDictionary vectorDictionary = new VectorDictionary();
                    vectorDictionary.setNumberOfVectors(1);
                    VectorFields vectorFields = new VectorFields(independentFeatures.stream().map(feature -> {
                        return new FieldRef(new FieldName(feature));
                    }).collect(Collectors.toList()));
                    vectorDictionary.setVectorFields(vectorFields);
                    vectorDictionary.withVectorInstances(IntStream.range(0, coefficients.size()).mapToObj(i -> {
                        return new VectorInstance("k" + i);
                    }).collect(Collectors.toList()));
                    svmModel.setVectorDictionary(vectorDictionary);

                    SupportVectorMachine svm = new SupportVectorMachine();
                    svm.setSupportVectors(new SupportVectors(IntStream
                            .range(0, coefficients.size())
                            .mapToObj(i -> {
                                return new SupportVector("k" + i);
                            })
                            .collect(Collectors.toList())));
                    svm.setCoefficients(new Coefficients(coefficients.stream()
                            .map(c -> {
                                Coefficient coef = new Coefficient();
                                coef.setValue(c);
                                return coef;
                            })
                            .collect(Collectors.toList())));
                    svmModel.withSupportVectorMachines(svm);

                    break;
                case "polynomial":
                    svmModel.setSvmRepresentation(SvmRepresentationType.SUPPORT_VECTORS);
                    PolynomialKernelType polyKernel = new PolynomialKernelType();
                    polyKernel.setDescription("Polynomial kernel");
                    polyKernel.setCoef0(options.get("coeff0"));
                    polyKernel.setDegree(options.get("degree"));
                    polyKernel.setGamma(options.get("gamma"));
                    svmModel.setKernelType(polyKernel);

                    vectorDictionary = new VectorDictionary();
                    vectorDictionary.setNumberOfVectors(1);
                    vectorFields = new VectorFields(independentFeatures.stream().map(feature -> {
                        return new FieldRef(new FieldName(feature));
                    }).collect(Collectors.toList()));
                    vectorDictionary.setVectorFields(vectorFields);
                    vectorDictionary.withVectorInstances(IntStream.range(0, coefficients.size()).mapToObj(i -> {
                        return new VectorInstance("mv" + i);
                    }).collect(Collectors.toList()));
                    svmModel.setVectorDictionary(vectorDictionary);

                    svm = new SupportVectorMachine();
                    svm.setSupportVectors(new SupportVectors(IntStream
                            .range(0, coefficients.size())
                            .mapToObj(i -> {
                                return new SupportVector("mv" + i);
                            })
                            .collect(Collectors.toList())));
                    svm.setCoefficients(new Coefficients(coefficients.stream()
                            .map(c -> {
                                Coefficient coef = new Coefficient();
                                coef.setValue(c);
                                return coef;
                            })
                            .collect(Collectors.toList())));
                    svmModel.withSupportVectorMachines(svm);

                    break;
                case "linear":
                    svmModel.setSvmRepresentation(SvmRepresentationType.COEFFICIENTS);
                    LinearKernelType linearKernel = new LinearKernelType();
                    linearKernel.setDescription("Linear Kernel");
                    svmModel.setKernelType(linearKernel);

                    svm = new SupportVectorMachine();
                    svm.setCoefficients(new Coefficients(coefficients.stream()
                            .map(c -> {
                                Coefficient coef = new Coefficient();
                                coef.setValue(c);
                                return coef;
                            })
                            .collect(Collectors.toList())));
                    svmModel.withSupportVectorMachines(svm);
                    break;
            }
            VectorDictionary vd = new VectorDictionary();

            VectorFields vf = new VectorFields(IntStream.range(0, vectors.get(0).size())
                    .mapToObj(i -> new FieldRef(new FieldName("x" + i)))
                    .collect(Collectors.toList()));

            vd.withVectorFields(vf);

            vd.withVectorInstances(IntStream.range(0, vectors.size())
                    .mapToObj(i -> {
                        Map<Integer, Double> v = vectors.get(i);
                        VectorInstance vi = new VectorInstance("mv" + i);
                        RealSparseArray a = new RealSparseArray();
                        a.withN(v.size())
                                .withIndices(v.keySet())
                                .withEntries(v.values());
                        vi.withREALSparseArray(a);
                        return vi;
                    })
                    .collect(Collectors.toList())
            );
            
            svmModel.withVectorDictionary(vd);

            PMML pmml = new PMML();
            pmml.withModels(svmModel);
            pmml.setVersion("4.2");
            Header header = new Header();
            header.setCopyright("NTUA Chemical Engineering Control Lab 2015");
            header.setDescription(algorithmName + " Model");
            header.setTimestamp(new Timestamp());
            header.setApplication(new Application("Jaqpot Quattro"));
            pmml.setHeader(header);

            List<DataField> dataFields = features
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

            ByteArrayOutputStream pmmlBaos = new ByteArrayOutputStream();
            JAXBUtil.marshalPMML(pmml, new StreamResult(pmmlBaos));
            return pmmlBaos.toString();
        } catch (JAXBException ex) {
            Logger.getLogger(PmmlUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
