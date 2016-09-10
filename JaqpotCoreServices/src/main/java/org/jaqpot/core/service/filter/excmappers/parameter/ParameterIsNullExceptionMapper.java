package org.jaqpot.core.service.filter.excmappers.parameter;

/**
 * Created by root on 22/6/2016.
 */


import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.builder.ErrorReportBuilder;
import org.jaqpot.core.service.exceptions.parameter.ParameterIsNullException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ParameterIsNullExceptionMapper implements ExceptionMapper<ParameterIsNullException> {
    private static final Logger LOG = Logger.getLogger(ParameterIsNullExceptionMapper.class.getName());

    @Override
    public Response toResponse(ParameterIsNullException exception) {
        LOG.log(Level.INFO, "IsNull exception caught", exception);
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String details = sw.toString();
        ErrorReport error = ErrorReportBuilder.builderRandomId()
                .setCode("IsNullException")
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