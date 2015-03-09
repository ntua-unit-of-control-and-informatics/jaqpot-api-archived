/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.filter.excmappers;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.factory.ErrorReportFactory;

/**
 *
 * @author chung
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String details = sw.toString();
        ErrorReport error = ErrorReportFactory.notFoundError(details);
        error.setMessage(exception.getMessage());
        return Response
                .ok(error, MediaType.APPLICATION_JSON)
                .status(Response.Status.NOT_FOUND)
                .build();
    }

}
