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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.model.BibTeX;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.factory.ErrorReportFactory;

/**
 *
 * @author chung
 */
@Path("feature")
@Api(value = "/feature", description = "Feature API")
@Produces({"application/json", "text/uri-list"})
public class FeatureResource {
    
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
                .ok(ErrorReportFactory.notImplementedYet())
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();       
    }
    
    /**
     * Feature API:
     * GET /feature/{id}
     * POST /feature/{id}
     * PUT /feature/{id}
     * DELETE /feature/{id}
     */
}
