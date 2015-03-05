package org.jaqpot.core.service.dto.study;

import java.util.Map;

public class Effects {

    private Map<String, Object> conditions;
    private String endpoint;
    private Result result;

    public Map<String, Object> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, Object> conditions) {
        this.conditions = conditions;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
