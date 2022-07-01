package org.jaqpot.core.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author pantelispanka
 */
public class ResponseData {
    private final String responseState;
    private final String responseReport;
    private final String errorDetails;

    @JsonCreator
    public ResponseData(
        @JsonProperty("Response_State") final String responseState,
        @JsonProperty("Response_Report") final String responseReport,
        @JsonProperty("Error_details") final String errorDetails) {
        this.responseState = responseState;
        this.responseReport = responseReport;
        this.errorDetails = errorDetails;
    }

    public String getResponseState() {
        return this.responseState;
    }

    public String getResponseReport() {
        return this.responseReport;
    }

    public String getErrorDetails() {
        return this.errorDetails;
    }

    @Override
    public String toString() {
        return String.format(
            "ResponseData: responseState: %s; responseReport: %s; errorDetails: %s",
            this.responseState,
            this.responseReport,
            this.errorDetails
        );
    }
}
