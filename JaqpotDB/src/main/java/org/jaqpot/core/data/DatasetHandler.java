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
import org.jaqpot.core.model.dto.dataset.Dataset;

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

    public Dataset find(Object id, Integer rowStart, Integer rowMax, Integer colStart, Integer colMax) {

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> rows = new HashMap<>();
        rows.put("$slice", new int[]{rowStart, rowMax});
        properties.put("dataEntry", rows);

        Dataset dataset = em.find(Dataset.class, id, properties);

        dataset.getDataEntry().forEach(de -> {
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

        });

        return dataset;
    }

}
