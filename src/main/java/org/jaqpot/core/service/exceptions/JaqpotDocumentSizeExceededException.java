package org.jaqpot.core.service.exceptions;

import org.jaqpot.core.model.factory.ErrorReportFactory;

public class JaqpotDocumentSizeExceededException extends Exception{

    public JaqpotDocumentSizeExceededException() {
    }

    public JaqpotDocumentSizeExceededException(String message) {
        super(message);
    }

}
