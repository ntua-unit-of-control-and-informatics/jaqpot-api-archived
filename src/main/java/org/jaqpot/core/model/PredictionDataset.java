/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jaqpot.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author pantelispanka
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PredictionDataset extends Dataset{
    
    private boolean doa;
    private String batch;
    private Integer batchId;

    public boolean isDoa() {
        return doa;
    }

    public void setDoa(boolean doa) {
        this.doa = doa;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }
    
    
    
    
}
