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
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import javax.ejb.EJB;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.dto.aa.AuthToken;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

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
            @ApiParam("Password") @FormParam("password") String password) throws JaqpotNotAuthorizedException {

        AuthToken aToken;
        aToken = aaService.login(username, password);
        return Response
                .ok(aToken)
                .status(Response.Status.OK)
                .build();
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    @Authorize
    @ApiOperation(
            value = "Logs out a user",
            notes = "Invalidates a security token and logs out the corresponding user",
            produces = "text/plain")
    @ApiResponses(value = {
        @ApiResponse(code = 401, message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 200, message = "Logged out")
    })
    public Response logout(
            @HeaderParam("subjectid") String subjectId
    ) {
        boolean loggedOut = aaService.logout(subjectId);
        return Response
                .ok(loggedOut ? "true" : "false", MediaType.APPLICATION_JSON)
                .status(loggedOut ? Response.Status.OK : Response.Status.UNAUTHORIZED)
                .build();
    }

    @POST
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @Authorize
    @ApiOperation(
            value = "Validate authorization token",
            notes = "Checks whether an authorization token is valid",
            produces = "application/json")
    @ApiResponses(value = {
        @ApiResponse(code = 401, message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 200, message = "Logged out")
    })
    public Response validate(
            @HeaderParam("subjectid") String subjectId
    ) {
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
            notes = "Checks whether the client identified by the provided AA token can apply a method to a URI",
            produces = "application/json")
    @ApiResponses(value = {
        @ApiResponse(code = 401, message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 200, message = "Logged out")
    })
    public Response authorize(
            @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "HTTP method", allowableValues = "GET,POST,PUT,DELETE", defaultValue = "GET") @FormParam("method") String method,
            @ApiParam(value = "URI") @FormParam("uri") String uri
    ) {

        return Response.ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

}
