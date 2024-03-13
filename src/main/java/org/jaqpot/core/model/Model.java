/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.jaqpot.core.model;

import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement(name = "Model")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Model extends JaqpotEntity {

    /**
     *  The type of the model wether a molecular or material or a simple model
     */
    private String type;
    
    /**
     *  JaqpotPy version
    */
    private String jaqpotpyVersion;
    
    
    /**
     *  JaqpotPy version
    */
    private String jaqpotpyDockerVersion;
    
    
    private List<String> libraries;
    
    private List<String> libraryVersions;
    
    /**
     * List of dependent features of the model.
     */
    private List<String> dependentFeatures;

//    private List<String> linkedIndependendFeatures;
    /**
     * List of independent features.
     */
    private List<String> independentFeatures;
    /**
     * List of predicted features.
     */
    private List<String> predictedFeatures;

    /**
     * Reliability of the model (ranking).
     */
    private Integer reliability = 0;
    /**
     * URI of the dataset of this model.
     */
    private String datasetUri;
    /**
     * Set of parameters of this model.
     */
    private Map<String, Object> parameters;
    /**
     * Algorithm that was used to create this model.
     */
    private Algorithm algorithm;
    /**
     * BibTeX reference where one can find published info about the model.
     */
    private BibTeX bibtex;
    /**
     * The actual model as a string (ASCII).
     */
    private Object actualModel;
    /**
     * PMML representation of the model itself. Equivalent to the actualModel,
     * but in PMML format.
     */
    private Object pmmlModel;
    /**
     * A PMML defining the transformations of input features.
     */

    private Object additionalInfo;

    private String pmmlTransformations;

    private String doaModel;

    private List<String> transformationModels;

    private List<String> linkedModels;

    private Boolean pretrained;

    /**
     * The language that the model is implemented with
     */
    private String implementedIn;

    /**
     * The library that the model is implemented with
     */
    private String implementedWith;
    /**
     * The algorithm a pre trained model is created ad given by the user
     */
    private String algorithmForPretrained;

    private Boolean onTrash;
    
    private Boolean indexed;
    
    private String planguage;
    
    public Model() {
    }

    public Model(String id) {
        super(id);
    }

    /**
     * Copy-constructor for Model objects.
     *
     * @param other model to be copied
     *
     * @see #getPmmlModel()
     * @see #getPmmlTransformations()
     * @see #getActualModel()
     */
    public Model(Model other) {
        super(other);
        this.type = other.type;
        this.algorithm = other.algorithm != null ? new Algorithm(other.algorithm) : null;
        this.bibtex = other.bibtex != null ? new BibTeX(other.bibtex) : null;
        this.datasetUri = other.datasetUri;
        this.dependentFeatures = other.dependentFeatures != null
                ? new ArrayList<>(other.dependentFeatures) : null;
        this.independentFeatures = other.independentFeatures != null
                ? new ArrayList<>(other.independentFeatures) : null;
        this.parameters = other.parameters != null
                ? new HashMap<>(other.parameters) : null;
        this.predictedFeatures = other.predictedFeatures != null
                ? new ArrayList<>(other.predictedFeatures) : null;
        this.reliability = other.reliability;
        this.actualModel = other.actualModel;
        this.pmmlModel = other.pmmlModel;
        this.pmmlTransformations = other.pmmlTransformations;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJaqpotpyVersion() {
        return jaqpotpyVersion;
    }

    public void setJaqpotpyVersion(String jaqpotpyVersion) {
        this.jaqpotpyVersion = jaqpotpyVersion;
    }

    public String getJaqpotpyDockerVersion() {
        return jaqpotpyDockerVersion;
    }

    public void setJaqpotpyDockerVersion(String jaqpotpyDockerVersion) {
        this.jaqpotpyDockerVersion = jaqpotpyDockerVersion;
    }
    
    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public List<String> getLibraryVersions() {
        return libraryVersions;
    }

    public void setLibraryVersions(List<String> libraryVrsions) {
        this.libraryVersions = libraryVrsions;
    }
    
    public List<String> getDependentFeatures() {
        return dependentFeatures;
    }

    public void setDependentFeatures(List<String> dependentFeatures) {
        this.dependentFeatures = dependentFeatures;
    }

//    public List<String> getLinkedIndependendFeatures() {
//        return linkedIndependendFeatures;
//    }
//
//    public void setLinkedIndependendFeatures(List<String> linkedIndependendFeatures) {
//        this.linkedIndependendFeatures = linkedIndependendFeatures;
//    }
//    
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

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
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

    public Object getActualModel() {
        return actualModel;
    }

    public void setActualModel(Object actualModel) {
        this.actualModel = actualModel;
    }

    public Object getPmmlModel() {
        return pmmlModel;
    }

    public void setPmmlModel(Object pmmlModel) {
        this.pmmlModel = pmmlModel;
    }

    public Object getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Object additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getPmmlTransformations() {
        return pmmlTransformations;
    }

    public void setPmmlTransformations(String pmmlTransformations) {
        this.pmmlTransformations = pmmlTransformations;
    }

    public String getDoaModel() {
        return doaModel;
    }

    public void setDoaModel(String doaModel) {
        this.doaModel = doaModel;
    }

    public List<String> getTransformationModels() {
        return transformationModels;
    }

    public void setTransformationModels(List<String> transformationModels) {
        this.transformationModels = transformationModels;
    }

    public List<String> getLinkedModels() {
        return linkedModels;
    }

    public void setLinkedModels(List<String> linkedModels) {
        this.linkedModels = linkedModels;
    }

    public Boolean getPretrained() {
        return pretrained;
    }

    public void setPretrained(Boolean pretrained) {
        this.pretrained = pretrained;
    }

    public String getImplementedIn() {
        return implementedIn;
    }

    public void setImplementedIn(String implementedIn) {
        this.implementedIn = implementedIn;
    }

    public String getImplementedWith() {
        return implementedWith;
    }

    public void setImplementedWith(String implementedWith) {
        this.implementedWith = implementedWith;
    }

    public String getAlgorithmForPretrained() {
        return algorithmForPretrained;
    }

    public void setAlgorithmForPretrained(String algorithmForPretrained) {
        this.algorithmForPretrained = algorithmForPretrained;
    }
    
    public Boolean getOnTrash() {
        return onTrash;
    }

    public void setOnTrash(Boolean onTrash) {
        this.onTrash = onTrash;
    }

    public Boolean getIndexed() {
        return indexed;
    }

    public void setIndexed(Boolean indexed) {
        this.indexed = indexed;
    }

    public String getPlanguage() {
        return planguage;
    }

    public void setPlanguage(String planguage) {
        this.planguage = planguage;
    }

}
