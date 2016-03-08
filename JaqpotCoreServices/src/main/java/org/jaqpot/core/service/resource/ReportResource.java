/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
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
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.data.ReportHandler;
import org.jaqpot.core.service.annotations.Authorize;

/**
 *
 * @author hampos
 */
@Path("report")
@Api(value = "/report", description = "Report API")
@Produces(MediaType.APPLICATION_JSON)
@Authorize
public class ReportResource {

    @Context
    SecurityContext securityContext;

    @EJB
    ReportHandler reportHandler;

    @GET
    @ApiOperation(value = "Retrieves Reports of User")
    public Response getReports(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", defaultValue = "20") @QueryParam("max") Integer max
    ) {
        if (max == null || max > 500) {
            max = 500;
        }
        String userName = securityContext.getUserPrincipal().getName();
        return Response.ok(reportHandler.listOnlyIDsOfCreator(userName, start != null ? start : 0, max))
                .header("total", reportHandler.countAllOfCreator(userName))
                .build();

    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Retrieves Report by id")
    public Response getReport(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @PathParam("id") String id) {

        return Response.ok(reportHandler.find(id)).build();
    }

}
