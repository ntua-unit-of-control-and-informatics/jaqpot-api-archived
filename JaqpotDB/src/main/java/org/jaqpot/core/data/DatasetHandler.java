/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.DatasetFactory;

/**
 *
 * @author hampos
 */
@Stateless
public class DatasetHandler extends AbstractHandler<Dataset> {

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public DatasetHandler() {
        super(Dataset.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }

    public Dataset find(Object id, Integer rowStart, Integer rowMax, Integer colStart, Integer colMax, String stratify, Long seed, Integer folds, String targetFeature) {
        Dataset dataset = em.find(Dataset.class, id);
        if (dataset == null) {
            return null;
        }

        switch (stratify) {
            case "random":
                dataset = DatasetFactory.randomize(dataset, seed);
                break;
            case "normal":
                dataset = DatasetFactory.stratify(dataset, folds, targetFeature);
                break;

            case "default":
                break;
        }

        if (rowStart == null) {
            rowStart = 0;
        }
        if (colStart == null) {
            colStart = 0;
        }
        dataset.setTotalRows(dataset.getDataEntry().size());
        dataset.setTotalColumns(dataset.getDataEntry().stream().findFirst().get().getValues().size());

        if (rowMax == null || rowMax > dataset.getTotalRows()) {
            rowMax = dataset.getTotalRows();
        }
        if (colMax == null || colMax > dataset.getTotalColumns()) {
            colMax = dataset.getTotalColumns();
        }

        dataset.setDataEntry(dataset.getDataEntry().subList(rowStart, rowStart + rowMax));

        for (DataEntry de : dataset.getDataEntry()) {
            TreeMap<String, Object> values = (TreeMap) de.getValues();
            NavigableSet<String> valuesSet = values.navigableKeySet();

            Iterator<String> it = valuesSet.iterator();
            for (int i = 0; i < colStart; i++) {
                it.next();
                it.remove();
            }
            for (int i = 0; i < colMax; i++) {
                it.next();
            }
            while (it.hasNext()) {
                it.next();
                it.remove();
            }

        }

//        DataEntry blank = new DataEntry();
//        blank.setValues(new TreeMap<>());
//        DataEntry firstEntry = dataset.getDataEntry().stream().findFirst().orElse(blank);
//        dataset.getFeatures().keySet().retainAll(firstEntry.getValues().keySet());                
        return dataset;
    }

}
