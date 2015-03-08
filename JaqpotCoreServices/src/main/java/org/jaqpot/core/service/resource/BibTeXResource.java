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
import java.util.UUID;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.data.BibTeXHandler;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.validator.BibTeXValidator;

/**
 *
 * @author chung
 */
@Path("bibtex")
@Api(value = "/bibtex", description = "BibTeX API")
@Produces({"application/json", "text/uri-list"})
public class BibTeXResource {

    @EJB
    BibTeXHandler handler;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all BibTeX entries",
            notes = "Finds all BibTeX entries in the DB of Jaqpot and returns them in a list",
            response = BibTeX.class,
            responseContainer = "List",
            position = 1)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entries found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getBibTeXs(
            @ApiParam(value = "BibTeX type of entry", 
                    allowableValues = "Article,Conference,Book,PhDThesis,InBook,InCollection,"
                            + "InProceedings,Manual,Mastersthesis,Proceedings,TechReport,"
                            + "Unpublished,Entry", defaultValue = "Entry") @QueryParam("bibtype") String bibtype,
            @ApiParam("Creator of the BibTeX entry") @QueryParam("creator") String creator,
            @ApiParam("Generic query (e.g., Article title, journal name, etc)") @QueryParam("query") String query
    ) {
        return Response
                .ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Returns BibTeX entry",
            notes = "Finds and returns a BibTeX by ID",
            response = BibTeX.class,
            position = 2)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entries found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 404, message = "No such bibtex entry on the server (not found)"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getBibTeX() {
        return Response
                .ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new BibTeX entry",
            notes = "Creates a new BibTeX entry which is assigned a random unique ID",
            response = BibTeX.class,
            position = 3)
    //TODO add code for user's quota exceeded
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entry was created successfully."),
        @ApiResponse(code = 400, message = "Bad request: malformed bibtex (e.g., mandatory fields are missing)"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response createBibTeX(
            @ApiParam(value = "Clients need to authenticate in order to create resources on the server") 
            @HeaderParam("subjectid") 
                    String subjectId,
            @ApiParam(value="BibTeX in JSON representation compliant with the BibTeX specifications. "
                    + "Malformed BibTeX entries with missing fields will not be accepted.", required = true) 
                    BibTeX bib
    ) {
        
        if (bib==null){
            ErrorReport report = ErrorReportFactory.badRequest("No bibtex provided; check out the API specs", 
                    "Clients MUST provide a BibTeX document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        bib.setId(UUID.randomUUID().toString());
        ErrorReport error = BibTeXValidator.validate(bib);
        if (error!=null)
        {
            return Response
                .ok(error)
                .status(Response.Status.BAD_REQUEST)
                .build();
        }
        handler.create(bib);        
        return Response
                .ok(bib)
                .status(Response.Status.OK)
                .build();
        
    }

    @PUT
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Places a new BibTeX entry at a particular URI",
            notes = "Creates a new BibTeX entry which is assigned a random unique ID",
            response = BibTeX.class,
            position = 4)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entry was created successfully."),
        @ApiResponse(code = 400, message = "BibTeX entry was not created because the request was malformed"),
        @ApiResponse(code = 401, message = "You are not authorized to create a bibtex on the server"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response createBibTeXGivenID(
            @ApiParam(value = "ID of the BibTeX.", required = true) @PathParam("id") String id,
            @ApiParam("Clients need to authenticate in order to create resources on the server") @HeaderParam("subjectid") String subjectId
    ) {
        return Response
                .ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Deletes a particular BibTeX resource",
            notes = "Deletes a BibTeX resource of a given ID",
            position = 5)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entry was deleted successfully."),
        @ApiResponse(code = 404, message = "No such BibTeX - nothing was deleted"),
        @ApiResponse(code = 401, message = "You are not authorized to delete this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response deleteBibTeX(
            @ApiParam("Clients need to authenticate in order to create resources on the server") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "ID of the BibTeX.", required = true) @PathParam("id") String id
    ) {
        return Response
                .ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

}
