package org.jaqpot.core.service.dto.study;

import java.util.List;

public class Protocol {

    private Category category;
    private String endpoint;
    private List<String> guideline;
    private String topcategory;

    public Category getCategory() {
        return this.category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<String> getGuideline() {
        return this.guideline;
    }

    public void setGuideline(List<String> guideline) {
        this.guideline = guideline;
    }

    public String getTopcategory() {
        return this.topcategory;
    }

    public void setTopcategory(String topcategory) {
        this.topcategory = topcategory;
    }
}
