/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licenced
 * with the aforementioned licence. 
 */
package org.jaqpot.core.service.filter.excmappers;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.factory.ErrorReportFactory;

/**
 * Takes care of duplicate key errors and other un-handled DB errors.
 * 
 * @author Pantelis Sopasakis
 * @author Charampos Chomenidis
 */
@Provider
public class MongoWriteExceptionMapper implements ExceptionMapper<MongoWriteException> {

    @Override
    public Response toResponse(MongoWriteException exception) {

        ErrorReport error;
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        if (ErrorCategory.DUPLICATE_KEY.equals(exception.getError().getCategory())) {
            error = ErrorReportFactory
                    .alreadyInDatabase(exception.getMessage());
            status = Response.Status.BAD_REQUEST;
        } else {
            error = ErrorReportFactory
                    .internalServerError(exception, "MongoWriteException", "", null);
        }

        return Response
                .ok(error, MediaType.APPLICATION_JSON)
                .status(status)
                .build();
    }

}
