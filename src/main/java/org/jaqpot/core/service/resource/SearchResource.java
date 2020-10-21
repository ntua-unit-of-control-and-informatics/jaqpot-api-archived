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

//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.messagebeans.IndexEntityProducer;
import org.jaqpot.core.messagebeans.SearchSessionProducer;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.JaqpotEntity;
import org.jaqpot.core.model.dto.search.FountEntities;
import org.jaqpot.core.model.dto.search.SearchSession;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.search.engine.JaqpotSearch;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.sessions.SessionClient;

/**
 *
 * @author pantelispanka
 */
@Path("/search")
//@Api(value = "/search", description = "Search API")
@Produces({"application/json", "text/uri-list"})
@Tag(name = "search")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class SearchResource {

    @EJB
    JaqpotSearch jaqpotSearch;

    @EJB
    SearchSessionProducer ssp;

    @Context
    SecurityContext securityContext;

    @EJB
    SessionClient sessionClient;

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    /*@ApiOperation(
     value = "Searches for Jaqpot Entities",
     notes = "Searches for Jaqpot Entities"
     )
     @ApiResponses(value = {
     @ApiResponse(code = 401, response = ErrorReport.class, message = "Wrong, missing or insufficient credentials. Error report is produced.")
     ,
     @ApiResponse(code = 200, response = JaqpotEntity.class, responseContainer = "List", message = "A list of jaqpot entities found")
     ,
     @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")

     })*/
   
    @Operation(summary = "Searches for Jaqpot Entities",
            description = "Searches for Jaqpot Entities",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = JaqpotEntity.class)),
                        description = "A list of jaqpot entities found"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response search(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "term", description = "term", schema = @Schema(implementation = String.class)) @QueryParam("term") String term
    ) {

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        
        String userId = securityContext.getUserPrincipal().getName();
        ROG rog = new ROG(true);
        String sessionId = rog.nextStringId(12);

        String termf = term.replace("\\", "")
                .replace("\n", "")
                .replace("\r", " ")
                .replace("#", " ")
                .replace('"', ' ')
                .replaceAll("(\\d+)-(?=\\d)", "$1_")
                .replace("-", " ")
                .replace(".", " ")
                .replace(":", " ")
                .replace("[", " ")
                .replace("]", " ")
                .replace(";", " ")
                .replaceAll(":", " ")
                .replace("<", " ")
                .replace(">", " ")
                .replace("*", " ").replaceAll("[^\\x20-\\x7e]", " ");
        String termf1 = termf.replace("*", "");
        ssp.startSearchSession(userId, sessionId, termf1, apiKey);
        List<String> entityIds = new ArrayList();
        this.sessionClient.searchSession(sessionId, entityIds, Boolean.FALSE);
        SearchSession searchS = new SearchSession();
        searchS.setSeacrhSession(sessionId);

        return Response
                .ok(searchS)
                .build();
    }

    @GET
    @Path("/session")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(summary = "List of found entities through session",
            description = "List of found entities through session",
            responses = {
                @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = FountEntities.class)),
                        description = "A list of jaqpot entities found"),
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced."),
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response searchSession(
           @Parameter(name = "session", description = "session", schema = @Schema(implementation = String.class)) @QueryParam("session") String term,
           @Parameter(name = "from", description = "from", schema = @Schema(implementation = Integer.class)) @QueryParam("from") Integer from,
           @Parameter(name = "to", description = "to", schema = @Schema(implementation = Integer.class)) @QueryParam("to") Integer to
    ) {

        if (from == null) {
            from = 0;
        }

        if (to == null) {
            to = 20;
        }
        Boolean exists = this.sessionClient.searchSessionExcists(term);
        if (exists.equals(false)) {
            throw new NotFoundException("Search request is by long gone. Please search again!");
        }

        FountEntities fe = null;
        try {
            fe = this.sessionClient.searchSessionFound(term, from, to);
        } catch (Exception e) {
            throw new NotFoundException(e.getLocalizedMessage());
        }

        return Response
                .ok(fe)
                .build();
    }

}
