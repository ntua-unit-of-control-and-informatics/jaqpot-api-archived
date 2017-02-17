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
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.dto.aa.AuthToken;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("aa")
@Api(value = "/aa", description = "AA API")
@Produces({"application/json"})
public class AAResource {

    @EJB
    AAService aaService;

    @POST
    @Path("/login")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(
            value = "Creates Security Token",
            notes = "Uses OpenAM server to get a security token."
    )
    @ApiResponses(value = {
        @ApiResponse(code = 401, response = JaqpotNotAuthorizedException.class, message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 200, response=AuthToken.class, message = "Logged in - authentication token can be found in the response body (in JSON)")
    })
    public Response login(
            @ApiParam("Username") @FormParam("username") String username,
            @ApiParam("Password") @FormParam("password") String password) throws JaqpotNotAuthorizedException {

        AuthToken aToken;
        aToken = aaService.login(username, password);
        return Response.ok(aToken)
                .build();
    }

    @POST
    @Path("/logout")
    @Consumes("text/plain")
    @Authorize
    @Produces("application/json")
    @ApiOperation(
            value = "Logs out a user",
            notes = "Invalidates a security token and logs out the corresponding user")
    @ApiResponses(value = {
        @ApiResponse(code = 403, response = ErrorReport.class , message = "Your authorization token is not valid."),
        @ApiResponse(code = 401, response = JaqpotNotAuthorizedException.class , message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 200, response = String.class , message = "Logged out")
    })
    public Response logout(
            @HeaderParam("subjectid") String subjectId
    ) throws JaqpotNotAuthorizedException {
        boolean loggedOut = aaService.logout(subjectId);
        return Response
                .ok(loggedOut, MediaType.APPLICATION_JSON)
                .status(Response.Status.OK)
                .build();
    }

    @POST
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @Authorize
    @ApiOperation(
            value = "Validate authorization token",
            notes = "Checks whether an authorization token is valid")
    @ApiResponses(value = {
        @ApiResponse(code = 403, response = ErrorReport.class , message = "Your authorization token is not valid."),
        @ApiResponse(code = 401, response=JaqpotNotAuthorizedException.class , message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 200, response=String.class , message = "Your authorization token is valid")
    })
    public Response validate(
            @HeaderParam("subjectid") String subjectId
    ) throws JaqpotNotAuthorizedException {
        boolean valid = aaService.validate(subjectId);
        return Response.ok(valid ? "true" : "false", MediaType.APPLICATION_JSON)
                .status(valid ? Response.Status.OK : Response.Status.UNAUTHORIZED)
                .build();
    }

    @POST
    @Path("/authorize")
    @Produces(MediaType.APPLICATION_JSON)
    @Authorize
    @ApiOperation(
            value = "Requests authorization from SSO",
            notes = "Checks whether the client identified by the provided AA token can apply a method to a URI"
            )
    @ApiResponses(value = {
        @ApiResponse(code = 403, response = ErrorReport.class , message = "Your authorization token is not valid."),
        @ApiResponse(code = 401, response=JaqpotNotAuthorizedException.class , message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 200, message = "You have authorization for the given URI and HTTP method")
    })
    public Response authorize(
            @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "HTTP method", allowableValues = "GET,POST,PUT,DELETE", defaultValue = "GET") @FormParam("method") String method,
            @ApiParam(value = "URI") @FormParam("uri") String uri
    ) throws JaqpotNotAuthorizedException {
        boolean valid = aaService.authorize(subjectId, method, uri);
        return Response.ok(valid ? "true" : "false", MediaType.APPLICATION_JSON)
                .status(valid ? Response.Status.OK : Response.Status.FORBIDDEN)
                .build();
    }

}
