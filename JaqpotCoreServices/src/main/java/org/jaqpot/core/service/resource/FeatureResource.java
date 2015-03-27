/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.util.Date;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.validator.BibTeXValidator;
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
    public Response getFeatures(
            @ApiParam("Creator of the feature") @QueryParam("creator") String creator,
            @ApiParam("Generic query") @QueryParam("query") String query
    ) {
        return Response
                .ok(featureHandler.findAll())
                .status(Response.Status.OK)
                .build();
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
        if (feature.getId() == null) {
            feature.setId(UUID.randomUUID().toString());
        }
        feature.setCreatedBy(securityContext.getUserPrincipal().getName());

        featureHandler.create(feature);
        return Response
                .ok(feature)
                .status(Response.Status.OK)
                .build();

    }
    /**
     * Feature API: GET /feature/{id} POST /feature/{id} PUT /feature/{id}
     * DELETE /feature/{id}
     */
}
