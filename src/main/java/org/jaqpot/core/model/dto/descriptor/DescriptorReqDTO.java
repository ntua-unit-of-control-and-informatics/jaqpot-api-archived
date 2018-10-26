package org.jaqpot.core.model.dto.descriptor;

import io.swagger.annotations.ApiParam;

import javax.ws.rs.FormParam;
import java.util.ArrayList;
import java.util.Set;

public class DescriptorReqDTO {
    @ApiParam(name = "title of resulting dataset", required = true) @FormParam("title") private String title;
    @ApiParam(name = "description of resulting dataset", required = true) @FormParam("description") private String description;
    @ApiParam(name = "dataset_uri") @FormParam("dataset_uri") private String datasetURI;
    @FormParam("parameters") private String parameters;
    @FormParam("featureURIs") private Set<String> featureURIs;

    public DescriptorReqDTO(){}

    public DescriptorReqDTO(String title, String description, String datasetURI, String parameters, Set<String> featureURIs) {
        this.title = title;
        this.description = description;
        this.datasetURI = datasetURI;
        this.parameters = parameters;
        this.featureURIs = featureURIs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatasetURI() {
        return datasetURI;
    }

    public void setDatasetURI(String datasetURI) {
        this.datasetURI = datasetURI;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Set<String> getFeatureURIs() {
        return featureURIs;
    }

    public void setFeatureURIs(Set<String> featureURIs) {
        this.featureURIs = featureURIs;
    }
}
