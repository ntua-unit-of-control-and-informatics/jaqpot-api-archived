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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.UnSecure;

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

    @Inject
    @UnSecure
    Client client;
    
    @Context
    SecurityContext securityContext;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Datasets",
            notes = "Finds all Datasets in the DB of Jaqpot and returns them in a list. Results can be obtained "
            + "either in the form of a URI list or as a JSON list as specified by the Accept HTTP header. "
            + "In the latter case, a list will be returned containing only the IDs of the datasets, their metadata "
            + "and their ontological classes. The parameter max, which specifies the maximum number of IDs to be "
            + "listed is limited to 500; if the client specifies a larger value, an HTTP Warning Header will be "
            + "returned (RFC 2616) with code P670.",
            response = Dataset.class,
            responseContainer = "List",
            position = 1)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Datasets found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listDatasets(
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", defaultValue = "10") @QueryParam("max") Integer max,
            @ApiParam(value = "createdBy") @QueryParam("creator") String creator
    ) {
        start = start != null ? start : 0;
        boolean doWarnMax = false;
        if (max == null || max > 500) {
            max = 500;
            doWarnMax = true;
        }
        Response.ResponseBuilder responseBuilder = Response
                .ok(datasetHandler.listOnlyIDsOfCreator(creator, start, max))
                .status(Response.Status.OK);
        if (doWarnMax) {
            responseBuilder.header("Warning", "P670 Parameter max has been limited to 500");
        }
        return responseBuilder.build();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Counts all datasets", response = Long.class)
    public Response countDatasets(@QueryParam("creator") String creator) {
        return Response.ok(datasetHandler.countAllOfCreator(creator)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(value = "Finds Dataset by Id",
            notes = "Finds specified Dataset",
            response = Dataset.class)
    public Response getDataset(@PathParam("id") String id,
            @QueryParam("rowStart") Integer rowStart,
            @QueryParam("rowMax") Integer rowMax,
            @QueryParam("colStart") Integer colStart,
            @QueryParam("colMax") Integer colMax,
            @QueryParam("stratify") String stratify,
            @QueryParam("seed") Long seed,
            @QueryParam("folds") Integer folds,
            @QueryParam("target_feature") String targetFeature) {
        Dataset dataset = datasetHandler.find(id, rowStart, rowMax, colStart, colMax, stratify, seed, folds, targetFeature);
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        return Response.ok(dataset).build();
    }

    @GET
    @Path("/featured")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Datasets",
            notes = "Finds Featured Datasets in the DB of Jaqpot and returns them in a list. Results can be obtained "
            + "either in the form of a URI list or as a JSON list as specified by the Accept HTTP header. "
            + "In the latter case, a list will be returned containing only the IDs of the datasets, their metadata "
            + "and their ontological classes. The parameter max, which specifies the maximum number of IDs to be "
            + "listed is limited to 500; if the client specifies a larger value, an HTTP Warning Header will be "
            + "returned (RFC 2616) with code P670.",
            response = Dataset.class,
            responseContainer = "List",
            position = 1)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Datasets found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listFeaturedDatasets(
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", defaultValue = "10") @QueryParam("max") Integer max
    ) {
        start = start != null ? start : 0;
        boolean doWarnMax = false;
        if (max == null || max > 500) {
            max = 500;
            doWarnMax = true;
        }
        Response.ResponseBuilder responseBuilder = Response
                .ok(datasetHandler.findFeatured(start, max))
                .status(Response.Status.OK);
        if (doWarnMax) {
            responseBuilder.header("Warning", "P670 Parameter max has been limited to 500");
        }
        return responseBuilder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/features")
    @ApiOperation(value = "Finds Dataset by Id",
            notes = "Finds specified Dataset",
            response = Dataset.class)
    public Response getDatasetFeatures(@PathParam("id") String id) {
        Dataset dataset = datasetHandler.find(id);
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        return Response.ok(dataset.getFeatures()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/meta")
    @ApiOperation(value = "Finds Dataset by Id",
            notes = "Finds specified Dataset",
            response = Dataset.class)
    public Response getDatasetMeta(@PathParam("id") String id) {
        Dataset dataset = datasetHandler.find(id);
        dataset.setDataEntry(new ArrayList<>());
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        return Response.ok(dataset).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/uri-list")
    @ApiOperation(value = "Creates a new Dataset",
            notes = "The new Dataset created will be assigned on a random generated Id",
            response = Dataset.class)
    public Response createDataset(Dataset dataset) throws URISyntaxException {
        ROG randomStringGenerator = new ROG(true);
        dataset.setId(randomStringGenerator.nextString(14));
        dataset.setFeatured(Boolean.FALSE);
        datasetHandler.create(dataset);

        return Response.created(new URI(dataset.getId())).entity(dataset).build();

    }

    @POST
    @Path("/merge")
    @Produces("text/uri-list")
    @ApiOperation(value = "Merges Datasets")
    public Response mergeDatasets(@FormParam("dataset_uris") String datasetURIs,
            @HeaderParam("subjectid") String subjectId) throws URISyntaxException {

        String[] datasets = datasetURIs.split(",");
        Dataset dataset = null;
        for (String datasetURI : datasets) {
            Dataset d = client.target(datasetURI)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("subjectid", subjectId)
                    .get(Dataset.class);
            dataset = DatasetFactory.mergeRows(dataset, d);
        }
        ROG randomStringGenerator = new ROG(true);
        dataset.setId(randomStringGenerator.nextString(14));
        dataset.setFeatured(Boolean.FALSE);
        datasetHandler.create(dataset);

        return Response.created(new URI(dataset.getId())).entity(dataset).build();

    }

    @DELETE
    @Path("/{id}")
    @ApiOperation("Deletes dataset")
    public Response deleteDataset(@PathParam("id") String id) {        
        Dataset ds = datasetHandler.find(id, 0, 0, null, null, null, null, null, null);
        String userName = securityContext.getUserPrincipal().getName();
        if(!ds.getMeta().getCreators().contains(userName)){
            return Response.status(Response.Status.FORBIDDEN).entity("You cannot delete a Dataset that was not created by you.").build();
        }
        ds.setId(id);
        datasetHandler.remove(ds);
        return Response.ok().build();
    }
}
