package org.jaqpot.core.service.dto.study;

import java.util.List;
import java.util.Map;

public class Study {

    private List effects;
    private Interpretation interpretation;
    private Owner owner;
    private Map<String, Object> parameters;
    private Protocol protocol;
    private String uuid;

    public List getEffects() {
        return this.effects;
    }

    public void setEffects(List effects) {
        this.effects = effects;
    }

    public Interpretation getInterpretation() {
        return this.interpretation;
    }

    public void setInterpretation(Interpretation interpretation) {
        this.interpretation = interpretation;
    }

    public Owner getOwner() {
        return this.owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
