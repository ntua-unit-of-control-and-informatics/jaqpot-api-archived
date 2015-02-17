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

import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Substance extends JaqpotEntity {

    /**
     * Set of features and feature values (mapping) for this feature. These are
     * actual values (experimental or descriptors)
     */
    private Map<String, FeatureValue> features;
    /**
     * Predicted features.
     */
    private Map<String, FeatureValue> predictedFeatures;
    /**
     * ID of the user who created this substance.
     */
    private String createdBy;

    public Substance() {
    }

    public Substance(String id) {
        super(id);
    }

    public Map<String, FeatureValue> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, FeatureValue> features) {
        this.features = features;
    }

    public Map<String, FeatureValue> getPredictedFeatures() {
        return predictedFeatures;
    }

    public void setPredictedFeatures(Map<String, FeatureValue> predictedFeatures) {
        this.predictedFeatures = predictedFeatures;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

}
