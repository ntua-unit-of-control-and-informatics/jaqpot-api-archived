///*
// *
// * JAQPOT Quattro
// *
// * JAQPOT Quattro and the components shipped with it, in particular:
// * (i)   JaqpotCoreServices
// * (ii)  JaqpotAlgorithmServices
// * (iii) JaqpotDB
// * (iv)  JaqpotDomain
// * (v)   JaqpotEAR
// * are licensed by GPL v3 as specified hereafter. Additional components may ship
// * with some other licence as will be specified therein.
// *
// * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// * 
// * Source code:
// * The source code of JAQPOT Quattro is available on github at:
// * https://github.com/KinkyDesign/JaqpotQuattro
// * All source files of JAQPOT Quattro that are stored on github are licensed
// * with the aforementioned licence. 
// */
//package org.jaqpot.core.service.resource;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
//import io.swagger.annotations.Extension;
//import io.swagger.annotations.ExtensionProperty;
//import java.util.HashMap;
//import javax.ejb.EJB;
//import javax.ws.rs.BadRequestException;
//import javax.ws.rs.GET;
//import javax.ws.rs.HeaderParam;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import org.jaqpot.core.data.FeatureHandler;
//import org.jaqpot.core.data.ModelHandler;
//import org.jaqpot.core.model.Feature;
//import org.jaqpot.core.model.Model;
//import org.jaqpot.core.model.dto.models.QuickPredictionNeeds;
//import org.jaqpot.core.service.annotations.TokenSecured;
//import org.jaqpot.core.service.authenitcation.RoleEnum;
//
///**
// *
// * @author pantelispanka
// */
//@Path("/quickpredictions")
//@Api(value = "/quickpredictions", description = "Quick Predictions API")
//@Produces({"application/json"})
//public class QuickPredictionsResource {
//
//    @EJB
//    ModelHandler modelHandler;
//    
//    @EJB
//    FeatureHandler featHandler;
//    
//    
//    @GET
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("/{id}")
//    @Produces({MediaType.APPLICATION_JSON})
//    @ApiOperation(value = "Gets the form from a model if quickpredictiosn are supported",
//            notes = "Gets the form that a pretrained Model needs in order to create a quick prediction"
//            + "Get that form and submit it with the values needed in the POST function",
//            response = QuickPredictionNeeds.class,
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Model"),}
//                )
//                ,
//                @Extension(name = "orn:expects", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AcessToken")
//            ,
//                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotModelId")
//        })
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:QuickPredictionForm")
//        })
//            })
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Model quick prediction needs"),
//        @ApiResponse(code = 401, message = "You are not authorized to access this model"),
//        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
//        @ApiResponse(code = 404, message = "This model was not found."),
//        @ApiResponse(code = 400, message = "This model does not support quick predictions"),
//        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
//    })
//    public Response getPretrainedModel(
//            @PathParam("id") String id,
//            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("Authorization") String api_key) {
//        String[] apiA = api_key.split("\\s+");
//        String apiKey = apiA[1];
//        Model model = modelHandler.findModel(id);
////        if(!model.getPretrained().equals(Boolean.TRUE)){
////            throw new BadRequestException("Tour provided model does not support quick predictions");
////        }
//        QuickPredictionNeeds quickPredNeeds = new QuickPredictionNeeds();
//        quickPredNeeds.setId(model.getId());
//        HashMap needs = new HashMap();
//        model.getIndependentFeatures().forEach((indFeat) -> {
//            String[] indFeatAll = indFeat.split("/");
//            int idon = indFeatAll.length;
//            String featId =  indFeatAll[idon - 1];
//            Feature indFeature = featHandler.find(featId);
//            needs.put(indFeature.getActualIndependentFeatureName(), null);
//        });
//        quickPredNeeds.setIndependentFeatures(needs);
//        return Response.ok(quickPredNeeds).build();
//    }
//
//    @POST
//    @Path("/")
//    @ApiOperation(value = "Gets the form from a model if quickpredictiosn are supported",
//            notes = "Gets the form that a pretrained Model needs in order to create a quick prediction"
//            + "Get that form and submit it with the values needed in the POST function",
//            response = QuickPredictionNeeds.class,
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Model"),}
//                )
//                ,
//                @Extension(name = "orn:expects", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:AcessToken")
//            ,
//                    @ExtensionProperty(name = "x-orn-@id", value = "x-orn:JaqpotQuickPredictionNeeds")
//        })
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Predictions")
//        })
//            })
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Predictions"),
//        @ApiResponse(code = 401, message = "You are not authorized to access this model"),
//        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
//        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
//    })
//    public Response getQuickPrediction(QuickPredictionNeeds quickPredNeeds,
//            @ApiParam(value = "Clients need to authenticate in order to access models") @HeaderParam("Authorization") String api_key){
//        Model model = modelHandler.find(quickPredNeeds.getId());
//        return Response.ok().build();
//    }
//    
//}
