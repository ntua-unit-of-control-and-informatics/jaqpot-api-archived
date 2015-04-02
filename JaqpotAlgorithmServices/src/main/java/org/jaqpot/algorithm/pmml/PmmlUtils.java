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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.bind.JAXBException;
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
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jpmml.model.JAXBUtil;

/**
 *
 * @author hampos
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

}