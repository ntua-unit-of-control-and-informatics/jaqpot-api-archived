/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.mdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.jaqpot.core.data.DescriptorHandler;
import org.jaqpot.core.data.DoaHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.wrappers.DatasetLegacyWrapper;
import org.jaqpot.core.model.DataEntry;
import org.jaqpot.core.model.Descriptor;
import org.jaqpot.core.model.Doa;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.EntryId;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.service.client.jpdi.JPDIClient;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

/**
 *
 * @author pantelispanka
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:jboss/exported/jms/topic/chempot"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Topic")
})
public class ChempotProcedure extends AbstractJaqpotProcedure implements MessageListener {

    private static final Logger LOG = Logger.getLogger(ChempotProcedure.class.getName());

    @Inject
    JPDIClient jpdiClient;

    @EJB
    DescriptorHandler descriptorHandler;

    @EJB
    ModelHandler modelHandler;

    @EJB
    DoaHandler doaHandler;

    @EJB
    FeatureHandler featureHandler;

    @EJB
    DatasetLegacyWrapper datasetLegacyWrapper;

    public ChempotProcedure() {
        super(null);
    }

    @Inject
    public ChempotProcedure(TaskHandler taskHandler) {
        super(taskHandler);
    }

    @Override
    public void onMessage(Message msg) {
        Map<String, Object> messageBody;
        try {

            messageBody = msg.getBody(Map.class);

            String taskId = (String) messageBody.get("taskId");
            String apiKey = (String) messageBody.get("api_key");
            String modelId = (String) messageBody.get("modelId");
            String descriptorId = (String) messageBody.get("descriptors");
            String smiles = (String) messageBody.get("smiles");
            String doa = (String) messageBody.get("doa");
            String userId = (String) messageBody.get("userId");

            init(taskId);
            checkCancelled();
            start(Task.Type.PREDICTION);

            progress(5f, "Descriptor Procedure is now running with ID " + Thread.currentThread().getName());
            progress(10f, "Starting descriptor calculation...");
            checkCancelled();

            Dataset initialDataset = new Dataset();
            initialDataset.setMeta(null);
            initialDataset.setId(null);
            DataEntry de = new DataEntry();
            EntryId entryId = new EntryId();
            entryId.setName("smiles");
            entryId.setOwnerUUID(userId);
            entryId.setType("String");
            entryId.setURI("None");
            
            
            TreeMap<String, Object> values = new TreeMap();
            values.put("0", smiles);
            de.setValues(values);
            de.setEntryId(entryId);
            List<DataEntry> del = new ArrayList();
            del.add(de);
            initialDataset.setDataEntry(del);
//            FeatureInfoFactory
            FeatureInfo fi = new FeatureInfo();
            fi.setKey("0");
            fi.setName("smiles");
            fi.setCategory(Dataset.DescriptorCategory.EXPERIMENTAL);
            fi.setURI("None");
            fi.setUnits("None");
            HashMap<String, Object> cond = new HashMap();
            fi.setConditions(cond);
            Set<FeatureInfo> fis = new HashSet();
            fis.add(fi);
            initialDataset.setFeatures(fis);

            HashMap<String, Object> parameterMap = null;
            if (descriptorId.equals("cdk")) {
                List<String> categs = new ArrayList();
                categs.add("all");
                parameterMap.put("categories", categs);
            }

            Descriptor descriptor = descriptorHandler.find(descriptorId);
            Model model = modelHandler.find(modelId);

            Future<Dataset> futureDataset = jpdiClient.descriptor(initialDataset, descriptor, parameterMap, taskId);

            progress("Created descriptors.");
            progress(40f, "Dataset was built successfully.");
            checkCancelled();

            List<String> featNames = new ArrayList();
            List<FeatureInfo> feats = new ArrayList();

            Dataset forPrediction = new Dataset();
            forPrediction.setMeta(null);
            forPrediction.setByModel(modelId);

            Integer i = 0;
            for (String f : model.getIndependentFeatures()) {
                String[] featId_ar = f.split("/");
                String featId = featId_ar[featId_ar.length - 1];
                Feature feat = featureHandler.find(featId);
                String featName = feat.getMeta().getTitles().iterator().next();
                featNames.add(featName);
                FeatureInfo featInfo = new FeatureInfo();
                featInfo.setKey(i.toString());
                featInfo.setName(featName);
                featInfo.setCategory(Dataset.DescriptorCategory.FORPREDICTION);
                featInfo.setURI(f);
                featInfo.setUnits("None");
                feats.add(featInfo);
                i += 1;
            }

            Dataset datasetGot = futureDataset.get();
            TreeMap<String, Object> valsForPred = new TreeMap();
            DataEntry deForPred = new DataEntry();
            Set<FeatureInfo> featsForPred = new HashSet();
            for (DataEntry dataEntryDescribed : datasetGot.getDataEntry()) {
                for (Map.Entry<String, Object> dat : dataEntryDescribed.getValues().entrySet()) {
                    if (featNames.contains(dat.getKey())) {
                        for (FeatureInfo feInf : feats) {
                            if (feInf.getName().equals(dat.getKey())) {
                                valsForPred.put(feInf.getKey(), dat.getValue());
                                featsForPred.add(feInf);
                            }
                        }
                    }
                }
            }

            deForPred.setValues(valsForPred);
            forPrediction.setFeatures(featsForPred);
            List<DataEntry> data = new ArrayList();
            data.add(deForPred);
            forPrediction.setDataEntry(data);

            Doa doaM = null;
            if (doa != null && doa.equals("true")) {
                doaM = doaHandler.findBySourcesWithDoaMatrix("model/" + model.getId());
            }

            progress("Prediction completed successfully.");
            progress(80f, "Dataset was built successfully.");
            checkCancelled();
            Dataset dataset = jpdiClient.predict(forPrediction, model, forPrediction.getMeta(), taskId, doaM).get();

            checkCancelled();
            progress(90f, "Now saving to database...");
            dataset.setVisible(Boolean.TRUE);
            dataset.setFeatured(Boolean.FALSE);
            dataset.setByModel(model.getId());
            EntryId entryIdPredicted = new EntryId();
            entryIdPredicted.setName(smiles);
            entryIdPredicted.setOwnerUUID(userId);
            entryIdPredicted.setType("Prediction");
            entryIdPredicted.setURI("none");
            dataset.getDataEntry().get(0).setEntryId(entryIdPredicted);
            datasetLegacyWrapper.create(dataset);
            complete("dataset/" + dataset.getId());
            

        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "JMS message could not be read", ex);
            return;
        } catch (InterruptedException ex) {
            Logger.getLogger(ChempotProcedure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ChempotProcedure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ChempotProcedure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JaqpotDocumentSizeExceededException ex) {
            Logger.getLogger(ChempotProcedure.class.getName()).log(Level.SEVERE, null, ex);
        } 
//        catch (JaqpotDocumentSizeExceededException ex) {
//            Logger.getLogger(ChempotProcedure.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

}
