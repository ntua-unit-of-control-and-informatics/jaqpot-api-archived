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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.dto.aa.AuthToken;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

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
    AAService aaService;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Users",
            notes = "Finds all Users of Jaqpot Quattro",
            response = User.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Users found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getUsers(
            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("subjectid") String subjectId
    ) {
        return Response
                .ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Path("/{id}")
    @ApiOperation(value = "Finds User by Id",
            notes = "Finds specified user",
            response = User.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User is found"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 404, message = "This user was not found."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getUser(
            @PathParam("id") String id,
            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("subjectid") String subjectId) {

        return Response
                .ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates Security Token",
            notes = "Uses OpenAM server to get a security token.",
            produces = "application/json")
    @ApiResponses(value = {
        @ApiResponse(code = 401, message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 200, message = "Logged in - authentication token can be found in the response body (in JSON)")
    })
    public Response login(
            @ApiParam("Username") @FormParam("username") String username,
            @ApiParam("Password") @FormParam("password") String password) {

        AuthToken aToken;
        try {
            aToken = aaService.login(username, password);
            return Response.ok(aToken).status(Response.Status.OK).build();
        } catch (JaqpotNotAuthorizedException ex) {
            throw new NotAuthorizedException(Response.ok(ex.getError()).status(Response.Status.UNAUTHORIZED).build());
        }

    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Logsout user",
            notes = "Invalidates a security token and logs out the user",
            produces = "application/json")
    @ApiResponses(value = {
        @ApiResponse(code = 401, message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 200, message = "Logged out")
    })
    public Response logout(
            @HeaderParam("subjectid") String subjectId
    ) {
        return Response
                .ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }
}
