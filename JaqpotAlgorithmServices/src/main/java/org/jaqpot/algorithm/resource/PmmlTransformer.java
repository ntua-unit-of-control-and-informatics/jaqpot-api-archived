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
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.sax.SAXSource;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.PMML;
import org.jaqpot.algorithm.model.PmmlModel;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.PredictionRequest;
import org.jaqpot.core.model.dto.jpdi.PredictionResponse;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.dto.jpdi.TrainingResponse;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jpmml.evaluator.ExpressionUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.PMMLEvaluationContext;
import org.jpmml.manager.PMMLManager;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@Path("pmml")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PmmlTransformer {

    private static final Logger LOG = Logger.getLogger(PmmlTransformer.class.getName());

    @POST
    @Path("training")
    public Response training(TrainingRequest request) {

        try {
            Map<String, Object> parameters = request.getParameters() != null ? request.getParameters() : new HashMap<>();
            String transformations = (String) parameters.get("transformations");

            ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();

            String pmmlString = client.target(transformations)
                    .request()
                    .accept(MediaType.APPLICATION_XML)
                    .get(String.class);

            InputStream in = new ByteArrayInputStream(pmmlString.getBytes());
            InputSource source = new InputSource(in);
            SAXSource transformedSource = ImportFilter.apply(source);
            PMML pmml = JAXBUtil.unmarshalPMML(transformedSource);

            //Wrapper for the PMML object with management functionality
            PMMLManager pmmlManager = new PMMLManager(pmml);

            PmmlModel model = new PmmlModel();
            model.setPmmlString(pmmlString);

            TrainingResponse response = new TrainingResponse();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(baos);
            out.writeObject(model);
            String base64Model = Base64.getEncoder().encodeToString(baos.toByteArray());
            response.setRawModel(base64Model);
            List<String> independentFeatures = pmmlManager.getDataDictionary()
                    .getDataFields()
                    .stream()
                    .map(field -> {
                        return field.getName().getValue();
                    })
                    .filter(field -> !field.equals(request.getPredictionFeature()))
                    .collect(Collectors.toList());
            response.setIndependentFeatures(independentFeatures);
            response.setAdditionalInfo(request.getPredictionFeature());
            List<String> predictedFeatures = pmmlManager.getTransformationDictionary()
                    .getDerivedFields()
                    .stream()
                    .map(field -> {
                        return field.getName().getValue();
                    })
                    .filter(field -> !field.equals(request.getPredictionFeature()))
                    .collect((Collectors.toList()));
            response.setPredictedFeatures(predictedFeatures);
            response.setPmmlModel(pmmlString);

            return Response.ok(response).build();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
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
            PmmlModel model = (PmmlModel) in.readObject();

            String pmmlString = model.getPmmlString();
            InputStream inStr = new ByteArrayInputStream(pmmlString.getBytes());
            InputSource source = new InputSource(inStr);
            SAXSource transformedSource = ImportFilter.apply(source);
            PMML pmml = JAXBUtil.unmarshalPMML(transformedSource);

            //Wrapper for the PMML object with management functionality
            PMMLManager pmmlManager = new PMMLManager(pmml);

            //The list of derived fields from the pmml file, each derived fields uses various datafields
            List<DerivedField> derivedFields = pmmlManager.getTransformationDictionary()
                    .getDerivedFields()
                    .stream()
                    .filter(field -> !field.getName().getValue().equals((String) request.getAdditionalInfo()))
                    .collect(Collectors.toList());
            //The list of data fields from the pmml file
            List<DataField> dataFields = pmmlManager.getDataDictionary()
                    .getDataFields()
                    .stream()
                    .filter(field -> !field.getName().getValue().equals((String) request.getAdditionalInfo()))
                    .collect(Collectors.toList());

            Dataset dataset = request.getDataset();

            List<LinkedHashMap<String, Object>> predictions = new ArrayList<>();

            dataset.getDataEntry().stream().forEach((dataEntry) -> {
                //For each data entry a PMMLEvaluationContext is saturated with values for each data field
                PMMLEvaluationContext context = new PMMLEvaluationContext(pmmlManager);
                Map<String, Object> values = dataEntry.getValues();
                dataFields.stream().forEach(dataField -> {
                    if (!values.containsKey(dataField.getName().getValue())) {
                        throw new BadRequestException("DataField " + dataField.getName().getValue()
                                + "specified in transformations PMML does not exist in dataset.");
                    }
                    context.declare(dataField.getName(), values.get(dataField.getName().getValue()));
                });
                LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                //Each derived field is evaluated by the context and a value is produced
                derivedFields.stream().forEach((derivedField) -> {
                    FieldValue value = ExpressionUtil.evaluate(derivedField, context);
                    result.put(derivedField.getName().getValue(), value.asNumber());
                });
                //A newly created map of transformed property names and values is placed in the data entry
                predictions.add(result);
            });
            PredictionResponse response = new PredictionResponse();
            response.setPredictions(predictions);
            return Response.ok(response).build();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ex.getMessage())
                    .build();
        }
    }

}
