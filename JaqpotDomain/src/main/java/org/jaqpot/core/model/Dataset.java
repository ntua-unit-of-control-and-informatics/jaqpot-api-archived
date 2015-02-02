/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

import java.util.Set;

/**
 *
 * @author chung
 */
public class Dataset extends JaqpotEntity {

    private Set<String> substances;
    private Set<String> features;
    private Set<String> predictedFeatures;

    public Set<String> getSubstances() {
        return substances;
    }

    public void setSubstances(Set<String> substances) {
        this.substances = substances;
    }

    public Set<String> getFeatures() {
        return features;
    }

    public void setFeatures(Set<String> features) {
        this.features = features;
    }

    public Set<String> getPredictedFeatures() {
        return predictedFeatures;
    }

    public void setPredictedFeatures(Set<String> predictedFeatures) {
        this.predictedFeatures = predictedFeatures;
    }

}
