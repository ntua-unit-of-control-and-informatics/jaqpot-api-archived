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
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.factory.ErrorReportFactory;

/**
 *
 * @author chung
 */
@Path("bibtex")
@Api(value = "/bibtex", description = "BibTeX API")
@Produces({"application/json", "text/uri-list"})
public class BibTeXResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all BibTeX entries",
            notes = "Finds all BibTeX entries in the DB of Jaqpot",
            response = BibTeX.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entries found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getBibTeXs(
            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("subjectid") String subjectId
    ) {
        return Response
                .ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }
    
    
}
