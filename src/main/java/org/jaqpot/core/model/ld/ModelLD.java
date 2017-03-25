/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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
package org.jaqpot.core.model.ld;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Charalampos Chomenidis
 * @author Angelos Valsamis
 * 
 */
public class ModelLD extends BaseLD {

    public static final String ONT_TYPE = "enm:ENM_8000076";

    private List<String> dependentFeatures;
    private List<String> independentFeatures;
    private List<String> predictedFeatures;

    private String algorithm;

    private String doaModel;
    private List<String> transformationModels;
    private List<String> linkedModels;

    private Map<String, Object> parameters;

    public ModelLD(String url) {
        this.id = url;
        this.type = ONT_TYPE;
        this.context.put("dependentFeatures", new TypeLD("enm:ENM_8000084", "@id"));
        this.context.put("independentFeatures", new TypeLD("enm:ENM_8000085", "@id"));
        this.context.put("predictedFeatures", new TypeLD("enm:ENM_8000089", "@id"));

        this.context.put("algorithm", new TypeLD("ot:algorithm", "@id"));

        this.context.put("doaModel", new TypeLD("enm:ENM_8000076", "@id"));
        this.context.put("transformationModels", new TypeLD("enm:ENM_8000076", "@id"));
        this.context.put("linkedModels", new TypeLD("enm:ENM_8000076", "@id"));
        this.context.put("parameters", new TypeLD("enm:ENM_8000088", "@id"));
    }

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

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
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

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

}
