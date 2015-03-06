/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.service.client.Util;

/**
 *
 * @author hampos
 */
@Path("user")
@Api(value = "/user", description = "Operations about Users")
public class UserResource {

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Creates Security Token",
            notes = "Uses OpenAM server to get a security token.",
            response = String.class)
    public Response getAlgorithms(
            @FormParam("username") String username,
            @FormParam("password") String password) {
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
            return Response.status(response.getStatus()).entity(responseValue).build();
        } catch (GeneralSecurityException ex) {
            throw new InternalServerErrorException();
        }
    }
}
