/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.models.ChempotDto;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.data.ChempotService;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;
import org.jaqpot.core.service.quotas.QuotsClient;
import xyz.euclia.euclia.accounts.client.models.User;
import xyz.euclia.jquots.models.CanProceed;

/**
 *
 * @author pantelispanka
 */
@Path("/chempot")
//@Api(value = "/model", description = "Models API")
@Produces({"application/json"})
@Tag(name = "chempot")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class ChempotResource {
    
    @EJB
    QuotsClient quotsClient;
    
    @EJB
    UserHandler userHandler;
    
    @EJB
    ChempotService chempotService;
    
    @Context
    SecurityContext securityContext;
    
    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Creates Prediction with smiles input",
            description = "Creates Prediction with smiles input",
            responses = {
                @ApiResponse(content = @Content(schema = @Schema(implementation = Task.class))),},
            extensions = {
                @Extension(properties = {
            @ExtensionProperty(name = "orn-@type", value = "x-orn:JaqpotPredictionTaskId"),}
                )
                ,
                @Extension(name = "orn:expects", properties = {
            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AcessToken")
            ,
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotModelId")
            ,
                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Dataset")
        })
                ,
                @Extension(name = "orn:returns", properties = {
            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotPredictionTaskId")
        })
            })
    @org.jaqpot.core.service.annotations.Task
    public Response makePrediction(
            ChempotDto chempotDto,
            @Parameter(name = "Authorization", description = "Authorization required", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key
    ) throws QuotaExceededException, InterruptedException, ExecutionException, InternalServerErrorException, JaqpotNotAuthorizedException, ParseException, JaqpotDocumentSizeExceededException{
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        
        User user = userHandler.find(securityContext.getUserPrincipal().getName(), apiKey);
        
        CanProceed cp = quotsClient.canUserProceedSync(user.get_id(), "PREDICTION", "1", apiKey);
        if (cp.isProceed() == false) {
            throw new QuotaExceededException("Dear user, your credits has been "
                    + "exceeded. Request for more through accounts.jaqpot.org");
        }
        
        Map<String, Object> options = new HashMap<>();
        options.put("smiles", chempotDto.getSmiles());
        options.put("api_key", apiKey);
        options.put("modelId", chempotDto.getModelId());
        options.put("descriptors", chempotDto.getDescriptors());
        options.put("doa", String.valueOf(chempotDto.isWithDoa()));
        options.put("userId", user.get_id());
//        options.put("base_uri", uriInfo.getBaseUri().toString());
        Task task = chempotService.initiatePrediction(options, user.getEmail());
        return Response.ok(task).build();
        
    }
    
    
}
