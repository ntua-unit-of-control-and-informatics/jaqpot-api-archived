package org.jaqpot.core.service.dto.study;

public class Result {

    private String loQualifier;
    private Number loValue;
    private String upQualifier;
    private Number upValue;
    private String unit;

    public String getLoQualifier() {
        return this.loQualifier;
    }

    public void setLoQualifier(String loQualifier) {
        this.loQualifier = loQualifier;
    }

    public Number getLoValue() {
        return this.loValue;
    }

    public void setLoValue(Number loValue) {
        this.loValue = loValue;
    }

    public String getUpQualifier() {
        return upQualifier;
    }

    public void setUpQualifier(String upQualifier) {
        this.upQualifier = upQualifier;
    }

    public Number getUpValue() {
        return upValue;
    }

    public void setUpValue(Number upValue) {
        this.upValue = upValue;
    }

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
