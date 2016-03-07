/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import javax.ejb.EJB;
import javax.ws.rs.GET;
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
    public Response getReport(@PathParam("id") String id) {

        return Response.ok(reportHandler.find(id)).build();
    }

}
