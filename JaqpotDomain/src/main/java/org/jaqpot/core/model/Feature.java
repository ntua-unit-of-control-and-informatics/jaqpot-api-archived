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

import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Feature: The definition of a property, either measured, predicted or
 * computed for a substance.
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Feature extends JaqpotEntity {

    private String units;
    private String createdBy;
    /**
     * In case the feature is nominal, this field stores it admissible values.
     * Whether the field is Nominal or Numeric or String is determined by its
     * ontological classes which can be retrieved from its superclass,
     * {@link JaqpotEntity}.
     */
    private Set<String> admissibleValues;

    public Feature() {
    }       

    public Feature(String id) {
        super(id);
    }
        
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

}
