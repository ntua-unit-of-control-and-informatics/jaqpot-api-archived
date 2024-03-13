/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.dto.models;

import java.util.ArrayList;

/**
 *
 * @author pantelispanka
 */
public class ChempotDto {
    
    private String smiles;
    private ArrayList<String> smilesArray;
    private String modelId;
    private boolean withDoa;
    private String descriptors;

    public ArrayList<String> getSmilesArray() {
        return smilesArray;
    }

    public void setSmilesArray(ArrayList<String> smilesArray) {
        this.smilesArray = smilesArray;
    }

    public String getSmiles() {
        return smiles;
    }

    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public boolean isWithDoa() {
        return withDoa;
    }

    public void setWithDoa(boolean withDoa) {
        this.withDoa = withDoa;
    }

    public String getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(String descriptors) {
        this.descriptors = descriptors;
    }
        
}
