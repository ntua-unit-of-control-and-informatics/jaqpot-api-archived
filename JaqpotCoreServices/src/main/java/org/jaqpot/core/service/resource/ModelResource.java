/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.model.Model;

/**
 *
 * @author hampos
 */
@Path("model")
@Api(value = "/model", description = "Operations about Models")
public class ModelResource {

    @EJB
    ModelHandler modelHandler;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Models",
            notes = "Finds all Models from Jaqpot Dataset",
            response = Model.class,
            responseContainer = "List")
    public Response getModels() {
        return Response.ok(modelHandler.findAll()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(value = "Finds Model by Id",
            notes = "Finds specified Model",
            response = Model.class)
    public Response getModel(@PathParam("id") String id) {
        Model model = modelHandler.find(id);
        if (model == null) {
            throw new NotFoundException();
        }
        return Response.ok(model).build();
    }
}
