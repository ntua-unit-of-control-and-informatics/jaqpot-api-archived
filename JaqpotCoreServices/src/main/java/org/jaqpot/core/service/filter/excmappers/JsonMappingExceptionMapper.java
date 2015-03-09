/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.filter.excmappers;

import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.builder.ErrorReportBuilder;

/**
 *
 * @author chung
 */
@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException>{

    @Override
    public Response toResponse(JsonMappingException exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String details = sw.toString();
        ErrorReport error = ErrorReportBuilder.builderRandomUuid()
                .setCode("JsonMappingError")
                .setMessage(exception.getMessage())
                .setDetails(details)
                .setHttpStatus(400)
                .build();
        return Response
                .ok(error, MediaType.APPLICATION_JSON)
                .status(Response.Status.BAD_REQUEST)
                .build();     
    }
    
}
