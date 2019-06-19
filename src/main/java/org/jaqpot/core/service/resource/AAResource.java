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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.v3.oas.models.security.SecurityRequirement;
//import io.swagger.annotations.*;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.dto.aa.AuthToken;
import org.jaqpot.core.service.authentication.AAService;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.model.User;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("/aa")
//@Api(value = "/aa", description = "AA API")
@Produces({"application/json"})
@Tag(name = "aa")

public class AAResource {

    @EJB
    AAService aaService;

    @POST
    @Path("/validate/accesstoken")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Validate authorization token",
            description = "Checks whether an authorization token is valid",
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:TokenValidation"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:TokenStatus")
                })
            },
            responses = {
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "Your authorization token is valid"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response validateAccessToken(
            //@ApiParam(value = "Authorization token") @QueryParam("accessToken") String accessToken
            @Parameter(description = "Authorization token", schema = @Schema(implementation = String.class), name = "accessToken") String accessToken
    ) throws JaqpotNotAuthorizedException {
        boolean valid = aaService.validateAccessToken(accessToken);
        return Response.ok(valid ? "true" : "false", MediaType.APPLICATION_JSON)
                .status(valid ? Response.Status.OK : Response.Status.UNAUTHORIZED)
                .build();
    }

    @GET
    @Path("/claims")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Requests authorization from SSO",
            description = "Checks whether the client identified by the provided AA token can apply a method to a URI",
            extensions = {
                @Extension(properties = {
                    @ExtensionProperty(name = "orn-@type", value = "x-orn:Authorization"),}
                ),
                @Extension(name = "orn:expects", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
                }),
                @Extension(name = "orn:returns", properties = {
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:TokenClaims")
                })
            },
            responses = {
                @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Your authorization token is valid but you are forbidden from applying the specified method."),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "You have authorization for the given URI and HTTP method"),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response getClaims(
            //@ApiParam(value = "Authorization token") @QueryParam("accessToken") String accessToken
            @Parameter(description = "Authorization token", schema = @Schema(type = "String", implementation = String.class), name = "accessToken") String accessToken
    ) throws JaqpotNotAuthorizedException {
        JWTClaimsSet claims = aaService.getClaimsFromAccessToken(accessToken);
        return Response.ok(claims)
                .build();
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(description = "",
            summary = ""
    )
    public Response swaggerLogin(
            @Parameter(description = "Username", name = "username", schema = @Schema(implementation = String.class, type = "string")) @FormParam("username") String username,
            @Parameter(description = "Password", name = "password", schema = @Schema(implementation = String.class, type = "string")) @FormParam("password") String password) throws JaqpotNotAuthorizedException {

        AccessToken aToken;

        aToken = aaService.getAccessToken(username, password);
        AuthToken auToken = new AuthToken();
        auToken.setAuthToken(aToken.getValue());
        User user = aaService.getUserFromSSO(aToken.getValue());
        auToken.setUserName(user.getName());
        return Response.ok(auToken)
                .status(Response.Status.OK)
                .build();
    }
}
