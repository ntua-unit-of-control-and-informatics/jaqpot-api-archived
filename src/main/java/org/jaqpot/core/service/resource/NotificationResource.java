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
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.data.NotificationHandler;
import org.jaqpot.core.data.OrganizationHandler;
import org.jaqpot.core.model.Notification;
import org.jaqpot.core.model.Notification.Type;
import org.jaqpot.core.model.Organization;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;

/**
 *
 * @author pantelispanka
 */
@Path("notification")
@Api(value = "/notification", description = "Notifications API", position = 1)
@Produces({"application/json"})
public class NotificationResource {
    
    @EJB
    NotificationHandler notifHandler;
    
    @EJB
    OrganizationHandler orgHandler;
    
    @Context
    SecurityContext securityContext;
    
    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Lists all Users notifications",
            notes = "Lists all Notifications of Jaqpot Users. ",
            response = Notification.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Notifications found and are listed in the response body")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this resource")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listUsersNotifications(
            @ApiParam(value = "Clients need to authenticate in order to access notifications") @HeaderParam("Authorization") String api_key,
            @ApiParam(value = "query", allowableValues = "UNREAD, ALL") @QueryParam("query") String query,
            @ApiParam(value = "category", allowableValues = "AFFILIATIONS, SHARES, INVITATIONS") @QueryParam("category") String category,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max", defaultValue = "10") @QueryParam("max") Integer max
    ) throws JaqpotNotAuthorizedException {


        String currentUserID = securityContext.getUserPrincipal().getName();
//        Long countedNots = notifHandler.countAllOfOwnersUnviewed(currentUserID);
        
        if(start == null){
            start = 0;
        }
        if(max ==null){
            max = 10;
        }

                
        List<Notification> nots = new ArrayList();
        Long total = null;
        if(query != null){
            nots = notifHandler.getOwnersNotifs(currentUserID, query , start, max);
            total = notifHandler.countAllOfOwnersUnviewed(currentUserID);
        }
        
        if(category != null) {
            nots.clear();
            if(category.equals("AFFILIATIONS")){
                nots = notifHandler.getAffiliationsToOrg(currentUserID, start, max);
                nots.addAll(notifHandler.getBrokenAffiliationsToOrg(currentUserID, start, max));
                total = notifHandler.countAffiliationsToOrg(currentUserID);
                total += notifHandler.countBrokenAffiliationsToOrg(currentUserID);
            }
            if(category.equals("SHARES")){
                nots = notifHandler.getShares(currentUserID, start, max);
                total = notifHandler.countShares(currentUserID);
            }
            if(category.equals("INVITATIONS")){
                nots = notifHandler.getInvitations(currentUserID, start, max);
                total = notifHandler.countInvitations(currentUserID);
            }
            
        }

        return Response
                .ok(nots).header("total", total)
                .build();
    }
    
    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates notification",
            notes = "Creates Notifications for Jaqpot Users. ",
            response = Notification.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Notifications created")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this resource")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response createNotification(
            @ApiParam(value = "Clients need to authenticate in order to create notification") @HeaderParam("Authorization") String api_key,
            Notification notif
    ) throws JaqpotNotAuthorizedException {


        String currentUserID = securityContext.getUserPrincipal().getName();
        
        if(notif.getType().equals(Type.AFFILIATION.toString())){
            Organization org = orgHandler.find(notif.getOrganizationShared());
            if(org == null){
                throw new BadRequestException("Organization not found");
            }
            if(!org.getMeta().getCreators().contains(currentUserID) || org.getMeta().getContributors() != null && !org.getMeta().getContributors().contains(currentUserID)){
                throw new JaqpotNotAuthorizedException("Cannot create the affiliation");
            }
            Organization orgAffil = orgHandler.find(notif.getAffiliatedOrg());
            if(orgAffil == null){
                throw new BadRequestException("Organization not found");
            }
            if(!orgAffil.getMeta().getCreators().contains(currentUserID) || orgAffil.getMeta().getContributors() != null && !orgAffil.getMeta().getContributors().contains(currentUserID)){
                throw new JaqpotNotAuthorizedException("Cannot create the affiliation");
            }
        }
        
        ROG randomStringGenerator = new ROG(true);
        notif.setId("NOT" + randomStringGenerator.nextString(24));        
        
        try{
            notifHandler.create(notif);
        }catch(Exception e){
            throw new BadRequestException("Could not create Notification, cause: " + e.getMessage());
        }
        
        return Response
                .ok(notif)
                .build();
    }
    
    @PUT
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Updates notification",
            notes = "Updates Notifications for Jaqpot Users. ",
            response = Notification.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Notifications updated")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this resource")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response updateNotification(
            @ApiParam(value = "Clients need to authenticate in order to create notification") @HeaderParam("Authorization") String api_key,
            Notification notif
    ) throws JaqpotNotAuthorizedException {


        String currentUserID = securityContext.getUserPrincipal().getName();
        
        if(!notif.getOwner().contains(currentUserID)){
            throw new JaqpotNotAuthorizedException("Could not update Notification, Only the creator can");
        }
        
        try{
            notifHandler.edit(notif);
        }catch(Exception e){
            throw new BadRequestException("Could not create Notification, cause: " + e.getMessage());
        }
        
        return Response
                .ok().status(Response.Status.ACCEPTED)
                .build();
    }
    
    
    @DELETE
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Updates notification",
            notes = "Updates Notifications for Jaqpot Users. ",
            response = Notification.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Notifications updated")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this resource")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response deleteNotification(
            @ApiParam(value = "Clients need to authenticate in order to create notification") @HeaderParam("Authorization") String api_key,
            Notification notif
    ) throws JaqpotNotAuthorizedException {


        String currentUserID = securityContext.getUserPrincipal().getName();
        
        if(!notif.getOwner().contains(currentUserID)){
            throw new JaqpotNotAuthorizedException("Could not delete Notification, Only the creator can");
        }
        
        try{
            notifHandler.remove(notif);
        }catch(Exception e){
            throw new BadRequestException("Could not delete Notification, cause: " + e.getMessage());
        }
        
        return Response
                .ok().status(Response.Status.ACCEPTED)
                .build();
    }
    
    @DELETE
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Updates notification",
            notes = "Updates Notifications for Jaqpot Users. ",
            response = Notification.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Notifications updated")
        ,
        @ApiResponse(code = 401, message = "You are not authorized to access this resource")
        ,
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)")
        ,
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response deleteNotificationById(
            @ApiParam(value = "Clients need to authenticate in order to create notification") @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id
    ) throws JaqpotNotAuthorizedException {


        String currentUserID = securityContext.getUserPrincipal().getName();
        
        Notification notifToDelete = notifHandler.find(id);
        
        if(!notifToDelete.getOwner().contains(currentUserID)){
            throw new JaqpotNotAuthorizedException("Could not delete Notification, Only the creator can");
        }
        
        try{
            notifHandler.remove(notifToDelete);
        }catch(Exception e){
            throw new BadRequestException("Could not delete Notification, cause: " + e.getMessage());
        }
        
        return Response
                .ok().status(Response.Status.ACCEPTED)
                .build();
    }
    
    
    
}
