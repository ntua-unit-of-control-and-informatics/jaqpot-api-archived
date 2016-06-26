package org.jaqpot.core.service.filter.excmappers;

/**
 * Created by root on 22/6/2016.
 */


import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.builder.ErrorReportBuilder;
import org.jaqpot.core.service.exceptions.IsNullException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class IsNullExceptionMapper implements ExceptionMapper<IsNullException> {
    private static final Logger LOG = Logger.getLogger(IsNullExceptionMapper.class.getName());

    @Override
    public Response toResponse(IsNullException exception) {
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