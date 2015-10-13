/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    public static Dataset mergeColumns(Dataset dataset, Dataset other) {
        if (dataset != null && other == null) {
            return dataset;
        } else if (dataset == null && other != null) {
            return other;
        } else if (dataset == null && other == null) {
            return null;
        } else {
            for (int i = 0; i < dataset.getDataEntry().size(); i++) {
                DataEntry dataEntry = dataset.getDataEntry().get(i);
                DataEntry otherEntry = other.getDataEntry().get(i);
                dataEntry.getValues().putAll(otherEntry.getValues());
            }
            dataset.getFeatures().addAll(other.getFeatures());
            return dataset;
        }
    }

    public static Dataset mergeRows(Dataset dataset, Dataset other) {
        if (dataset != null && other == null) {
            return dataset;
        } else if (dataset == null && other != null) {
            return other;
        } else if (dataset == null && other == null) {
            return null;
        } else {
            dataset.getDataEntry().addAll(other.getDataEntry());
            dataset.getFeatures().addAll(other.getFeatures());
            return dataset;
        }
    }

    public static Dataset randomize(Dataset dataset, Long seed) {
        Random generator = new Random(seed);
        dataset.setDataEntry(generator.ints(dataset.getDataEntry().size(), 0, dataset.getDataEntry().size())
                .mapToObj(i -> {
                    return dataset.getDataEntry().get(i);
                })
                .collect(Collectors.toList()));
        return dataset;
    }

    public static Dataset stratify(Dataset dataset, Integer groupSize, String targetFeature) {
        Object value = dataset.getDataEntry().get(0).getValues().get(targetFeature);
        if (value instanceof Number) {
            List<DataEntry> sortedEntries = dataset.getDataEntry().stream()
                    .sorted((e1, e2) -> {
                        Double a = Double.parseDouble(e1.getValues().get(targetFeature).toString());
                        Double b = Double.parseDouble(e2.getValues().get(targetFeature).toString());
                        return a.compareTo(b);
                    })
                    .collect(Collectors.toList());

            List<DataEntry> finalEntries = new ArrayList<>();
            int i = 0;
            while (finalEntries.size() < sortedEntries.size()) {
                for (int j = 0; j < groupSize; j++) {
                    int k = i + j * groupSize;
                    if (k >= sortedEntries.size()) {
                        break;
                    }
                    DataEntry de = sortedEntries.get(k);
                    finalEntries.add(de);
                }

            }
            dataset.setDataEntry(finalEntries);
            return dataset;
        } else {
            return null;
        }

    }
}
