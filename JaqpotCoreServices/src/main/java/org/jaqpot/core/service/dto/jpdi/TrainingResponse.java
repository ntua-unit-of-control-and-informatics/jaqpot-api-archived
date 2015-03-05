/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.dto.jpdi;

import java.util.List;

/**
 *
 * @author hampos
 */
public class TrainingResponse {

    private String rawModel;
    private String pmmlModel;
    List<String> independentFeatures;

    public String getRawModel() {
        return rawModel;
    }

    public void setRawModel(String rawModel) {
        this.rawModel = rawModel;
    }

    public String getPmmlModel() {
        return pmmlModel;
    }

    public void setPmmlModel(String pmmlModel) {
        this.pmmlModel = pmmlModel;
    }

    public List<String> getIndependentFeatures() {
        return independentFeatures;
    }

    public void setIndependentFeatures(List<String> independentFeatures) {
        this.independentFeatures = independentFeatures;
    }

}
