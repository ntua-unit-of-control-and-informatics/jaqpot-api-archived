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
import java.security.GeneralSecurityException;
import javax.ws.rs.FormParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.client.Util;
import org.jaqpot.core.service.dto.aa.AuthToken;

/**
 *
 * @author hampos
 */
@Path("user")
@Api(value = "/user", description = "Operations about Users")
public class UserResource {

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
        try {
            Client client = Util.buildUnsecureRestClient();
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.putSingle("username", username);
            formData.putSingle("password", password);
            Response response = client.target("https://openam.in-silico.ch/auth/authenticate")
                    .request()
                    .post(Entity.form(formData));
            String responseValue = response.readEntity(String.class);
            response.close();
            if (response.getStatus() == 401) {
                return Response
                        .ok(ErrorReportFactory.authenticationRequired(responseValue))
                        .status(response.getStatus())
                        .build();
            } else {
                AuthToken aToken = new AuthToken();
                aToken.setAuthToken(responseValue.substring(9).replaceAll("\n", ""));
                aToken.setUserName(username);
                return Response
                        .ok(aToken)
                        .status(response.getStatus())
                        .build();
            }

        } catch (GeneralSecurityException ex) {
            throw new InternalServerErrorException();
        }
    }
}
