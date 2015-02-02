/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licenced
 * with the aforementioned licence. 
 */
package org.jaqpot.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author chung
 */
public class Model extends JaqpotEntity {
    
    private List<String> dependentFeatures = new ArrayList<>();
    private List<String> independentFeatures = new ArrayList<>();
    private List<String> predictedFeatures = new ArrayList<>();
    private User createdBy;
    private Integer reliability = 0;
    private String datasetUri;
    private Set<Parameter> parameters = new HashSet<>();
    private Algorithm algorithm;
    private BibTeX bibtex;
    
    private String actualModel;
    private String pmmlModel;
    private String pmmlTransformations;        

    public List<String> getDependentFeatures() {
        return dependentFeatures;
    }

    public void setDependentFeatures(List<String> dependentFeatures) {
        this.dependentFeatures = dependentFeatures;
    }

    public List<String> getIndependentFeatures() {
        return independentFeatures;
    }

    public void setIndependentFeatures(List<String> independentFeatures) {
        this.independentFeatures = independentFeatures;
    }

    public List<String> getPredictedFeatures() {
        return predictedFeatures;
    }

    public void setPredictedFeatures(List<String> predictedFeatures) {
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
