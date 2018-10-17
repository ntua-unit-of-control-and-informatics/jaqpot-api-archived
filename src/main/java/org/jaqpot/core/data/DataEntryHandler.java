package org.jaqpot.core.data;

import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.*;

@Stateless
public class DataEntryHandler extends AbstractHandler<DataEntry> {

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public DataEntryHandler() {
        super(DataEntry.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }


    public List<DataEntry> getDataEntriesByDatasetId(String datasetId, int start, int end) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("datasetId", datasetId);

        return em.find(DataEntry.class, properties, start, end);
    }

    public List<DataEntry> findDataEntriesByDatasetId(Object id, Integer rowStart, Integer rowMax, Integer colStart, Integer colMax) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("datasetId", id);
        if (rowMax==null)
            rowMax=Math.toIntExact(em.count(DataEntry.class,properties));
        List<DataEntry> dataEntries = em.find(DataEntry.class, properties, rowStart, rowMax);
        if (dataEntries.isEmpty()) {
            return null;
        }

        if (colStart == null) {
            colStart = 0;
        }

        if (colMax == null || colStart + colMax > dataEntries.get(0).getValues().size()) {
            colMax = dataEntries.get(0).getValues().size() - colStart;
        }

        for (int j = 0; j < dataEntries.size(); j++) {
            DataEntry de = dataEntries.get(j);
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
        return dataEntries;
    }
}
