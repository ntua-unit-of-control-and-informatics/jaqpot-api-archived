/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    @EJB
    ReportHandler reportHandler;

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Retrieves Validation Report")
    public Response getValidationReport(@PathParam("id") String id) {

        return Response.ok(reportHandler.find(id)).build();
    }

}
