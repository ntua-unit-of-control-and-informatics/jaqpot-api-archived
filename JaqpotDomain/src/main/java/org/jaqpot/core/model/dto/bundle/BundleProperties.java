/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.dto.bundle;

import java.util.Map;

/**
 *
 * @author hampos
 */
public class BundleProperties {

    private Map<String, Object> feature;

    public Map<String, Object> getFeature() {
        return feature;
    }

    public void setFeature(Map<String, Object> feature) {
        this.feature = feature;
    }

}
