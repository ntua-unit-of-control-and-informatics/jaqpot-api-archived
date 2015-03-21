/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.mdb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.ws.rs.BadRequestException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.sax.SAXSource;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.PMML;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.data.ConjoinerService;
import org.jpmml.evaluator.ExpressionUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.PMMLEvaluationContext;
import org.jpmml.manager.PMMLManager;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author hampos
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/preparation"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class PreparationMDB implements MessageListener {

    private static final Logger LOG = Logger.getLogger(PreparationMDB.class.getName());

    @Resource(lookup = "java:jboss/exported/jms/topic/training")
    private Topic trainingQueue;

    @Resource(lookup = "java:jboss/exported/jms/topic/prediction")
    private Topic predictionQueue;

    @Inject
    private JMSContext jmsContext;

    @EJB
    ConjoinerService conjoinerService;

    @EJB
    TaskHandler taskHandler;

    @EJB
    DatasetHandler datasetHandler;

    @Override
    public void onMessage(Message msg) {
        Task task = null;
        try {
            Map<String, Object> messageBody = msg.getBody(Map.class);
            task = taskHandler.find(messageBody.get("taskId"));

            if (task == null) {
                throw new NullPointerException("FATAL: Could not find task with id:" + messageBody.get("taskId"));
            }

            task.setStatus(Task.Status.RUNNING);
            task.getMeta().getComments().add("Preparation Task is now running.");
            taskHandler.edit(task);

            String bundleUri = (String) messageBody.get("bundle_uri");
            String subjectId = (String) messageBody.get("subjectid");

            task.getMeta().getComments().add("Starting Dataset preparation...");
            taskHandler.edit(task);
            Dataset dataset = conjoinerService.prepareDataset(bundleUri, subjectId);

            if (messageBody.containsKey("transformations") && messageBody.get("transformations") != null) {
                task.getMeta().getComments().add("Attempting to download transformations file...");
                taskHandler.edit(task);

                String transformations = (String) messageBody.get("transformations");

                //Open InputStream from transformations URL and parse it as PMML object
                URL transformationsURL = new URL(transformations);
                InputSource source = new InputSource(transformationsURL.openStream());
                SAXSource transformedSource = ImportFilter.apply(source);
                PMML pmml = JAXBUtil.unmarshalPMML(transformedSource);

                //Wrapper for the PMML object with management functionality
                PMMLManager pmmlManager = new PMMLManager(pmml);

                //The list of derived fields from the pmml file, each derived fields uses various datafields
                List<DerivedField> derivedFields = pmmlManager.getTransformationDictionary().getDerivedFields();
                //The list of data fields from the pmml file
                List<DataField> dataFields = pmmlManager.getDataDictionary().getDataFields();

                task.getMeta().getComments().add("Transformations file is downloaded and parsed. Applying transformations...");
                taskHandler.edit(task);

                for (DataEntry dataEntry : dataset.getDataEntry()) {
                    //For each data entry a PMMLEvaluationContext is saturated with values for each data field
                    PMMLEvaluationContext context = new PMMLEvaluationContext(pmmlManager);
                    Map<String, Object> values = dataEntry.getValues();
                    dataFields.stream().forEach(dataField -> {
                        if (!values.containsKey(dataField.getName().getValue())) {
                            throw new BadRequestException("DataField " + dataField.getName().getValue() + "specified in transformations PMML does not exist in dataset.");
                        }
                        context.declare(dataField.getName(), values.get(dataField.getName().getValue()));
                    });
                    TreeMap<String, Object> result = new TreeMap<>();
                    //Each derived field is evaluated by the context and a value is produced
                    for (DerivedField derivedField : derivedFields) {
                        FieldValue value = ExpressionUtil.evaluate(derivedField, context);
                        result.put(derivedField.getName().getValue(), value.asNumber());
                    }
                    //A newly created map of transformed property names and values is placed in the data entry
                    dataEntry.setValues(result);
                }
                task.getMeta().getComments().add("Transformations have been applied.");
                taskHandler.edit(task);
            }
            task.getMeta().getComments().add("Dataset ready.");
            task.getMeta().getComments().add("Saving to database...");
            taskHandler.edit(task);
            datasetHandler.create(dataset);

            task.getMeta().getComments().add("Dataset saved successfully.");
            taskHandler.edit(task);

            String mode = (String) messageBody.get("mode");
            String baseUri = (String) messageBody.get("base_uri");
            String datasetUri = baseUri + dataset.getClass().getSimpleName().toLowerCase() + "/" + dataset.getId();

            switch (mode) {
                case "TRAINING":
                    task.getMeta().getComments().add("Preparation Task is now completed.");
                    task.getMeta().getComments().add("Initiating Training Task...");
                    taskHandler.edit(task);
                    messageBody.put("dataset_uri", datasetUri);
                    jmsContext.createProducer().setDeliveryDelay(1000).send(trainingQueue, messageBody);
                    break;
                case "PREDICTION":
                    task.getMeta().getComments().add("Preparation Task is now completed.");
                    task.getMeta().getComments().add("Initiating Prediction Task...");
                    taskHandler.edit(task);
                    messageBody.put("dataset_uri", datasetUri);
                    jmsContext.createProducer().setDeliveryDelay(1000).send(predictionQueue, messageBody);
                    break;

                default:
                    task.setStatus(Task.Status.COMPLETED);
                    task.getMeta().getComments().add("Preparation Task is now completed.");
                    task.setResult("dataset/"+dataset.getId());
                    taskHandler.edit(task);
                    break;
            }

        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "JMS", "Error Accessing JMS asynchronous queues.", ex.getMessage()));
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.badRequest("Error downloading transformations file.", ex.getMessage()));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError("IOError", "IO Error while downloading transformations file.", ex.getMessage()));
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.badRequest("Error while parsing transformations xml.", ex.getMessage()));
        } catch (JAXBException ex) {
            LOG.log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.internalServerError(ex, "JMS", "Error Accessing JMS asynchronous queues.", ex.getMessage()));
        } catch (BadRequestException ex) {
            LOG.log(Level.SEVERE, null, ex);
            task.setStatus(Task.Status.ERROR);
            task.setErrorReport(ErrorReportFactory.badRequest("Error while processing input.", ex.getMessage()));
        } finally {
            taskHandler.edit(task);
        }
    }

}
