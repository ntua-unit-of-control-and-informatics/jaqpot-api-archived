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
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.data.PmmlHandler;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.Pmml;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

/**
 *
 * @author chung
 */
@Path("pmml")
@Api(value = "/pmml", description = "PMML API")
public class PmmlResource {

    @EJB
    AAService aaService;

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;
    
    @EJB
    PmmlHandler pmmlHandler;

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new BibTeX entry",
            notes = "Creates a new BibTeX entry which is assigned a random unique ID",
            response = Pmml.class)
    //TODO add code for user's quota exceeded
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entry was created successfully."),
        @ApiResponse(code = 400, message = "Bad request: malformed bibtex (e.g., mandatory fields are missing)"),
        @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    @Authorize
    public Response createPMML(
            @ApiParam(value = "Clients need to authenticate in order to create resources on the server")
            @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "PMML in JSON representation.", required = true) Pmml pmml
    ) throws JaqpotNotAuthorizedException {
        // First check the subjectid:
        if (subjectId == null || !aaService.validate(subjectId)) {
            throw new JaqpotNotAuthorizedException("Invalid auth token");
        }
        if (pmml == null) {
            ErrorReport report = ErrorReportFactory.badRequest("No PMML document provided; check out the API specs",
                    "Clients MUST provide a PMML document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        if (pmml.getId() == null) {
            ROG rog = new ROG(true);
            pmml.setId(rog.nextString(10));
        }
        pmml.setCreatedBy(securityContext.getUserPrincipal().getName());
        
        pmmlHandler.create(pmml);
        return Response
                .ok(pmml)
                .status(Response.Status.CREATED)
                .header("Location", uriInfo.getBaseUri().toString() + "pmml/" + pmml.getId())
                .build();

    }
    
    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Returns PMML entry",
            notes = "Finds and returns a PMML document by ID",
            response = Pmml.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entries found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 404, message = "No such bibtex entry on the server (not found)"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getPmml(
            @ApiParam(value = "ID of the BibTeX", required = true) @PathParam("id") String id
    ) {
        Pmml retrievedPmml = pmmlHandler.find(id);
        if (retrievedPmml == null) {
            throw new NotFoundException("PMML with ID " + id + " not found.");
        }
        return Response.ok(retrievedPmml).build();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all PMML entries",
            notes = "Finds all PMML entries in the DB of Jaqpot and returns them in a list",
            response = Pmml.class,
            responseContainer = "List",
            position = 1)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "PMML  entries found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listPmml(
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max", defaultValue = "10") @QueryParam("max") Integer max
    ) {
        return Response
                .ok(pmmlHandler.findAll(start, max))
                .status(Response.Status.OK)
                .build();
    }

}
