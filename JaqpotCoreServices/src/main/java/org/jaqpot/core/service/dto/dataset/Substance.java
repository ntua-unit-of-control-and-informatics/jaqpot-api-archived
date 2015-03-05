/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.dto.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hampos
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Substance {

    @JsonProperty("URI")
    String URI;

    @JsonIgnore
    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    @Override
    public String toString() {
        return "Substance{" + "URI=" + URI + '}';
    }

}
