package org.jaqpot.core.model.dto.jpdi;

import java.util.Map;

/**
 * Created by Angelos Valsamis on 3/5/2017.
 */
public class CalculateRequest {
    byte[] file;
    private Map<String, Object> parameters;

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
