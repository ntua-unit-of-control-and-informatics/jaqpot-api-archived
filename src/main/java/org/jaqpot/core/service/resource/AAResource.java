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

//    @POST
//    @Path("/login")
//    @Consumes({"application/x-www-form-urlencoded", MediaType.APPLICATION_FORM_URLENCODED})
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @ApiOperation(
//            value = "Creates Security Token",
//            response = AuthToken.class,
//            notes = "Uses OpenAM server to get a security token.",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Authentication"),}
//                )
//                ,
//                @Extension(name = "orn:expects", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Credentials")
//        })
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
//        })
//            }
//    )
//    @ApiResponses(value = {
//        @ApiResponse(code = 401, response = ErrorReport.class, message = "Wrong, missing or insufficient credentials. Error report is produced.")
//        ,        @ApiResponse(code = 200, response = AccessToken.class, message = "Logged in - authentication token can be found in the response body (in JSON)")
//        ,
//        @ApiResponse(code = 200, response = AuthToken.class, message = "Successfully log in")
//        ,
//        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
//    })
//    public Response login(
//            @ApiParam(value = "Username", required = true) @FormParam("username") String username,
//            @ApiParam(value = "Password", required = true) @FormParam("password") String password) throws JaqpotNotAuthorizedException {
//
//        AccessToken aToken;
//        aToken = aaService.getAccessToken(username, password);
//        return Response.ok(aToken)
//                .status(Response.Status.OK)
//                .build();
//    }
//    @POST
//    @Path("/logout")
//    @Consumes("text/plain")
//    @Authorize
//    @Produces("application/json")
//    @ApiOperation(
//            value = "Logs out a user",
//            notes = "Invalidates a security token and logs out the corresponding user",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Logout"),}
//                )
//                ,
//                            @Extension(name = "orn:expects", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
//        })
//                ,
//                            @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Status")
//        })
//            }
//    )
//    @ApiResponses(value = {
//        @ApiResponse(code = 401, response = ErrorReport.class, message = "Wrong, missing or insufficient credentials. Error report is produced.")
//        ,
//        @ApiResponse(code = 200, response = String.class, message = "Logged out")
//        ,
//        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
//    })
//    public Response logout(
//            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId
//    ) throws JaqpotNotAuthorizedException {
//        boolean loggedOut = aaService.logout(subjectId);
//        return Response
//                .ok(loggedOut, MediaType.APPLICATION_JSON)
//                .status(Response.Status.OK)
//                .build();
//    }
//    @POST
//    @Path("/validate")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Authorize
//    @ApiOperation(
//            value = "Validate authorization token",
//            notes = "Checks whether an authorization token is valid",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:TokenValidation"),}
//                )
//                ,
//                @Extension(name = "orn:expects", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
//        })
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:TokenStatus")
//        })
//            })
//    @ApiResponses(value = {
//        @ApiResponse(code = 401, response = ErrorReport.class, message = "Wrong, missing or insufficient credentials. Error report is produced.")
//        ,
//        @ApiResponse(code = 200, response = String.class, message = "Your authorization token is valid")
//        ,
//        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
//    })
//    public Response validate(
//            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId
//    ) throws JaqpotNotAuthorizedException {
//        boolean valid = aaService.validate(subjectId);
//        return Response.ok(valid ? "true" : "false", MediaType.APPLICATION_JSON)
//                .status(valid ? Response.Status.OK : Response.Status.UNAUTHORIZED)
//                .build();
//    }
    @POST
    @Path("/validate/accesstoken")
    @Produces(MediaType.APPLICATION_JSON)
    /*@ApiOperation(
     value = "Validate authorization token",
     notes = "Checks whether an authorization token is valid",
     extensions = {
     @Extension(properties = {
     @ExtensionProperty(name = "orn-@type", value = "x-orn:TokenValidation"),}
     )
     ,
     @Extension(name = "orn:expects", properties = {
     @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
     })
     ,
     @Extension(name = "orn:returns", properties = {
     @ExtensionProperty(name = "x-orn-@id", value = "x-orn:TokenStatus")
     })
     })
     @ApiResponses(value = {
     @ApiResponse(code = 401, response = ErrorReport.class, message = "Wrong, missing or insufficient credentials. Error report is produced.")
     ,
     @ApiResponse(code = 200, response = String.class, message = "Your authorization token is valid")
     ,
     @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
     })*/
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

//    @POST
//    @Path("/authorize")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Authorize
//    @ApiOperation(
//            value = "Requests authorization from SSO",
//            notes = "Checks whether the client identified by the provided AA token can apply a method to a URI",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Authorization"),}
//                )
//                ,
//                @Extension(name = "orn:expects", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
//        })
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:TokenAuthorization")
//        })
//            }
//    )
//    @ApiResponses(value = {
//        @ApiResponse(code = 403, response = ErrorReport.class, message = "Your authorization token is valid but you are forbidden from applying the specified method.")
//        ,
//        @ApiResponse(code = 401, response = ErrorReport.class, message = "Wrong, missing or insufficient credentials. Error report is produced.")
//        ,
//        @ApiResponse(code = 200, response = String.class, message = "You have authorization for the given URI and HTTP method")
//        ,
//        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
//    })
//    public Response authorize(
//            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
//            @ApiParam(value = "HTTP method", required = true, allowableValues = "GET,POST,PUT,DELETE", defaultValue = "GET") @FormParam("method") String method,
//            @ApiParam(value = "URI", required = true) @FormParam("uri") String uri
//    ) throws JaqpotNotAuthorizedException {
//        boolean valid = aaService.authorize(subjectId, method, uri);
//        return Response.ok(valid ? "true" : "false", MediaType.APPLICATION_JSON)
//                .status(valid ? Response.Status.OK : Response.Status.FORBIDDEN)
//                .build();
//    }
    @GET
    @Path("/claims")
    @Produces(MediaType.APPLICATION_JSON)
    /*@ApiOperation(
     value = "Requests authorization from SSO",
     notes = "Checks whether the client identified by the provided AA token can apply a method to a URI",
     extensions = {
     @Extension(properties = {
     @ExtensionProperty(name = "orn-@type", value = "x-orn:Authorization"),}
     )
     ,
     @Extension(name = "orn:expects", properties = {
     @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AccessToken")
     })
     ,
     @Extension(name = "orn:returns", properties = {
     @ExtensionProperty(name = "x-orn-@id", value = "x-orn:TokenClaims")
     })
     }
     )
     @ApiResponses(value = {
     @ApiResponse(code = 403, response = ErrorReport.class, message = "Your authorization token is valid but you are forbidden from applying the specified method.")
     ,
     @ApiResponse(code = 401, response = ErrorReport.class, message = "Wrong, missing or insufficient credentials. Error report is produced.")
     ,
     @ApiResponse(code = 200, response = String.class, message = "You have authorization for the given URI and HTTP method")
     ,
     @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
     })*/
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
            //@ApiParam(value = "Username", required = true) @FormParam("username") String username,
            //@ApiParam(value = "Password", required = true) @FormParam("password") String password) throws JaqpotNotAuthorizedException {

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
//    @GET
//    @Path("/checkcap")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Authorize
//    @ApiOperation(
//            value = "Requests authorization from SSO",
//            notes = "Checks whether the client identified by the provided AA token can apply a method to a URI"
//    )
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, response = String.class, message = "You have authorization for the given URI and HTTP method")
//    })
//    public Response checkCreation(
//            @ApiParam(value = "Authorization token") @QueryParam("accessToken") String accessToken
//    ) throws JaqpotNotAuthorizedException {
//        User user = aaService.getUserFromSSO(accessToken);
//        aaService.isAdmin(accessToken);
//        return Response.ok(user)
//                .build();
//    }
}
