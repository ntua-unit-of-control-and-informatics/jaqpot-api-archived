/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
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
package org.jaqpot.core.model.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.dto.dataset.Substance;
import org.jaqpot.core.model.util.ROG;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
public class DatasetFactory {

    public static Dataset createEmpty(Integer rows) {
        Dataset dataset = new Dataset();
        List<DataEntry> dataEntries = IntStream.range(1, rows + 1)
                .mapToObj(i -> {
                    DataEntry de = new DataEntry();
                    de.setValues(new TreeMap<>());
                    Substance s = new Substance();
                    s.setName(Integer.toString(i));
                    s.setURI("/substance/" + i);
                    de.setCompound(s);
                    return de;
                }).collect(Collectors.toList());
        dataset.setDataEntry(dataEntries);
        dataset.setId(UUID.randomUUID().toString());
        dataset.setVisible(Boolean.TRUE);
        ROG randomStringGenerator = new ROG(true);
        dataset.setId(randomStringGenerator.nextString(14));
        dataset.setFeatures(new HashSet<>());
        dataset.setMeta(MetaInfoBuilder.builder()
                .addTitles("Empty dataset")
                .addDescriptions("Empty dataset")
                .addCreators(new String[0])
                .build());

        return dataset;
    }

    public static void addEmptyRows(Dataset dataset, Integer rows) {
        List<DataEntry> dataEntries = IntStream.range(1, rows + 1)
                .mapToObj(i -> {
                    DataEntry de = new DataEntry();
                    de.setValues(new TreeMap<>());
                    Substance s = new Substance();
                    s.setName(Integer.toString(i));
                    s.setURI("/substance/" + i);
                    de.setCompound(s);
                    return de;
                }).collect(Collectors.toList());
        dataset.setDataEntry(dataEntries);
    }

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
        result.setFeatures(dataset.getFeatures());
        result.setDatasetURI(dataset.getDatasetURI());
        result.setDescriptors(dataset.getDescriptors());
        result.setTotalColumns(dataset.getTotalColumns());
        result.setTotalRows(dataset.getTotalRows());
        result.setByModel(dataset.getByModel());
        return result;
    }

    public static Dataset copy(Dataset dataset, Integer rowStart, Integer rowMax) {
        Dataset result = new Dataset();
        result.setId(UUID.randomUUID().toString());
        result.setMeta(dataset.getMeta());

        List<DataEntry> dataEntries = dataset.getDataEntry()
                .parallelStream()
                .skip(rowStart)
                .limit(rowMax)
                .map(dataEntry -> {
                    DataEntry newEntry = new DataEntry();
                    newEntry.setCompound(dataEntry.getCompound());
                    newEntry.setValues(new TreeMap<>(dataEntry.getValues()));
                    return newEntry;
                })
                .collect(Collectors.toList());
        result.setDataEntry(dataEntries);
        result.setFeatures(dataset.getFeatures());
        result.setDatasetURI(dataset.getDatasetURI());
        result.setDescriptors(dataset.getDescriptors());
        result.setTotalColumns(dataset.getTotalColumns());
        result.setTotalRows(dataset.getTotalRows());
        result.setByModel(dataset.getByModel());
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
            for (int i = 0; i < Math.min(dataset.getDataEntry().size(), other.getDataEntry().size()); i++) {
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

    public static Dataset stratify(Dataset dataset, Integer folds, String targetFeature) {
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
                int k = 0, j = 0;
                while (k < sortedEntries.size()) {
                    k = i + j * folds;
                    if (k >= sortedEntries.size()) {
                        break;
                    }
                    DataEntry de = sortedEntries.get(k);
                    finalEntries.add(de);
                    j++;
                }
                i++;
            }
            dataset.setDataEntry(finalEntries);
            return dataset;
        } else {
            return null;
        }

    }
}
