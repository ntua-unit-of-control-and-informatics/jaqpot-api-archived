/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.dto.dataset;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author hampos
 */
public class FeatureInfo {

    private String URI;
    private String name;
    private String units;
    private Map<String, Object> conditions;

    public FeatureInfo() {
    }

    public FeatureInfo(String URI, String name) {
        this.URI = URI;
        this.name = name;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Map<String, Object> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, Object> conditions) {
        this.conditions = conditions;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.URI);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FeatureInfo other = (FeatureInfo) obj;
        if (!Objects.equals(this.URI, other.URI)) {
            return false;
        }
        return true;
    }

}
