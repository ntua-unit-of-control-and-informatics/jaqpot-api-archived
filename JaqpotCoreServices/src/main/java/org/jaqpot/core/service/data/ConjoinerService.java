/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.data;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.bundle.BundleProperties;
import org.jaqpot.core.model.dto.bundle.BundleSubstances;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.Substance;
import org.jaqpot.core.model.dto.study.Effect;
import org.jaqpot.core.model.dto.study.Studies;
import org.jaqpot.core.model.dto.study.Study;
import org.jaqpot.core.model.dto.study.proteomics.Proteomics;
import org.jaqpot.core.model.factory.TaskFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.UnSecure;

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

    @EJB
    TaskHandler taskHandler;

    @Resource(lookup = "java:jboss/exported/jms/topic/preparation")
    private Topic preparationQueue;

    @Inject
    private JMSContext jmsContext;

    public Task initiatePreparation(Map<String, Object> options, String userName) {

        Task task = TaskFactory.queuedTask("Preparation on bundle: " + options.get("bundle_uri"),
                "A preparation procedure will return a Dataset if completed successfully."
                + "It may also initiate other procedures if desired.",
                userName);
        task.setType(Task.Type.PREPARATION);
        options.put("taskId", task.getId());
        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(preparationQueue, options);
        return task;
    }

    public Dataset prepareDataset(String bundleURI, String subjectId) {

        BundleSubstances substances = client.target(bundleURI + "/substance")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("subjectid", subjectId)
                .get(BundleSubstances.class);
        BundleProperties properties = client.target(bundleURI + "/property")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("subjectid", subjectId)
                .get(BundleProperties.class);

        Dataset dataset = new Dataset();
        List<DataEntry> dataEntries = new ArrayList<>();

        for (Substance s : substances.getSubstance()) {
            Studies studies = client.target(s.getURI() + "/study")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("subjectid", subjectId)
                    .get(Studies.class);
            DataEntry dataEntry = createDataEntry(studies, properties.getFeature().keySet());
            dataEntries.add(dataEntry);
        }

        ROG rog = new ROG(true);
        dataset.setId(rog.nextString(12));
        dataset.setDataEntry(dataEntries);

        //Takes the intersection of properties of all substances
        dataset.getDataEntry().parallelStream().forEach(de -> {
            dataset.getDataEntry().parallelStream().forEach(e -> {
                de.getValues().keySet().retainAll(e.getValues().keySet());
            });
        });

        return dataset;

    }

    //TODO: Handle multiple effects that map to the same property
    public DataEntry createDataEntry(Studies studies, Set<String> propertyCategories) {
        DataEntry dataEntry = new DataEntry();
        Substance compound = new Substance();
        TreeMap<String, Object> values = new TreeMap<>();
        for (Study study : studies.getStudy()) {
            compound.setURI(study.getOwner().getSubstance().getUuid());

            //Checks if the protocol category is present in the selection Set
            String code = study.getProtocol().getCategory().getCode();
            if (!propertyCategories.stream().filter(c -> c.contains(code)).findAny().isPresent()) {
                continue;
            }

            //Parses Proteomics data if study's protocol category is PROTEOMICS_SECTION
            if (study.getProtocol().getCategory().getCode().equals("PROTEOMICS_SECTION")) {
                values.putAll(parseProteomics(study));
                continue;
            }

            //Parses each effect of the study as a different property
            for (Effect effect : study.getEffects()) {
                if (effect.getEndpoint().equals("IMAGE")) {
                    Response response = client.target("http://enanomapper.ntua.gr:8880/imageAnalysis/service/analyze")
                            .request()
                            .accept(MediaType.APPLICATION_JSON)
                            .post(Entity.entity(new Form("image", effect.getResult().getTextValue()), "application/x-www-form-urlencoded"));
                    GenericType<List<Map<String, Object>>> type = new GenericType<List<Map<String, Object>>>() {
                    };
                    List<Map<String, Object>> allParticles = response.readEntity(type);
                    response.close();
                    for (Map<String, Object> particle : allParticles) {
                        try {
                            String propertyURI = "property/image/" + URLEncoder.encode((String) particle.remove("id"), "UTF-8");
                            for (Entry<String, Object> entry : particle.entrySet()) {
                                try {
                                    Number value = Double.parseDouble((String) entry.getValue());
                                    values.put(propertyURI + "/" + entry.getKey(), value);
                                } catch (NumberFormatException ex) {
                                    continue;
                                }
                            }
                        } catch (UnsupportedEncodingException ex) {
                            continue;
                        }
                    }
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
                if (value == null) {
                    continue;
                }
                values.put(propertyURIJoiner.toString(), value);
            }
        }
        dataEntry.setCompound(compound);
        dataEntry.setValues(values);

        return dataEntry;
    }

    //TODO: Implement Dixon's q-test
    public Object calculateValue(Effect effect) {

        Object currentValue = null; // return null if conditions not satisfied

        // values not allowed for loQualifier & upQualifier --> this can be switched to "allowed values" if necessary
        List<String> loNotAllowed = Arrays.asList(null, "", " ", "~", "!=", ">", ">=");
        List<String> upNotAllowed = Arrays.asList(null, "", " ", "~", "!=", "<", "<=");

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
        } else {
            if ((effect.getResult().getUpValue() != null) && (!(upNotAllowed.contains(effect.getResult().getUpQualifier())))) {
                currentValue = effect.getResult().getUpValue().doubleValue();
            }
        }

        return currentValue;
    }

    public Map<String, Object> parseProteomics(Study study) {
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
                values.put(propertyURIJoiner.toString(), protValue);
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
