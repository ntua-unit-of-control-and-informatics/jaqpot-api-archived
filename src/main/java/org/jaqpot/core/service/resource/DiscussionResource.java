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
package org.jaqpot.core.service.resource;

//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import org.jaqpot.core.data.DiscussionHandler;
import org.jaqpot.core.model.Discussion;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

/**
 *
 * @author pantelispanka
 */
@Path("discussion")
//@Api(value = "/discussion", description = "AA API")
@Produces({"application/json"})
public class DiscussionResource {
 
    @Context
    UriInfo uriInfo;
    
    @EJB
    DiscussionHandler discussionHandler;
    
    @EJB
    PropertyManager pManager;
    
    @Context
    SecurityContext securityContext;
    
    
    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)           
    @Operation(summary = "Creates a Discussion on a Jaqpot entity", 
               responses = 
                   {
                    @ApiResponse(responseCode = "200", description = "Discussion was created successfully."),
                    @ApiResponse(responseCode = "400", description = "Bad request: malformed feature"),
                    @ApiResponse(responseCode = "401", description = "You are not authorized to access this resource"),
                    @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
                   })
    //@ApiOperation(value = "Creates a Discussion on a Jaqpot entity",
    //        notes = "Creates a Discussion",
    //        response = Discussion.class)
    //@ApiResponses(value = {
    //    @ApiResponse(code = 200, message = "Discussion was created successfully."),
    //    @ApiResponse(code = 400, message = "Bad request: malformed feature"),
    //    @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
    //    @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
    //    @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    //})
    public Response createNewDiscussion(@HeaderParam("Authorization") String api_key
            ,Discussion discussion)
    throws JaqpotNotAuthorizedException, JaqpotDocumentSizeExceededException{
        
        ROG rog = new ROG(true);
        discussion.setId(rog.nextString(10));
        this.discussionHandler.create(discussion);
//        URI uri = pManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE);
        return Response.ok(discussion).build();
    }
    
    @GET
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a Discussion from it's id",
                  responses = 
                          {     
                           @ApiResponse(responseCode = "200", description="Disussion found"),
                           @ApiResponse(responseCode = "400", description = "Bad request: malformed feature"),
                           @ApiResponse(responseCode = "401", description = "You are not authorized to access this resource"),
                           @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                           @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
                           })
    //@ApiOperation(value = "Get a Discussion from it's id",
    //        notes = "Get's a Discussion",
    //        response = Discussion.class)
    //@ApiResponses(value = {
    //    @ApiResponse(code = 200, message="Disussion found"),
    //    @ApiResponse(code = 400, message = "Bad request: malformed feature"),
    //    @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
    //    @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
    //    @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    //})
    public Response getDiscussionById(@HeaderParam("Authorization") String api_key,
            @PathParam("id") String id)
    throws JaqpotNotAuthorizedException, JaqpotDocumentSizeExceededException{
        
        ROG rog = new ROG(true);
        Discussion disc = discussionHandler.find(id);
//        URI uri = pManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE);
        return Response.ok(disc).build();
    }
    
    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a Discussion from it's entity id",
               responses = 
                        {
                         @ApiResponse(responseCode = "200", description = "Disussion's on entity found"),
                         @ApiResponse(responseCode = "400", description = "Bad request: malformed discussion"),
                         @ApiResponse(responseCode = "401", description = "You are not authorized to access this resource"),
                         @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                         @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
                        })
    //@ApiOperation(value = "Get a Discussion from it's entity id",
    //        notes = "Get's the Discussion on an entity",
    //        response = Discussion.class)
    //@ApiResponses(value = {
    //    @ApiResponse(code = 200, message = "Disussion's on entity found"),
    //    @ApiResponse(code = 400, message = "Bad request: malformed discussion"),
    //    @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
    //    @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
    //    @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    //})
    
    public Response getDiscussionByEntityId(@HeaderParam("Authorization") String api_key,
            @QueryParam("entityid") String entityId, @QueryParam("start") Integer start,
            @QueryParam("max") Integer max)
    throws JaqpotNotAuthorizedException, JaqpotDocumentSizeExceededException{
        
        if(start == null){
            start = 0;
        }
        if(max == null){
            max = 20;
        }
        
        List<Discussion> disc = discussionHandler.findByEntityId(entityId, start, max);
        Long total = discussionHandler.countByEntityId(entityId);
        
        return Response.ok(disc)
                .header("total", total)
                .build();
    }
    
    
    @PUT
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(summary = "Updates a Discussion at a particular URI",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Discussion entry was created / updated successfully."),
                   @ApiResponse(responseCode = "400", description = "Discussion entry was not created because the request was malformed"),
                   @ApiResponse(responseCode = "401", description = "You are not authorized to create a feature on the server"),
                   @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                   @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
               })
    @Consumes(MediaType.APPLICATION_JSON)
    
    //@ApiOperation(value = "Updates a Discussion at a particular URI",
    //        notes = "Updates a Discussion entry at the specified URI. If a Discussion already exists at this URI,"
    //        + "it will be replaced. If, instead, no Discussion is stored under the specified URI, a new "
    //        + "Discussion entry will be created. Notice that authentication, authorization and accounting (quota) "
    //        + "restrictions may apply.",
    //        response = Discussion.class,
    //        position = 4)
    //@Consumes(MediaType.APPLICATION_JSON)
    //@ApiResponses(value = {
    //    @ApiResponse(code = 200, message = "Discussion entry was created / updated successfully."),
    //    @ApiResponse(code = 400, message = "Discussion entry was not created because the request was malformed"),
    //    @ApiResponse(code = 401, message = "You are not authorized to create a feature on the server"),
    //    @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
    //    @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    //})
    public Response putDiscussionWithId(
            //@ApiParam(value = "ID of the Discussion.", required = true) @PathParam("id") String id,
            //@ApiParam(value = "Discussion in JSON", required = true) Discussion discussion,
            //@ApiParam(value = "Clients need to authenticate in order to create resources on the server") @HeaderParam("Authorization") String api_key
            @Parameter(description = "ID of the Discussion.", required = true) @PathParam("id") String id,
            @Parameter(description = "Discussion in JSON", required = true) Discussion discussion,
            @Parameter(description = "Clients need to authenticate in order to create resources on the server") @HeaderParam("Authorization") String api_key
           
    )throws JaqpotDocumentSizeExceededException  {
        if (discussion == null) {
            ErrorReport report = ErrorReportFactory.badRequest("No feature provided; check out the API specs",
                    "Clients MUST provide a Feature document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        discussion.setId(id);

        Discussion discussionFound = discussionHandler.find(id);
        if (discussionFound != null) {
            discussionHandler.edit(discussion);
        } else {
            discussionHandler.create(discussion);
        }

        return Response
                .ok(discussion)
                .status(Response.Status.ACCEPTED)
                .header("Location", uriInfo.getBaseUri().toString() + "discussion/" + discussion.getId())
                .build();
    }
    
    @PUT
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(summary = "Updates a Discussion",
               responses = {
                            @ApiResponse(responseCode = "200", description = "Discussion entry was created / updated successfully."),
                            @ApiResponse(responseCode = "400", description = "Discussion entry was not created because the request was malformed"),
                            @ApiResponse(responseCode = "401", description = "You are not authorized to create a feature on the server"),
                            @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                            @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
                })   
    @Consumes(MediaType.APPLICATION_JSON)
    
    //@ApiOperation(value = "Updates a Discussion",
    //        notes = "Updates a Discussion entry at the specified URI. If a Discussion already exists at this URI,"
    //        + "it will be replaced. If, instead, no Discussion is stored under the specified URI, a new "
    //        + "Discussion entry will be created. Notice that authentication, authorization and accounting (quota) "
    //        + "restrictions may apply.",
    //        response = Discussion.class,
    //        position = 4)
    //@Consumes(MediaType.APPLICATION_JSON)
    //@ApiResponses(value = {
    //    @ApiResponse(code = 200, message = "Discussion entry was created / updated successfully."),
    //    @ApiResponse(code = 400, message = "Discussion entry was not created because the request was malformed"),
    //    @ApiResponse(code = 401, message = "You are not authorized to create a feature on the server"),
    //    @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
    //    @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    //})
    public Response putDiscussion(
            //@ApiParam(value = "Discussion in JSON", required = true) Discussion discussion,
            //@ApiParam(value = "Clients need to authenticate in order to create resources on the server") @HeaderParam("Authorization") String api_key
            @Parameter(description = "Discussion in JSON", required = true) Discussion discussion,
            @Parameter(description = "Clients need to authenticate in order to create resources on the server") @HeaderParam("Authorization") String api_key
    )throws JaqpotDocumentSizeExceededException  {
        if (discussion == null) {
            ErrorReport report = ErrorReportFactory.badRequest("No feature provided; check out the API specs",
                    "Clients MUST provide a Feature document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        try{
            discussionHandler.edit(discussion);
        }catch(Exception e){
            throw new BadRequestException("Could not create Notification, cause: " + e.getMessage());
        }
        Discussion disc = discussionHandler.find(discussion.getId());
        return Response
                .ok(disc)
                .status(Response.Status.ACCEPTED)
                .header("Location", uriInfo.getBaseUri().toString() + "discussion/" + discussion.getId())
                .build();
    }
    
    @DELETE
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a Discussion from it's id",
               responses = {
                            @ApiResponse(responseCode = "200", description = "Disussion found"),
                            @ApiResponse(responseCode = "400", description = "Bad request: malformed feature"),
                            @ApiResponse(responseCode = "401", description = "You are not authorized to access this resource"),
                            @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                            @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
               })
    //@ApiOperation(value = "Get a Discussion from it's id",
    //        notes = "Get's a Discussion",
    //        response = Discussion.class)
    //@ApiResponses(value = {
    //    @ApiResponse(code = 200, message="Disussion found"),
    //    @ApiResponse(code = 400, message = "Bad request: malformed feature"),
    //    @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
    //    @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
    //    @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    //})
    public Response deleteDiscussionById(@HeaderParam("Authorization") String api_key,
            @PathParam("id") String id)
    throws JaqpotNotAuthorizedException, JaqpotDocumentSizeExceededException{
        
        String creator = securityContext.getUserPrincipal().getName();
        Discussion discussion = discussionHandler.find(id);
        if(!discussion.getMeta().getCreators().contains(creator)){
            throw new JaqpotNotAuthorizedException("You are not authorized to delete this resource");
        }else{
            discussionHandler.remove(discussion);
        }
        
        return Response.accepted().build();
    }
    
}
