/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.dto.jpdi;

import org.jaqpot.core.service.dto.dataset.Dataset;

/**
 *
 * @author hampos
 */
public class PredictionRequest {

    Dataset dataset;
    String rawModel;

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public String getRawModel() {
        return rawModel;
    }

    public void setRawModel(String rawModel) {
        this.rawModel = rawModel;
    }

}
