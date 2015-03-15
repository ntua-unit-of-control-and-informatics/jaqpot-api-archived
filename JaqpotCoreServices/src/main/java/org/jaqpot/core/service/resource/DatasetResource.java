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
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.model.dto.dataset.Dataset;

/**
 *
 * @author hampos
 */
@Path("dataset")
@Api(value = "/dataset", description = "Dataset API")
@Produces({"application/json", "text/uri-list"})
public class DatasetResource {

    @EJB
    DatasetHandler datasetHandler;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(value = "Finds Dataset by Id",
            notes = "Finds specified Dataset",
            response = Dataset.class)
    public Response getDataset(@PathParam("id") String id) {
        Dataset dataset = datasetHandler.find(id);
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        return Response.ok(dataset).build();
    }

}
