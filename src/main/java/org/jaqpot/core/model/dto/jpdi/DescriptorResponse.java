package org.jaqpot.core.model.dto.jpdi;


import org.jaqpot.core.model.dto.dataset.Dataset;

public class DescriptorResponse {

    Dataset responseDataset;

    public Dataset getResponseDataset() {
        return responseDataset;
    }

    public void setResponseDataset(Dataset responseDataset) {
        this.responseDataset = responseDataset;
    }
}
