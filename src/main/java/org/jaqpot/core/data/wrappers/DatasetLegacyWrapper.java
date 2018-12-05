package org.jaqpot.core.data.wrappers;

import org.jaqpot.core.data.DataEntryHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.model.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Stateless
public class DatasetLegacyWrapper {

    @EJB
    DataEntryHandler dataEntryHandler;

    @EJB
    DatasetHandler datasetHandler;

    public void create(Dataset dataset) throws IllegalArgumentException, JaqpotDocumentSizeExceededException {
        ROG randomStringGenerator = new ROG(true);

        HashSet<String> features = dataset.getFeatures().stream().map(FeatureInfo::getURI).collect(Collectors.toCollection(HashSet::new));
        for (DataEntry dataEntry : dataset.getDataEntry()) {
            HashSet<String> entryFeatures = new HashSet<>(dataEntry.getValues().keySet());
            if (!entryFeatures.equals(features)) {
                throw new IllegalArgumentException("Invalid Dataset - DataEntry URIs do not match with Feature URIs. "
                        + " Problem was found when parsing " + dataEntry.getEntryId());
            }
        }

        dataset.setTotalRows(dataset.getDataEntry().size());
        dataset.setTotalColumns(dataset.getDataEntry()
                .stream()
                .max((e1, e2) -> Integer.compare(e1.getValues().size(), e2.getValues().size()))
                .orElseGet(() -> {
                    DataEntry de = new DataEntry();
                    de.setValues(new TreeMap<>());
                    return de;
                })
                .getValues().size());
        dataset.setVisible(Boolean.TRUE);

        List<DataEntry> dataEntryList = dataset.getDataEntry();
        dataset.setDataEntry(new LinkedList<>());
        datasetHandler.create(dataset);
        for (DataEntry dataentry: dataEntryList) {
            dataentry.setId(randomStringGenerator.nextString(14));
            dataentry.setDatasetId(dataset.getId());
            dataEntryHandler.create(dataentry);
        }
    }

    public Dataset find(Object id, Integer rowStart, Integer rowMax, Integer colStart, Integer colMax){
        List<DataEntry> dataEntryList = dataEntryHandler.findDataEntriesByDatasetId(id, rowStart, rowMax, colStart, colMax);
        Dataset dataset = datasetHandler.find(id);
        dataset.setDataEntry(dataEntryList);
        return dataset;
    }

     public Dataset find(Object id){
        return find(id, null,null,null,null);
     }

}