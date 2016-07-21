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

import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Parameter extends JaqpotEntity {

    /**
     * The scope of a parameter can either be {@link #MANDATORY Mandatory} or
     * {@link #OPTIONAL Optional}.
     */
    public enum Scope {

        /**
         * If a parameter is tagged as 'Optional' then the client does not need
         * to provide its value explicitly but instead a default value will be
         * used.
         */
        OPTIONAL,
        /**
         * A parameter is mandatory when the user has to provide it's value and
         * no default values can be assigned to it.
         */
        MANDATORY;
    };



    /**
     * Name of the parameter.
     */
    private String name;
    /**
     * Parameter's value. Default value if it is a parameter of an algorithm, or
     * actual value if it is the parameter of a model.
     */
    private Object value;
    /**
     * Scope of the parameter (optional/mandatory).
     */
    private Scope scope;

    private List<Object> allowedValues;

    private Object minValue;
    private Object maxValue;

    private Integer minArraySize;
    private Integer maxArraySize;

    private String description;

    public Parameter() {
    }

    public Parameter(String id) {
        super(id);
    }

    public Parameter(Parameter other) {
        super(other);
        this.name = other.name;
        this.scope = other.scope;
        this.value = other.value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public List<Object> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<Object> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public Object getMinValue() {
        return minValue;
    }

    public void setMinValue(Object minValue) {
        this.minValue = minValue;
    }

    public Object getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getMinArraySize() {
        return minArraySize;
    }

    public void setMinArraySize(Integer minArraySize) {
        this.minArraySize = minArraySize;
    }

    public Integer getMaxArraySize() {
        return maxArraySize;
    }

    public void setMaxArraySize(Integer maxArraySize) {
        this.maxArraySize = maxArraySize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
