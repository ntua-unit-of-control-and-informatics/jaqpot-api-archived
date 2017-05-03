package org.jaqpot.core.model.dto.jpdi;


import org.jaqpot.core.model.dto.dataset.Dataset;

/**
 * Created by Angelos Valsamis on 3/4/2017.
 */
public class CalculateResponse {

    Dataset entries;

    public Dataset getEntries() {
        return entries;
    }

    public void setEntries(Dataset entries) {
        this.entries = entries;
    }
}
