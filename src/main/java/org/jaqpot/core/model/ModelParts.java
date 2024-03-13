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
public class ModelParts extends JaqpotEntity {
    
    
    private String modelId;
    private String userId;
    private String part;
    private int totalParts;
    private int partNumber;

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public int getTotalParts() {
        return totalParts;
    }

    public void setTotalParts(int totalParts) {
        this.totalParts = totalParts;
    }
    
}
