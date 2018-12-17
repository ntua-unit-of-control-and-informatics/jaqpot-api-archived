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

import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashSet;
import java.util.List;
import org.jaqpot.core.model.dto.dataset.Dataset;

/**
 * Feature: The definition of a property, either measured, predicted or computed
 * for a substance.
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Feature extends JaqpotEntity {

    /**
     * Units of measurement.
     */
    private String units;
    /**
     * In case the feature is a prediction feature, this field is used to refer
     * to the original feature that is predicted. This field will point to a
     * URI.
     */
    private String predictorFor;

    /**
     * In case the feature is nominal, this field stores it admissible values.
     * Whether the field is Nominal or Numeric or String is determined by its
     * ontological classes which can be retrieved from its superclass,
     * {@link JaqpotEntity}.
     */
    private Set<String> admissibleValues;
//    /**
//     * In case the feature is produced from a pretrained model, 
//     * this field stores it's actual name that is needed for predictions.
//     * {@link JaqpotEntity}.
//     */
//    private String actualIndependentFeatureName;
    
//    private Boolean fromPretrained;
    private org.jaqpot.core.model.dto.dataset.Dataset.DescriptorCategory category;
    
    
    public Feature() {
    }

    public Feature(String id) {
        super(id);
    }

    public Feature(Feature other) {
        super(other);
        this.admissibleValues = other.admissibleValues != null ? new HashSet<>(other.admissibleValues) : null;
        this.units = other.units;
        this.predictorFor = other.predictorFor;
        this.setMeta(other.getMeta());

    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setAdmissibleValues(Set<String> admissibleValues) {
        this.admissibleValues = admissibleValues;
    }

    public Set<String> getAdmissibleValues() {
        return admissibleValues;
    }

    public String getPredictorFor() {
        return predictorFor;
    }

    public void setPredictorFor(String predictorFor) {
        this.predictorFor = predictorFor;
    }
    
    

//    public String getActualIndependentFeatureName() {
//        return actualIndependentFeatureName;
//    }
//
//    public void setActualIndependentFeatureName(String actualIndependentFeatureName) {
//        this.actualIndependentFeatureName = actualIndependentFeatureName;
//    }
//
//    public Boolean getFromPretrained() {
//        return fromPretrained;
//    }
//
//    public void setFromPretrained(Boolean fromPretrained) {
//        this.fromPretrained = fromPretrained;
//    }

    public Dataset.DescriptorCategory getCategory() {
        return category;
    }

    public void setCategory(Dataset.DescriptorCategory category) {
        this.category = category;
    }

}
