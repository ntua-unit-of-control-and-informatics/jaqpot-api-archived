package org.jaqpot.core.service.filter.excmappers;

/**
 * Created by root on 13/6/2016.
 */
/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence.
 */

import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.factory.ErrorReportFactory;

import javax.ejb.EJBException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;



@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException> {

    private static final Logger LOG = Logger.getLogger(EJBExceptionMapper.class.getName());

    @Override
    public Response toResponse(EJBException exception) {

        Exception ne = (Exception) exception.getCause();

        LOG.log(Level.FINEST, "Runtime exception caught", exception);
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String details = sw.toString();
        ErrorReport error = ErrorReportFactory.badRequest(exception.getMessage(), details);
        error.setDetails(details);

        if (ne instanceof IllegalArgumentException)
            error.setCode("IllegalArgumentException");
        else
            error.setCode("RuntimeException");
        error.setHttpStatus(404);
        return Response
                .ok(error, MediaType.APPLICATION_JSON)
                .status(Response.Status.BAD_REQUEST)
                .build();
    }
}
