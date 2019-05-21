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

//import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.AAService;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.JaqpotForbiddenException;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("feature")
//@Api(value = "/feature", description = "Feature API")
@Produces({"application/json", "text/uri-list"})
public class FeatureResource {

    private static final String DEFAULT_FEATURE = "{\n"
            + "  \"units\":\"kDa\",\n"
            + "  \"ontologicalClasses\": [\n"
            + "    \"ot:Feature\",\n"
            + "    \"ot:NumericFeature\"\n"
            + "  ],\n"
            + "  \"meta\": {\n"
            + "    \"titles\":        [ \"Molecular Weight\" ],\n"
            + "    \"descriptions\":  [ \"The molecular weight is the mass of one mole of a substance.\" ],\n"
            + "    \"subjects\":      [ \"MW\", \"Molecular Weight\"],\n"
            + "    \"sameAs\":        [ \"ot:MolecularWeight\"],\n"
            + "    \"seeAlso\":       [ \"http://en.wikipedia.org/wiki/Molecular_mass\" ],\n"
            + "    \"hasSources\":    [ \"http://enanomapper.ntua.gr:8880/jaqpot/services/algorithm/DescCalcMW\"]\n"
            + "  }\n"
            + "}";

    @Context
    UriInfo uriInfo;

    @EJB
    AAService aaService;

    @EJB
    FeatureHandler featureHandler;

    @Context
    SecurityContext securityContext;

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    /*@ApiOperation(value = "Lists features",
            notes = "Lists Feature entries in the DB of Jaqpot and returns them in a list. "
            + "Results can be obtained "
            + "either in the form of a URI list or as a JSON list as specified by the Accept HTTP header. "
            + "In the latter case, a list will be returned containing only the IDs of the features, their metadata "
            + "and their ontological classes. The parameter max, which specifies the maximum number of IDs to be "
            + "listed is limited to 500; if the client specifies a larger value, an HTTP Warning Header will be "
            + "returned (RFC 2616) with code P670.",
            response = Feature.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Feature entries found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })*/
    @Operation(summary = "Lists features",
            description = "Lists Feature entries in the DB of Jaqpot and returns them in a list. "
            + "Results can be obtained "
            + "either in the form of a URI list or as a JSON list as specified by the Accept HTTP header. "
            + "In the latter case, a list will be returned containing only the IDs of the features, their metadata "
            + "and their ontological classes. The parameter max, which specifies the maximum number of IDs to be "
            + "listed is limited to 500; if the client specifies a larger value, an HTTP Warning Header will be "
            + "returned (RFC 2616) with code P670.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Feature.class))), description = "Feature entries found and are listed in the response body"),
                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            })
    public Response listFeatures(
            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(description = "Generic query") @QueryParam("query") String query,
            @Parameter(description = "start", schema = @Schema(type = "String", defaultValue = "0")) @QueryParam("start") Integer start,
            @Parameter(description = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", schema = @Schema(type = "String", defaultValue = "10")) @QueryParam("max") Integer max
    ) {
        //TODO Support querying at GET /feature
        if (max == null || max > 500) {
            max = 500;
        }
        String creator = securityContext.getUserPrincipal().getName();
        return Response.ok(featureHandler.listMetaOfCreator(creator, start != null ? start : 0, max))
                .status(Response.Status.OK)
                .header("total", featureHandler.countAllOfCreator(creator))
                .build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Path("/{id}")
    //@ApiOperation(value = "Finds Feature by ID",
    //        notes = "Finds specified Feature (by ID)",
    //        response = Feature.class)
    @Operation(summary = "Finds Feature by ID",
               description = "Finds specified Feature (by ID)",
               responses = {
                   @ApiResponse(content = @Content(schema = @Schema(implementation = Feature.class)))
               })
    public Response getFeature(
            //@ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id) {
        Feature feature = featureHandler.find(id);
        if (feature == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        return Response.ok(feature).build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)
    /*@ApiOperation(value = "Creates a new Feature",
            notes = "Creates a new feature which is assigned a random unique ID. When creating a new feature, clients must wary not only for "
            + "its syntactic correctness, but also for its semantic completeness. It is strongly recommended to add a comprehensive and "
            + "identifying title to your feature using the <code>meta.titles</code> field, to add a description in <code>meta.descriptions</code> "
            + "and also to add a list of tags in <code>meta.subjects</code> that will facilitate the discoverability of your features later. "
            + "Additionally, all features should be annotated with appropriate ontological classes (from the OpenTox ontology), such as "
            + "<code>ot:Feature</code>, <code>ot:NumericFeature</code> and <code>ot:NominalFeature</code>. Features that are created as "
            + "prediction features for a model or are descriptors that can be calculated using a descriptor calculation web "
            + "service should be linked to this/these service(s) using <code>meta.hasSources</code>. Finally, nominal features should define their "
            + "admissible values in <code>admissibleValues</code>. Malformed feature documents will not be accepted by the server and an "
            + "error report will be generated and returned to the client. Notice also that authentication, authorization and accounting "
            + "restrictions may apply.",
            response = Feature.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Feature was created successfully."),
        @ApiResponse(code = 400, message = "Bad request: malformed feature"),
        @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })*/
    @Operation(summary = "Creates a new Feature",
            description = "Creates a new feature which is assigned a random unique ID. When creating a new feature, clients must wary not only for "
            + "its syntactic correctness, but also for its semantic completeness. It is strongly recommended to add a comprehensive and "
            + "identifying title to your feature using the <code>meta.titles</code> field, to add a description in <code>meta.descriptions</code> "
            + "and also to add a list of tags in <code>meta.subjects</code> that will facilitate the discoverability of your features later. "
            + "Additionally, all features should be annotated with appropriate ontological classes (from the OpenTox ontology), such as "
            + "<code>ot:Feature</code>, <code>ot:NumericFeature</code> and <code>ot:NominalFeature</code>. Features that are created as "
            + "prediction features for a model or are descriptors that can be calculated using a descriptor calculation web "
            + "service should be linked to this/these service(s) using <code>meta.hasSources</code>. Finally, nominal features should define their "
            + "admissible values in <code>admissibleValues</code>. Malformed feature documents will not be accepted by the server and an "
            + "error report will be generated and returned to the client. Notice also that authentication, authorization and accounting "
            + "restrictions may apply.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Feature.class)), description = "Feature was created successfully."),
                @ApiResponse(responseCode = "400", description = "Bad request: malformed feature"),
                @ApiResponse(responseCode = "401", description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            })
    public Response createFeature(
            //@ApiParam(value = "Clients need to authenticate in order to create resources on the server")
            @Parameter(description = "Clients need to authenticate in order to create resources on the server")
            @HeaderParam("Authorization") String api_key,
            //@ApiParam(value = "Feature in JSON representation compliant with the Feature specifications. "
            //        + "Malformed Feature entries with missing fields will not be accepted.", required = true,
            //        defaultValue = DEFAULT_FEATURE) Feature feature
            @Parameter(description = "Feature in JSON representation compliant with the Feature specifications. "
                   + "Malformed Feature entries with missing fields will not be accepted.", required = true,
                   schema = @Schema(implementation = Feature.class, defaultValue = DEFAULT_FEATURE)) Feature feature
    ) throws JaqpotNotAuthorizedException, JaqpotDocumentSizeExceededException {
        if (feature == null) {
            ErrorReport report = ErrorReportFactory.badRequest("No feature provided; check out the API specs",
                    "Clients MUST provide a Feature document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        if (feature.getMeta() == null) {
            feature.setMeta(new MetaInfo());
        }
        feature.getMeta().setDate(new Date());
        feature.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
        ROG rog = new ROG(true);
        if (feature.getId() == null) {
            feature.setId(rog.nextString(10));
        }

        featureHandler.create(feature);
        return Response
                .ok(feature)
                .status(Response.Status.OK)
                .header("Location", uriInfo.getBaseUri().toString() + "feature/" + feature.getId())
                .build();

    }

    @DELETE
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    /*@ApiOperation(value = "Deletes a particular Feature resource.",
            notes = "Deletes a Feature of a given ID. The method is idempondent, that is, it can be used more than once without "
            + "triggering an exception/error. If the Feature does not exist, the method will return without errors. "
            + "Authentication and authorization requirements apply, so clients that are not authenticated with a "
            + "valid token or do not have sufficient priviledges will not be able to delete a Feature using this method.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Feature entry was deleted successfully."),
        @ApiResponse(code = 401, message = "You are not authorized to delete this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })*/
    @Operation(summary = "Deletes a particular Feature resource.",
            description = "Deletes a Feature of a given ID. The method is idempondent, that is, it can be used more than once without "
            + "triggering an exception/error. If the Feature does not exist, the method will return without errors. "
            + "Authentication and authorization requirements apply, so clients that are not authenticated with a "
            + "valid token or do not have sufficient priviledges will not be able to delete a Feature using this method.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Feature entry was deleted successfully."),
                @ApiResponse(responseCode = "401", description = "You are not authorized to delete this resource"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            })
    public Response deleteFeature(
           // @ApiParam("Clients need to authenticate in order to create resources on the server") @HeaderParam("Authorization") String api_key,
           // @ApiParam(value = "ID of the Model.", required = true) @PathParam("id") String id
            @Parameter(description = "Clients need to authenticate in order to create resources on the server") @HeaderParam("Authorization") String api_key,
            @Parameter(description = "ID of the Model.", required = true) @PathParam("id") String id
    ) throws JaqpotForbiddenException {
        Feature feature = new Feature(id);
        MetaInfo metaInfo = feature.getMeta();
        if (metaInfo.getLocked())
            throw new JaqpotForbiddenException("You cannot delete a Feature that is locked.");

        featureHandler.remove(new Feature(id));
        return Response.ok().build();
    }

    @PUT
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(summary = "Places a new Feature at a particular URI",
            description = "Creates a new Feature entry at the specified URI. If a Feature already exists at this URI,"
            + "it will be replaced. If, instead, no Feature is stored under the specified URI, a new "
            + "Feature entry will be created. Notice that authentication, authorization and accounting (quota) "
            + "restrictions may apply.",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Feature.class)), description = "Feature entry was created successfully."),
                @ApiResponse(responseCode = "400", description = "Feature entry was not created because the request was malformed"),
                @ApiResponse(responseCode = "401", description = "You are not authorized to create a feature on the server"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
    
            })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putFeature(
            //@ApiParam(value = "ID of the Feature.", required = true) @PathParam("id") String id,
            //@ApiParam(value = "Feature in JSON", defaultValue = DEFAULT_FEATURE, required = true) Feature feature,
            //@ApiParam("Clients need to authenticate in order to create resources on the server") @HeaderParam("Authorization") String api_key
            @Parameter(description = "ID of the Feature.", required = true) @PathParam("id") String id,
            @Parameter(description = "Feature in JSON", required = true, schema = @Schema(implementation = Feature.class, defaultValue = DEFAULT_FEATURE)) Feature feature,
            @Parameter(description = "Clients need to authenticate in order to create resources on the server") @HeaderParam("Authorization") String api_key
    )throws JaqpotDocumentSizeExceededException  {
        if (feature == null) {
            ErrorReport report = ErrorReportFactory.badRequest("No feature provided; check out the API specs",
                    "Clients MUST provide a Feature document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        feature.setId(id);

        Feature foundFeature = featureHandler.find(id);
        if (foundFeature != null) {
            featureHandler.edit(feature);
        } else {
            featureHandler.create(feature);
        }

        return Response
                .ok(feature)
                .status(Response.Status.CREATED)
                .header("Location", uriInfo.getBaseUri().toString() + "feature/" + feature.getId())
                .build();
    }
}
