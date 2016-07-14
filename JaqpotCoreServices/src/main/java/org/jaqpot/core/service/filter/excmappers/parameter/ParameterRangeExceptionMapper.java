package org.jaqpot.core.service.filter.excmappers.parameter;

import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.builder.ErrorReportBuilder;
import org.jaqpot.core.service.exceptions.parameter.ParameterRangeException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by root on 12/7/2016.
 */
@Provider

public class ParameterRangeExceptionMapper implements ExceptionMapper<ParameterRangeException> {
    private static final Logger LOG = Logger.getLogger(ParameterRangeExceptionMapper.class.getName());
    @Override
    public Response toResponse(ParameterRangeException exception) {
        LOG.log(Level.INFO, "ParameterRange exception caught", exception);
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String details = sw.toString();
        ErrorReport error = ErrorReportBuilder.builderRandomId()
                .setCode("ParameterRangeException")
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

