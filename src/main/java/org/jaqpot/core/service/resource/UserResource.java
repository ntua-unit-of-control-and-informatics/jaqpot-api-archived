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

import io.swagger.annotations.*;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.UserQuota;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.data.QuotaService;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("user")
@Api(value = "/user", description = "Users API", position = 1)
@Produces({"application/json", "text/uri-list"})
public class UserResource {

    @EJB
    QuotaService quotaService;

    @EJB
    UserHandler userHandler;

    @Inject
    PropertyManager propertyManager;

    @Context
    SecurityContext securityContext;

    @GET
    @TokenSecured({RoleEnum.ADMNISTRATOR})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Lists all Users (admins only)",
            notes = "Lists all Users of Jaqpot Quattro. This operation can only be performed by the system administrators.",
            response = User.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Users found and are listed in the response body")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listUsers(
            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("Authorization") String api_key,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max", defaultValue = "10") @QueryParam("max") Integer max
    ) throws JaqpotNotAuthorizedException {
        // This resource can be accessed only by the system administrators
        String admins = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_ADMINISTRATORS);
        List<String> adminsList = Arrays.asList(admins.split("\\s*,\\s*"));
        String currentUserID = securityContext.getUserPrincipal().getName();
        if (!adminsList.contains(currentUserID)) {
            throw new JaqpotNotAuthorizedException("User " + currentUserID + " is not a system administrator, "
                    + "therefore is not authorized to access this resource.", "AdministratorsOnly");
        }

        List<User> users = userHandler.listMeta(start, max);
        return Response
                .ok(users)
                .build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @ApiOperation(value = "Finds User by Id",
            notes = "Finds specified user",
            response = User.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User is found")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 404, message = "This user was not found.")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getUser(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access this resource")
            @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {

//        String admins = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_ADMINISTRATORS);
//        List<String> adminsList = Arrays.asList(admins.split("\\s*,\\s*"));
        String currentUserID = securityContext.getUserPrincipal().getName();
//        if (!adminsList.contains(currentUserID) && !id.equals(currentUserID)) {
//            throw new JaqpotNotAuthorizedException("User " + currentUserID + "is not authorized access "
//                    + "this resource (/user/" + id + ")", "Unauthorized");
//        }
        User user = userHandler.find(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
        } else {
            // Hide the hashed password!
            user.setHashedPass(null);
        }
        return Response.ok(user).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/ids")
    @ApiOperation(value = "Finds User from partial given username",
            notes = "Finds all users queried")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Users found")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 404, message = "No user was not found.")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getAllUser(
            @QueryParam("name") String name,
            @QueryParam("mail") String mail,
            @ApiParam(value = "Clients need to authenticate in order to access this resource")
            @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {

        String currentUserID = securityContext.getUserPrincipal().getName();
        Map<String, Object> search = new HashMap();
        if(name != null){
            search.put("name", name.toLowerCase());
        }
        if(mail != null){
            search.put("mail", mail.toLowerCase());
        }
        
        List<User> users = userHandler.findAllWithPattern(search);

        return Response.ok(users).build();
    }
    
    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @ApiOperation(value = "Updates User by Id",
            notes = "Updates specified user",
            response = User.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User is found")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 404, message = "This user was not found.")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response updateUser(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access this resource")
            @HeaderParam("Authorization") String api_key,
            User userForUpadate) throws JaqpotNotAuthorizedException {

        String currentUserID = securityContext.getUserPrincipal().getName();

        User userById = userHandler.find(currentUserID);

        if (!userForUpadate.getId().equals(userById.getId())) {
            throw new JaqpotNotAuthorizedException("Only the actual user can update its resources");
        }
        if (userById == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
        } else {
            // Hide the hashed password!
            userById.setHashedPass(null);
        }
        userHandler.edit(userForUpadate);
        User userUpdated = userHandler.find(currentUserID);
        return Response.ok(userUpdated).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{id}/quota")
    @ApiOperation(value = "Retrieves user's quota",
            notes = "Returns user's quota given the user's ID. Authenicated users can access only their own quota. "
            + "Jaqpot administrators can access the quota of all Jaqpot users.",
            response = UserQuota.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User is found and quota are retrieved")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user's quota")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 404, message = "This user was not found.")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getUserQuota(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access this resource")
            @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {

        String currentUserID = securityContext.getUserPrincipal().getName();
        String admins = propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_ADMINISTRATORS);
        List<String> adminsList = Arrays.asList(admins.split("\\s*,\\s*"));
        if (!adminsList.contains(currentUserID) && !id.equals(currentUserID)) {
            throw new JaqpotNotAuthorizedException("User " + currentUserID + "is not authorized access "
                    + "this resource (/user/" + id + ")", "Unauthorized");
        }

        UserQuota userQuota = quotaService.getUserQuota(currentUserID);
        
        return Response.ok(userQuota).build();
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}/picture")
    @ApiOperation(value = "Finds Users profile pic by Id",
            notes = "Finds specified users profile pic",
            response = User.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User is found")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 404, message = "This user was not found.")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getUserPic(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access this resource")
            @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {

        String currentUserID = securityContext.getUserPrincipal().getName();
        User user = userHandler.getProfPic(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
        } else {
            // Hide the hashed password!
            user.setHashedPass(null);
        }
        return Response.ok(user).build();
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}/occupation")
    @ApiOperation(value = "Finds User occupation by Id",
            notes = "Finds specified users occupation",
            response = User.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User is found")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 404, message = "This user was not found.")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getUserOccupation(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access this resource")
            @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {

        String currentUserID = securityContext.getUserPrincipal().getName();
        User user = userHandler.getOccupation(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
        } else {
            // Hide the hashed password!
            user.setHashedPass(null);
        }
        return Response.ok(user).build();
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}/occupationat")
    @ApiOperation(value = "Finds User occupation place by Id",
            notes = "Finds specified users occupation organization",
            response = User.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User is found")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 404, message = "This user was not found.")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getUserOccupationAt(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access this resource")
            @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {

        String currentUserID = securityContext.getUserPrincipal().getName();
        User user = userHandler.getOccupationAt(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
        } else {
            // Hide the hashed password!
            user.setHashedPass(null);
        }
        return Response.ok(user).build();
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}/name")
    @ApiOperation(value = "Finds User occupation place by Id",
            notes = "Finds specified users occupation organization",
            response = User.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User is found")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 404, message = "This user was not found.")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getUserName(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access this resource")
            @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {

        String currentUserID = securityContext.getUserPrincipal().getName();
        User user = userHandler.getName(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find User with id:" + id).build();
        } else {
            // Hide the hashed password!
            user.setHashedPass(null);
        }
        return Response.ok(user).build();
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}/organizations")
    @ApiOperation(value = "Finds User's Organizations by user Id",
            notes = "Finds specified users organization",
            response = User.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User is found")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this user")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 404, message = "This user was not found.")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getUserOrganizations(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access this resource")
            @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {

        String currentUserID = securityContext.getUserPrincipal().getName();
        User user = userHandler.getOrganizations(id);
        return Response.ok(user).build();
    }

    
}
