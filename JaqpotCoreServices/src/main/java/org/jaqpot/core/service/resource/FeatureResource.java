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
import java.util.Date;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("feature")
@Api(value = "/feature", description = "Feature API")
@Produces({"application/json", "text/uri-list"})
public class FeatureResource {

    private static final String DEFAULT_FEATURE = "{\n" +
            "  \"units\":\"kDa\",\n" +
            "  \"ontologicalClasses\": [\n" +
            "    \"ot:Feature\",\n" +
            "    \"ot:NumericFeature\"\n" +
            "  ],\n" +
            "  \"meta\": {\n" +
            "    \"titles\":        [ \"Molecular Weight\" ],\n" +
            "    \"descriptions\":  [ \"The molecular weight is the mass of one mole of a substance.\" ],\n" +
            "    \"subjects\":      [ \"MW\", \"Molecular Weight\"],\n" +
            "    \"sameAs\":        [ \"ot:MolecularWeight\"],\n" +
            "    \"seeAlso\":       [ \"http://en.wikipedia.org/wiki/Molecular_mass\" ],\n" +
            "    \"hasSources\":    [ \"http://enanomapper.ntua.gr:8880/jaqpot/services/algorithm/DescCalcMW\"]\n" +
            "  }\n" +
            "}";

    @Context
    UriInfo uriInfo;
    
    @EJB
    AAService aaService;

    @EJB
    FeatureHandler featureHandler;

    @Context
    SecurityContext securityContext;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all features",
            notes = "Finds all features entries in the DB of Jaqpot and returns them in a list",
            response = Feature.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Feature entries found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listFeatures(
            @ApiParam("Creator of the feature") @QueryParam("creator") String creator,
            @ApiParam("Generic query") @QueryParam("query") String query,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max", defaultValue = "10") @QueryParam("max") Integer max
    ) {
        //TODO Support querying at GET /feature
        return Response
                .ok(featureHandler.findAll(start, max))
                .status(Response.Status.OK)
                .build();
    }
    
     @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(value = "Finds Feature by ID",
            notes = "Finds specified Feature (by ID)",
            response = Feature.class)
    public Response getFeature(@PathParam("id") String id) {
        Feature feature = featureHandler.find(id);
        if (feature == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        return Response.ok(feature).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Feature",
            notes = "Creates a new feature which is assigned a random unique ID",
            response = Feature.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Feature was created successfully."),
        @ApiResponse(code = 400, message = "Bad request: malformed feature"),
        @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    @Authorize
    public Response createFeature(
            @ApiParam(value = "Clients need to authenticate in order to create resources on the server")
            @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "BibTeX in JSON representation compliant with the BibTeX specifications. "
                    + "Malformed BibTeX entries with missing fields will not be accepted.", required = true,
                    defaultValue = DEFAULT_FEATURE) Feature feature
    ) throws JaqpotNotAuthorizedException {
        // First check the subjectid:
        if (subjectId == null || !aaService.validate(subjectId)) {
            throw new JaqpotNotAuthorizedException("Invalid auth token");
        }
        if (feature == null) {
            ErrorReport report = ErrorReportFactory.badRequest("No feature provided; check out the API specs",
                    "Clients MUST provide a Feature document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        if (feature.getMeta() == null) {
            feature.setMeta(new MetaInfo());
        }
        feature.getMeta().setDate(new Date());
        ROG rog = new ROG(true);
        if (feature.getId() == null) {
            feature.setId(rog.nextString(10));
        }
        feature.setCreatedBy(securityContext.getUserPrincipal().getName());

        featureHandler.create(feature);
        return Response
                .ok(feature)
                .status(Response.Status.OK)
                .header("Location", uriInfo.getBaseUri().toString() + "feature/" + feature.getId())
                .build();

    }
    /**
     * Feature API: GET /feature/{id} POST /feature/{id} PUT /feature/{id}
     * DELETE /feature/{id}
     */
}
