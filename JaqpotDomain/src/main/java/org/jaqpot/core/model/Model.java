/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author chung
 */
public class Model extends JaqpotEntity {
    
    private Set<String> dependentFeatures;
    private Set<String> independentFeatures;
    private Set<String> predictedFeatures;
    private User createdBy;
    private Integer reliability = 0;
    private String datasetUri;
    private Set<Parameter> parameters;
    private Algorithm algorithm;
    private BibTeX bibtex;
    
    private String actualModel;
    private String pmmlModel;
    private String pmmlTransformations;        

    public Set<String> getDependentFeatures() {
        return dependentFeatures;
    }

    public void setDependentFeatures(Set<String> dependentFeatures) {
        this.dependentFeatures = dependentFeatures;
    }

    public Set<String> getIndependentFeatures() {
        return independentFeatures;
    }

    public void setIndependentFeatures(Set<String> independentFeatures) {
        this.independentFeatures = independentFeatures;
    }

    public Set<String> getPredictedFeatures() {
        return predictedFeatures;
    }

    public void setPredictedFeatures(Set<String> predictedFeatures) {
        this.predictedFeatures = predictedFeatures;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getReliability() {
        return reliability;
    }

    public void setReliability(Integer reliability) {
        this.reliability = reliability;
    }

    public String getDatasetUri() {
        return datasetUri;
    }

    public void setDatasetUri(String datasetUri) {
        this.datasetUri = datasetUri;
    }

    public Set<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Set<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public BibTeX getBibtex() {
        return bibtex;
    }

    public void setBibtex(BibTeX bibtex) {
        this.bibtex = bibtex;
    }

    public String getActualModel() {
        return actualModel;
    }

    public void setActualModel(String actualModel) {
        this.actualModel = actualModel;
    }

    public String getPmmlModel() {
        return pmmlModel;
    }

    public void setPmmlModel(String pmmlModel) {
        this.pmmlModel = pmmlModel;
    }

    public String getPmmlTransformations() {
        return pmmlTransformations;
    }

    public void setPmmlTransformations(String pmmlTransformations) {
        this.pmmlTransformations = pmmlTransformations;
    }
    
    
}
