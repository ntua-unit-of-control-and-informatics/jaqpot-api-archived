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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import javax.ejb.EJB;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.data.DoaHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.Discussion;
import org.jaqpot.core.model.Doa;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;
import org.jaqpot.core.service.httphandlers.Rights;

/**
 *
 * @author pantelispanka
 */
@Path("doa")
@Api(value = "/doa", description = "Doa API")
@Produces({"application/json"})
public class DoaResource {

    @EJB
    ModelHandler modelHandler;

    @EJB
    DoaHandler doaHandler;

    @EJB
    UserHandler userHandler;

    @EJB
    Rights rights;

    @Context
    SecurityContext securityContext;

    private final ROG randomStringGenerator = new ROG(true);

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates the domain of applicability for a Jaqpot model",
            notes = "Creates the domain of applicability for a Jaqpot model",
            response = Discussion.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Doi was created successfully.")
        ,
        @ApiResponse(code = 400, message = "Bad request: malformed feature")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this resource")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response storeDoi(Doa doa) throws JaqpotNotAuthorizedException {

        String creator = securityContext.getUserPrincipal().getName();

        String modelId = doa.getModelId();
        Model model = modelHandler.findMeta(modelId);

        if (!model.getMeta().getCreators().contains(creator)) {
            throw new JaqpotNotAuthorizedException("You are not authorized to "
                    + "create doi. You are not one of the creators of this model");
        }

        try {
            String doaId = randomStringGenerator.nextStringId(20);
            MetaInfo minf = MetaInfoBuilder.builder()
                    .addCreators(creator)
                    .addSources("model/" + modelId)
                    .build();
            doa.setMeta(minf);
            doa.setId(doaId);
            doaHandler.create(doa);
        } catch (JaqpotDocumentSizeExceededException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
        return Response.ok(doa.getId()).status(Response.Status.CREATED).build();
    }

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates the domain of applicability for a Jaqpot model",
            notes = "Creates the domain of applicability for a Jaqpot model",
            response = Discussion.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Doi was created successfully.")
        ,
        @ApiResponse(code = 400, message = "Bad request: malformed feature")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this resource")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getDoiBySources(
            @QueryParam("hasSources") String hasSources
    ) throws JaqpotNotAuthorizedException {

        String creator = securityContext.getUserPrincipal().getName();
        Doa doa = doaHandler.findBySources(hasSources);

        String[] source = hasSources.split("/");

        Model modelToCheck = modelHandler.findMeta(source[1]);
        User user = userHandler.find(creator);
        
        if(modelToCheck == null){
            ErrorReport erf = ErrorReportFactory.notFoundError("Model not found");
            return Response.ok(erf).status(Response.Status.NOT_FOUND).build();
        }
        
        Boolean canSee = rights.canView(modelToCheck.getMeta(), user);
        if (Objects.equals(canSee, Boolean.FALSE)) {
            throw new JaqpotNotAuthorizedException("You are not authorized to "
                    + "see the models doi.");
        }

        if (doa == null) {
            ErrorReport erf = ErrorReportFactory.notFoundError("Doa not found");
            return Response.ok(erf).status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(doa).build();
    }

}
