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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.SwaggerDefinition;
//import io.swagger.models.Swagger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.httphandlers.SwaggerLdHandler;

/**
 *
 * @author pantelispanka
 */
//@Path("swagger")
//@Api(value = "/swaggerld")
//@SwaggerDefinition
//@OpenAPIDefinition
public class SwaggerLdResource {

    private static final Logger LOG = Logger.getLogger(SwaggerLdResource.class.getName());

    @Inject
    PropertyManager pm;

    @Inject
    SwaggerLdHandler shndl;

    @GET
    @Path("/ld")
//    @ApiOperation(value = "Get the swagger.json with the Ld annotations",
//            notes = "Implements long polling",
//            response = Swagger.class)
    @Operation(summary = "Get the swagger.json with the Ld annotations",
               description = "Implements long polling",
               responses = {
                   @ApiResponse(content = @Content(schema = @Schema(implementation = OpenAPI.class)))
               })
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSwaggerLd() {

//        String jaqpot_uri_l = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_LOCAL_IP);
//        
//        String schema = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_SCHEMA);
//        String jaqBase = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE);
//        String port = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_PORT);
//        String host = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_HOST);
//        String swagUri = null;
//        if(!jaqpot_uri_l.equals("null")){
//            swagUri = jaqpot_uri_l + jaqBase + "/swagger.json";
//        }
//        else if(port.equals("80")){
//            swagUri = schema + "://" + host + jaqBase + "/swagger.json";
//        }else{
//            swagUri = schema + "://" + host + ":" + port + jaqBase + "/swagger.json";
//        }
        String swagUri = "http://localhost:8080/jaqpot/services/swagger.json";
//        LOG.log(Level.INFO, "Fetched schema : {0}", schema);
//        LOG.log(Level.INFO, "Fetched jaqBase : {0}", jaqBase);
//        LOG.log(Level.INFO, "Fetched port : {0}", port);
//        LOG.log(Level.INFO, "Fetched host : {0}", host);
//        LOG.log(Level.INFO, "uri  created: {0}", swagUri);

        URL url = null;
        HttpURLConnection con = null;
        InputStream response = null;
        StringBuffer respString = new StringBuffer();
        try {
            url = new URL(swagUri);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            response = con.getInputStream();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(response));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                respString.append(inputLine);
            }

            in.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Mallformed url, or io connection");
        }

        String swagS = respString.toString();
        JsonNode swaggerLd = shndl.test(swagS);

        return Response.ok(swaggerLd).build();

    }

    @GET
    @Path("/ld/oct")
//    @ApiOperation(value = "Get the swagger.json with the Ld annotations as octet stream",
  //          notes = "Implements long polling",
   //         response = Swagger.class)
    @Operation(summary = "Get the swagger.json with the Ld annotations as octet stream",
               description = "Implements long polling",
               responses = {
                   @ApiResponse(content = @Content(schema = @Schema(implementation = OpenAPI.class)))
               })
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public Response getSwaggerLdOct() throws IOException {

        String swagUri = "http://localhost:8080/jaqpot/services/swagger.json";

        URL url = null;
        HttpURLConnection con = null;
        InputStream response = null;
        StringBuffer respString = new StringBuffer();
        try {
            url = new URL(swagUri);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            response = con.getInputStream();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(response));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                respString.append(inputLine);
            }

            in.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Mallformed url, or io connection");
        }

        String swagS = respString.toString();
        JsonNode swaggerLd = shndl.test(swagS);
//        String swagtxt = swaggerLd.asText();

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] swagbyt = objectMapper.writeValueAsBytes(swaggerLd);
        InputStream targetStream = new ByteArrayInputStream(swagbyt);

        return Response.ok(targetStream).build();

    }

}
