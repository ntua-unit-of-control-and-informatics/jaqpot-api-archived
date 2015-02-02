/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

import java.util.Map;

/**
 *
 * @author chung
 */
public abstract class Substance extends JaqpotEntity {
    
    private Map<String, FeatureValue> features;
    private Map<String, FeatureValue> predictedFeatures;

    private String createdBy;

}
