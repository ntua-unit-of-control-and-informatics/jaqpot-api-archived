/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.factory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;

/**
 *
 * @author hampos
 */
public class DatasetFactory {

    public static Dataset copy(Dataset dataset) {
        Dataset result = new Dataset();
        result.setId(dataset.getId());
        result.setMeta(dataset.getMeta());

        List<DataEntry> dataEntries = dataset.getDataEntry()
                .parallelStream()
                .map(dataEntry -> {
                    DataEntry newEntry = new DataEntry();
                    newEntry.setCompound(dataEntry.getCompound());
                    newEntry.setValues(new TreeMap<>(dataEntry.getValues()));
                    return newEntry;
                })
                .collect(Collectors.toList());
        result.setDataEntry(dataEntries);
        return result;
    }

    public static Dataset copy(Dataset dataset, Set<String> features) {
        Dataset result = new Dataset();
        result.setId(dataset.getId());
        result.setMeta(dataset.getMeta());

        List<DataEntry> dataEntries = dataset.getDataEntry()
                .parallelStream()
                .map(dataEntry -> {
                    DataEntry newEntry = new DataEntry();
                    newEntry.setCompound(dataEntry.getCompound());
                    TreeMap<String, Object> values = new TreeMap<>();
                    dataEntry.getValues()
                    .keySet()
                    .stream()
                    .filter(feature -> features.contains(feature))
                    .forEach(feature -> {
                        values.put(feature, dataEntry.getValues().get(feature));
                    });
                    newEntry.setValues(values);
                    return newEntry;
                })
                .collect(Collectors.toList());
        result.setDataEntry(dataEntries);
        Set<FeatureInfo> featureInfo = new HashSet<>();
        dataset.getFeatures().stream().filter(f -> features.contains(f.getURI())).forEach(f -> featureInfo.add(f));
        result.setFeatures(featureInfo);
        return result;
    }

    public static Dataset merge(Dataset dataset, Dataset other) {
        for (int i = 0; i < dataset.getDataEntry().size(); i++) {
            DataEntry dataEntry = dataset.getDataEntry().get(i);
            DataEntry otherEntry = other.getDataEntry().get(i);
            dataEntry.getValues().putAll(otherEntry.getValues());
        }
        dataset.getFeatures().addAll(other.getFeatures());
        return dataset;
    }
}
