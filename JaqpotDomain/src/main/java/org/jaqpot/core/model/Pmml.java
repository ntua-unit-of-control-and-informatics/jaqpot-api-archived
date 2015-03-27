/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author chung
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pmml extends JaqpotEntity {

    private String pmml;

    private String createdBy;

    public Pmml() {
    }

    public Pmml(String id) {
        super(id);
    }

    public Pmml(Pmml other) {
        super(other);
        this.pmml = other.pmml;
    }

    public String getPmml() {
        return pmml;
    }

    public void setPmml(String pmml) {
        this.pmml = pmml;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

}
