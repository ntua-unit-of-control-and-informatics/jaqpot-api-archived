/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core;

/**
 *
 * @author chung
 */
public class FeatureValue {    
    
    private String feature;
    private Double lowValue;
    private Double highValue;
    private Double stdError;
    private String valueType;
    private String stringValue;
    private BibTeX bibtex;
    private User createdBy;
    
    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
