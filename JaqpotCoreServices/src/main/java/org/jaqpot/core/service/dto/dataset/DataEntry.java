/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.dto.dataset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hampos
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataEntry {

    Substance compound;

    Map<String, Object> values;

    public Substance getCompound() {
        return compound;
    }

    public void setCompound(Substance compound) {
        this.compound = compound;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "DataEntry{" + "compound=" + compound + ", values=" + values + '}';
    }

}
