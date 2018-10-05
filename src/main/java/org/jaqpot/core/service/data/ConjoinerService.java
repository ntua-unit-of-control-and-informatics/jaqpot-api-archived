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
package org.jaqpot.core.service.data;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.bundle.BundleData;
import org.jaqpot.core.model.dto.bundle.BundleProperties;
import org.jaqpot.core.model.dto.bundle.BundleSubstances;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.dto.dataset.EntryId;
import org.jaqpot.core.model.dto.study.Effect;
import org.jaqpot.core.model.dto.study.Studies;
import org.jaqpot.core.model.dto.study.Study;
import org.jaqpot.core.model.dto.study.proteomics.Proteomics;
import org.jaqpot.core.model.factory.TaskFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.client.ambit.Ambit;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.json.Json;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Stateless
public class ConjoinerService {

    @Inject
    @Jackson
    JSONSerializer serializer;

    @Inject
    @UnSecure
    Client client;

    @Inject
    PropertyManager propertyManager;

    @EJB
    TaskHandler taskHandler;

    @EJB
    FeatureHandler featureHandler;

    @Resource(lookup = "java:jboss/exported/jms/topic/preparation")
    private Topic preparationQueue;


    @Inject
    private JMSContext jmsContext;

    @Inject
    Ambit ambitClient;

    private Set<FeatureInfo> featureMap;

    private Set<Dataset.DescriptorCategory> usedDescriptors;

    public Task initiatePreparation(Map<String, Object> options, String userName) throws JaqpotDocumentSizeExceededException {

        Task task = TaskFactory.queuedTask("Preparation on bundle: " + options.get("bundle_uri"),
                "A preparation procedure will return a Dataset if completed successfully."
                + "It may also initiate other procedures if desired.",
                userName);
        task.setType(Task.Type.PREPARATION);
        options.put("taskId", task.getId());
        task.setVisible(Boolean.TRUE);
        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(preparationQueue, options);
        return task;
    }

    public Dataset  prepareDataset(String substanceOwner, Set<String> substances, String subjectId, Set<String> descriptors,  Set<String> properties, Boolean intersectColumns, Boolean retainNullValues) throws RuntimeException, ExecutionException, InterruptedException {
        retainNullValues = false;
        BundleData ambitClientBundle = ambitClient.getSubstancesBySubstanceOwner(substanceOwner,subjectId);

        featureMap = new HashSet<>();
        usedDescriptors = new HashSet<>();
        List<DataEntry> dataEntries = new ArrayList<>();


        //Set<String> arList = ambitClientBundle.getProperties().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

        if (ambitClientBundle.getSubstances() != null) {

            for (EntryId substance : ambitClientBundle.getSubstances()) {
                if (substances.contains(substance.getURI())) {
                    Studies studies = ambitClient.getSubstanceStudies(substance.getURI().split("substance")[1], subjectId);
                    if (studies != null) {
                        DataEntry dataEntry = createDataEntry(substance, studies, propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_AMBIT), properties, subjectId, descriptors, retainNullValues);
                        dataEntries.add(dataEntry);
                    }
                }
            }
        }

        Dataset dataset = new Dataset();

        dataset.setFeatures(featureMap);

        ROG rog = new ROG(true);
        dataset.setId(rog.nextString(12));
        dataset.setDataEntry(dataEntries);

      if (intersectColumns) {
            //Takes the intersection of properties of all substances
            dataset.getDataEntry().stream().forEach(de -> {
                dataset.getDataEntry().stream()
                        .filter(e -> !e.equals(de))
                        .forEach(e -> {
                            de.getValues().keySet().retainAll(e.getValues().keySet());
                        });
                if (!dataset.getDataEntry().isEmpty()) {
                    dataset.setFeatures(dataset.getFeatures()
                            .stream()
                            .filter(f -> dataset.getDataEntry()
                                    .get(0)
                                    .getValues()
                                    .keySet()
                                    .contains(f.getURI())
                            )
                            .collect(Collectors.toSet()));
                }
            });
        } else {
            dataset.getDataEntry().stream().forEach(de -> {
                dataset.getDataEntry().stream()
                        .filter(e -> !e.equals(de))
                        .forEach(e -> {
                            e.getValues().keySet().forEach(key -> {
                                if (!de.getValues().containsKey(key)) {
                                    de.getValues().put(key, null);
                                }
                            });
                        });
            });
        }


        dataset.setDescriptors(usedDescriptors);
        dataset.setVisible(Boolean.TRUE);
        return dataset;
    }



    //TODO: Handle multiple effects that map to the same property
    public DataEntry createDataEntry( EntryId substance, Studies studies, String remoteServerBase, Set<String> propertyCategories, String subjectId, Set<String> descriptors, Boolean retainNullValues) throws ExecutionException, InterruptedException {
        DataEntry dataEntry = new DataEntry();
        TreeMap<String, Object> values = new TreeMap<>();

        for (Study study : studies.getStudy()) {
            //Checks if the protocol category is present in the selection Set
            String code = study.getProtocol().getCategory().getCode();
            if (!propertyCategories.stream().filter(c -> c.contains(code)).findAny().isPresent()) {
                continue;
            }

            //Parses Proteomics data if study's protocol category is PROTEOMICS_SECTION
            if (study.getProtocol().getCategory().getCode().equals("PROTEOMICS_SECTION")) {
                if (!descriptors.contains(Dataset.DescriptorCategory.EXPERIMENTAL.name())) {
                    continue;
                }
                values.putAll(parseProteomics(study, remoteServerBase));
                usedDescriptors.add(Dataset.DescriptorCategory.EXPERIMENTAL);
                continue;
            }

            //Parses each effect of the study as a different property
            for (Effect effect : study.getEffects()) {
                if (effect.getEndpoint().equals("IMAGE")) {
                    if (!descriptors.contains(Dataset.DescriptorCategory.IMAGE.name())) {
                        continue;
                    }

                    Response response = client.target(propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_IMAGE) + "analyze")
                            .request()
                            .accept(MediaType.APPLICATION_JSON)
                            .post(Entity.entity(new Form("image", effect.getResult().getTextValue()), "application/x-www-form-urlencoded"));
                    GenericType<List<Map<String, Object>>> type = new GenericType<List<Map<String, Object>>>() {
                    };
                    List<Map<String, Object>> allParticles = response.readEntity(type);
                    response.close();
                    for (Map<String, Object> particle : allParticles) {
                        if (!particle.get("id").equals("Average Particle")) {
                            continue;
                        }
                        try {
                            for (Entry<String, Object> entry : particle.entrySet()) {
                                try {
                                    String descriptorID = URLEncoder.encode("image average particle " + entry.getKey(), "UTF-8");
                                    Feature f = new Feature();
                                    f.setId(descriptorID);
                                    Number value = Double.parseDouble((String) entry.getValue());
                                    String URI = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE)
                                            + "feature/" + f.getId();
                                    values.put(URI, value);
                                    FeatureInfo featureInfo = new FeatureInfo();
                                    featureInfo.setURI(URI);
                                    featureInfo.setName(entry.getKey());
                                    featureInfo.setCategory(Dataset.DescriptorCategory.IMAGE);
                                    featureMap.add(featureInfo);
                                } catch (NumberFormatException ex) {
                                    continue;
                                }
                            }
                        } catch (UnsupportedEncodingException ex) {
                            continue;
                        }
                        usedDescriptors.add(Dataset.DescriptorCategory.IMAGE);
                    }
                    continue;
                } else if (effect.getEndpoint().equals("PDB_CRYSTAL_STRUCTURE")) {
                    if (!descriptors.contains(Dataset.DescriptorCategory.MOPAC.name())) {
                        continue;
                    }
                    try {
                        URI pdbUri = new URI(effect.getResult().getTextValue());
                    } catch (URISyntaxException ex) {
                        continue;
                    }

                    Dataset dataset = ambitClient.generateMopacDescriptors( effect.getResult().getTextValue(),subjectId);

                    GenericType<Map<String, Object>> type = new GenericType<Map<String, Object>>() {
                    };

                    Map<String, Object> mopacDescriptors = dataset.getDataEntry().get(0).getValues();

                    values.putAll(mopacDescriptors);
                    mopacDescriptors.keySet().forEach((key) -> {
                        Response featureResponse = client.target(key)
                                .request()
                                .accept(MediaType.APPLICATION_JSON)
                                .header("subjectid", subjectId)
                                .get();
                        String featureTitle = Json.createReader(featureResponse.readEntity(InputStream.class))
                                .readObject()
                                .getJsonObject("feature")
                                .getJsonObject(key)
                                .getString("title");
                        featureResponse.close();
                        FeatureInfo featureInfo = new FeatureInfo(key, featureTitle);
                        featureInfo.setCategory(Dataset.DescriptorCategory.MOPAC);
                        featureMap.add(featureInfo);
                    });
                    usedDescriptors.add(Dataset.DescriptorCategory.MOPAC);
                    continue;
                } else {
                    if (!descriptors.contains(Dataset.DescriptorCategory.EXPERIMENTAL.name())) {
                        continue;
                    }
                    String name = effect.getEndpoint();
                    String units = effect.getResult().getUnit();
                    String conditions = serializer.write(effect.getConditions());
                    String identifier = createHashedIdentifier(name, units, conditions);
                    String topcategory = study.getProtocol().getTopcategory();
                    String endpointcategory = study.getProtocol().getCategory().getCode();
                    List<String> guidelines = study.getProtocol().getGuideline();
                    String guideline = guidelines == null || guidelines.isEmpty() ? "" : guidelines.get(0);
                    StringJoiner propertyURIJoiner = getRelativeURI(name, topcategory, endpointcategory, identifier, guideline);
                    Object value = calculateValue(effect);
                    if (value == null && !retainNullValues) {
                        continue;
                    }
                    String propertyKey = remoteServerBase + propertyURIJoiner.toString();
                    if (values.containsKey(propertyKey)) {
                        Object old = values.get(propertyKey);
                        if (old instanceof List) {
                            ((List) old).add(value);
                        } else {
                            List list = new ArrayList();
                            list.add(old);
                            list.add(value);
                            values.put(propertyKey, list);
                        }
                    } else {
                        values.put(propertyKey, value);
                    }
                    FeatureInfo featureInfo = new FeatureInfo();
                    featureInfo.setURI(propertyKey);
                    featureInfo.setName(name);
                    featureInfo.setUnits(units);
                    featureInfo.setConditions(effect.getConditions());
                    featureInfo.setCategory(Dataset.DescriptorCategory.EXPERIMENTAL);
                    featureMap.add(featureInfo);
                    usedDescriptors.add(Dataset.DescriptorCategory.EXPERIMENTAL);
                }
            }
        }
        dataEntry.setEntryId(substance);
        dataEntry.setValues(values);

        return dataEntry;
    }

    //TODO: Implement Dixon's q-test
    public Object calculateValue(Effect effect) {

        Object currentValue = null; // return null if conditions not satisfied

        // values not allowed for loQualifier & upQualifier --> this can be switched to "allowed values" if necessary
        List<String> loNotAllowed = Arrays.asList("~", "!=", ">", ">=");
        List<String> upNotAllowed = Arrays.asList("~", "!=", "<", "<=");

        if ((effect.getResult().getLoValue() != null) && (!(loNotAllowed.contains(effect.getResult().getLoQualifier())))) {

            checker:
            if ((effect.getResult().getUpValue() != null) && (!(upNotAllowed.contains(effect.getResult().getUpQualifier())))) {

                // check whether loValue <= upValue
                if (effect.getResult().getLoValue().doubleValue() > effect.getResult().getUpValue().doubleValue()) {
                    break checker;
                }

                // check whether error exists and > diff(loValue, upValue)
                if ((effect.getResult().getErrorValue() != null) && (effect.getResult().getErrorValue().doubleValue() >= Math.abs(effect.getResult().getUpValue().doubleValue() - effect.getResult().getLoValue().doubleValue()))) {
                    break checker;
                }

                /*
                 // can add if we decide on matching qualifiers
                 if(!(effect.getResult().getLoQualifier().equals(effect.getResult().getUpQualifier()))){
                 break checker;
                 }
                 */
                // return avg
                currentValue = (effect.getResult().getLoValue().doubleValue() + effect.getResult().getUpValue().doubleValue()) / 2;
            } else {
                currentValue = effect.getResult().getLoValue().doubleValue();
            }
        } else if ((effect.getResult().getUpValue() != null) && (!(upNotAllowed.contains(effect.getResult().getUpQualifier())))) {
            currentValue = effect.getResult().getUpValue().doubleValue();
        } else if (effect.getResult().getErrorValue() != null) {
            currentValue = effect.getResult().getErrorValue().doubleValue();
        }

        return currentValue;
    }

    public Map<String, Object> parseProteomics(Study study, String remoteServerBase) {
        Map<String, Object> values = new TreeMap<>();
        study.getEffects().stream().findFirst().ifPresent(effect -> {
            String textValue = effect.getResult().getTextValue();
            Proteomics proteomics = serializer.parse(textValue, Proteomics.class);
            proteomics.entrySet().stream().forEach(entry -> {
                String name = effect.getEndpoint();
                String units = effect.getResult().getUnit();
                String conditions = serializer.write(effect.getConditions());
                String identifier = createHashedIdentifier(name, units, conditions);
                String topcategory = study.getProtocol().getTopcategory();
                String endpointcategory = study.getProtocol().getCategory().getCode();
                List<String> guidelines = study.getProtocol().getGuideline();
                String guideline = guidelines == null || guidelines.isEmpty() ? "" : guidelines.get(0);
                StringJoiner propertyURIJoiner = getRelativeURI(name, topcategory, endpointcategory, identifier, guideline);
                propertyURIJoiner.add(entry.getKey());
                Object protValue = entry.getValue().getLoValue();
                values.put(remoteServerBase + propertyURIJoiner.toString(), protValue);
                FeatureInfo featureInfo = new FeatureInfo(remoteServerBase + propertyURIJoiner.toString(), entry.getKey());
                featureInfo.setCategory(Dataset.DescriptorCategory.EXPERIMENTAL);
                featureMap.add(featureInfo);
            });
        });
        return values;
    }

    public StringJoiner getRelativeURI(
            String name,
            String topcategory,
            String endpointcategory,
            String identifier,
            String guideline) {
        StringJoiner joiner = new StringJoiner("/");
        try {
            joiner.add("property");
            joiner.add(URLEncoder.encode(topcategory == null ? "TOX" : topcategory, "UTF-8"));
            joiner.add(URLEncoder.encode(endpointcategory == null ? "UNKNOWN_TOXICITY_SECTION" : endpointcategory, "UTF-8"));
            joiner.add(URLEncoder.encode(name, "UTF-8"));
            joiner.add(identifier);
            joiner.add(URLEncoder.encode(UUID.nameUUIDFromBytes(guideline.getBytes()).toString(), "UTF-8"));
            return joiner;
        } catch (UnsupportedEncodingException ex) {
            return new StringJoiner("/").add("property");
        }
    }

    @Deprecated
    public String getRelativeURI(String name, String topcategory, String endpointcategory, String identifier, Boolean extendedURI, String guideline) {
        try {
            return String.format("/property/%s/%s%s%s/%s%s%s",
                    URLEncoder.encode(topcategory == null ? "TOX" : topcategory, "UTF-8"),
                    URLEncoder.encode(endpointcategory == null ? "UNKNOWN_TOXICITY_SECTION" : endpointcategory, "UTF-8"),
                    name == null ? "" : "/",
                    name == null ? "" : URLEncoder.encode(name, "UTF-8"),
                    identifier,
                    extendedURI ? "/" : "",
                    extendedURI ? URLEncoder.encode(UUID.nameUUIDFromBytes(guideline.getBytes()).toString(), "UTF-8") : "");
        } catch (UnsupportedEncodingException x) {
            return "/property";
        }
    }

    public String createHashedIdentifier(String name, String units, String conditions) {
        HashFunction hf = Hashing.sha1();
        StringBuilder b = new StringBuilder();
        b.append(name == null ? "" : name);
        b.append(units == null ? "" : units);
        b.append(conditions == null ? "" : conditions);

        HashCode hc = hf.newHasher()
                .putString(b.toString(), Charsets.US_ASCII)
                .hash();
        return hc.toString().toUpperCase();
    }

}
