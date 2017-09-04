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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.jaxrs.PATCH;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.BibTeXHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.model.validator.BibTeXValidator;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.exceptions.JaqpotForbiddenException;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("bibtex")
@Api(value = "/bibtex", description = "BibTeX API")
@Produces({"application/json", "text/uri-list"})
public class BibTeXResource {
    
    @EJB
    AAService aaService;
    
    @Context
    SecurityContext securityContext;
    
    @Context
    UriInfo uriInfo;
    
    @Inject
    @Jackson
    JSONSerializer serializer;
    
    private static final Logger LOG = Logger.getLogger(BibTeXResource.class.getName());
    
    private static final String DEFAULT_BIBTEX
            = "{\n"
            + "  \"bibType\":\"Article\",\n"
            + "  \"title\":\"title goes here\",\n"
            + "  \"author\":\"A.N.Onymous\",\n"
            + "  \"journal\":\"Int. J. Biochem.\",\n"
            + "  \"year\":2010,\n"
            + "  \"meta\":{\"comments\":[\"default bibtex\"]}\n"
            + "}",
            DEFAULT_BIBTEX_PATCH = "[\n"
            + "  {\n"
            + "    \"op\": \"add\",\n"
            + "    \"path\": \"/key\",\n"
            + "    \"value\": \"foo\"\n "
            + "  }\n"
            + "]";
    
    @EJB
    BibTeXHandler bibtexHandler;
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all BibTeX entries",
            notes = "Finds all BibTeX entries in the DB of Jaqpot and returns them in a list", position = 1)
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = BibTeX.class, responseContainer = "List",
                  message = "BibTeX entries found and are listed in the response body"),
        @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response listBibTeXs(
            @ApiParam(value = "BibTeX type of entry",
                    allowableValues = "Article,Conference,Book,PhDThesis,InBook,InCollection,"
                    + "InProceedings,Manual,Mastersthesis,Proceedings,TechReport,"
                    + "Unpublished,Entry", defaultValue = "Entry") @QueryParam("bibtype") String bibtype,
            @ApiParam("Creator of the BibTeX entry") @QueryParam("creator") String creator,
            @ApiParam("Generic query (e.g., Article title, journal name, etc)") @QueryParam("query") String query,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max", defaultValue = "10") @QueryParam("max") Integer max
    ) {
        return Response
                .ok(bibtexHandler.listMeta(start != null ? start : 0, max != null ? max : Integer.MAX_VALUE))
                .status(Response.Status.OK)
                .build();
    }
    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Returns BibTeX entry",
            notes = "Finds and returns a BibTeX by ID",
            position = 2)
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = BibTeX.class, message = "BibTeX entries found and are listed in the response body"),
        @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this user"),
        @ApiResponse(code = 404, response = ErrorReport.class, message = "No such bibtex entry on the server (not found)"),
        @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response getBibTeX(
            @ApiParam(value = "ID of the BibTeX", required = true) @PathParam("id") String id
    ) {
        BibTeX b = bibtexHandler.find(id);
        if (b == null) {
            throw new NotFoundException("BibTeX " + id + " not found.");
        }
        return Response.ok(b).build();
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new BibTeX entry",
            notes = "Creates a new BibTeX entry which is assigned a random unique ID. "
            + "Clients are not allowed to specify a custom ID when using this method. "
            + "Clients should use PUT instead in such a case.",
            position = 3)
    //TODO add code for user's quota exceeded
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = BibTeX.class, message = "BibTeX entry was created successfully."),
        @ApiResponse(code = 400, response = ErrorReport.class, message = "Bad request: malformed bibtex (e.g., mandatory fields are missing)"),
        @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    @Authorize
    public Response createBibTeX(
            @ApiParam(value = "Clients need to authenticate in order to create resources on the server")
            @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "BibTeX in JSON representation compliant with the BibTeX specifications. "
                    + "Malformed BibTeX entries with missing fields will not be accepted.", required = true,
                    defaultValue = DEFAULT_BIBTEX) BibTeX bib
    ) throws JaqpotNotAuthorizedException {
        if (bib == null) {
            ErrorReport report = ErrorReportFactory.badRequest("No bibtex provided; check out the API specs",
                    "Clients MUST provide a BibTeX document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        if (bib.getId() == null) {
            ROG rog = new ROG(true);
            bib.setId(rog.nextString(10));
        }
        ErrorReport error = BibTeXValidator.validate(bib);
        if (error != null) {
            return Response
                    .ok(error)
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
        if (bib.getMeta() == null) {
            bib.setMeta(new MetaInfo());
        }
        bib.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
        bibtexHandler.create(bib);
        return Response
                .ok(bib)
                .status(Response.Status.CREATED)
                .header("Location", uriInfo.getBaseUri().toString() + "bibtex/" + bib.getId())
                .build();
        
    }
    
    @PUT
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Places a new BibTeX entry at a particular URI",
            notes = "Creates a new BibTeX entry at the specified URI. If a BibTeX already exists at this URI,"
            + "it will be replaced. If, instead, no BibTeX is stored under the specified URI, a new "
            + "BibTeX entry will be created. Notice that authentication, authorization and accounting (quota) "
            + "restrictions may apply.",
            position = 4)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = BibTeX.class, message = "BibTeX entry was created successfully."),
        @ApiResponse(code = 400, response = ErrorReport.class, message = "BibTeX entry was not created because the request was malformed"),
        @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to create a bibtex on the server"),
        @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response createBibTeXGivenID(
            @ApiParam(value = "ID of the BibTeX.", required = true) @PathParam("id") String id,
            @ApiParam(value = "BibTeX in JSON", defaultValue = DEFAULT_BIBTEX, required = true) BibTeX bib,
            @ApiParam("Clients need to authenticate in order to create resources on the server") @HeaderParam("subjectid") String subjectId
    ) {
        if (bib == null) {
            ErrorReport report = ErrorReportFactory.badRequest("No bibtex provided; check out the API specs",
                    "Clients MUST provide a BibTeX document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        bib.setId(id);
        ErrorReport error = BibTeXValidator.validate(bib);
        if (error != null) {
            return Response
                    .ok(error)
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
        
        BibTeX foundBibTeX = bibtexHandler.find(id);
        if (foundBibTeX != null) {
            bibtexHandler.edit(bib);
        } else {
            bibtexHandler.create(bib);
        }
        
        return Response
                .ok(bib)
                .status(Response.Status.CREATED)
                .header("Location", uriInfo.getBaseUri().toString() + "bibtex/" + bib.getId())
                .build();
    }
    
    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Deletes a particular BibTeX resource",
            notes = "Deletes a BibTeX resource of a given ID. The method is idempondent, that is, it can be used more than once without "
            + "triggering an exception/error. If the BibTeX does not exist, the method will return without errors. "
            + "Authentication and authorization requirements apply, so clients that are not authenticated with a "
            + "valid token or do not have sufficient priviledges will not be able to delete a BibTeX using this method.",
            position = 5)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entry was deleted successfully."),
        @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to delete this resource"),
        @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response deleteBibTeX(
            @ApiParam("Clients need to authenticate in order to create resources on the server") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "ID of the BibTeX.", required = true) @PathParam("id") String id
    ) throws JaqpotForbiddenException {
        BibTeX bibTeX = new BibTeX(id);

        MetaInfo metaInfo = bibTeX.getMeta();
        if (metaInfo.getLocked())
            throw new JaqpotForbiddenException("You cannot delete a Bibtex that is locked.");

        bibtexHandler.remove(new BibTeX(id));
        return Response.ok().build();
    }
    
    @PATCH
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes("application/json-patch+json")
    @ApiOperation(value = "Modifies a particular BibTeX resource",
            notes = "Modifies (applies a patch on) a BibTeX resource of a given ID. "
            + "This implementation of PATCH follows the RFC 6902 proposed standard. "
            + "See https://tools.ietf.org/rfc/rfc6902.txt for details.",
            position = 5)
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = BibTeX.class, message = "BibTeX entry was modified successfully."),
        @ApiResponse(code = 404, response = ErrorReport.class, message = "No such BibTeX - the patch will not be applied"),
        @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to modify this resource"),
        @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response modifyBibTeX(
            @ApiParam("Clients need to authenticate in order to create resources on the server") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "ID of an existing BibTeX.", required = true) @PathParam("id") String id,
            @ApiParam(value = "The patch in JSON according to the RFC 6902 specs", required = true, defaultValue = DEFAULT_BIBTEX_PATCH) String patch
    ) throws JsonPatchException, JsonProcessingException {
        
        BibTeX originalBib = bibtexHandler.find(id); // find doc in DB
        if (originalBib == null) {
            throw new NotFoundException("BibTeX " + id + " not found.");
        }
        
        BibTeX modifiedAsBib = serializer.patch(originalBib, patch, BibTeX.class);
        if (modifiedAsBib == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ErrorReportFactory.badRequest("Patch cannot be applied because the request is malformed", "Bad patch"))
                    .build();
        }
        ErrorReport validationError = BibTeXValidator.validate(modifiedAsBib);
        if (validationError != null) {
            return Response
                    .ok(validationError)
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
        bibtexHandler.edit(modifiedAsBib); // update the entry in the DB

        return Response
                .ok(modifiedAsBib)
                .build();
    }
    
}
