/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.dto.dataset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hampos
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset {
    
    String datasetURI;
    
    List<DataEntry> dataEntry;

    public String getDatasetURI() {
        return datasetURI;
    }

    public void setDatasetURI(String datasetURI) {
        this.datasetURI = datasetURI;
    }

    public List<DataEntry> getDataEntry() {
        return dataEntry;
    }

    public void setDataEntry(List<DataEntry> dataEntry) {
        this.dataEntry = dataEntry;
    }

    @Override
    public String toString() {
        return "Dataset{" + "datasetURI=" + datasetURI + ", dataEntry=" + dataEntry + '}';
    }
    
    
}
