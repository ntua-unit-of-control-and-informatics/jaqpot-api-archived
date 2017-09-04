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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.apache.commons.validator.routines.UrlValidator;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.ReportHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.factory.DatasetFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.client.ambit.Ambit;
import org.jaqpot.core.service.client.jpdi.JPDIClient;
import org.jaqpot.core.service.exceptions.JaqpotForbiddenException;
import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@Path("dataset")
@Api(value = "dataset", description = "Dataset API")
@Produces({"application/json", "text/uri-list"})
@Authorize
public class DatasetResource {

    private static final Logger LOG = Logger.getLogger(DatasetResource.class.getName());

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    UserHandler userHandler;

    @EJB
    ModelHandler modelHandler;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    ReportHandler reportHandler;

    @Inject
    Ambit ambitClient;

    @Inject
    @UnSecure
    Client client;

    @Inject
    JPDIClient jpdiClient;

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
            position = 1)
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = Dataset.class, responseContainer = "List",
                message = "Datasets found and are listed in the response body"),
        @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response listDatasets(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", defaultValue = "10") @QueryParam("max") Integer max
    ) {
        start = start != null ? start : 0;
        if (max == null || max > 500) {
            max = 500;
        }
        String creator = securityContext.getUserPrincipal().getName();
        return Response.ok(datasetHandler.listMetaOfCreator(creator, start, max))
                .status(Response.Status.OK)
                .header("total", datasetHandler.countAllOfCreator(creator))
                .build();

    }

    @GET
    @Produces({"text/csv", MediaType.APPLICATION_JSON})

    @Path("{id}")
    @ApiOperation(value = "Finds Dataset by Id",
            notes = "Finds specified Dataset"
            )
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Dataset.class, message = "Dataset was found"),
            @ApiResponse(code = 404, response = ErrorReport.class, message = "Dataset was not found in the system"),
            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response getDataset(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @PathParam("id") String id,
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
            position = 1)
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = Dataset.class, responseContainer = "List", message = "Datasets found and are listed in the response body"),
        @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response listFeaturedDatasets(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", defaultValue = "10") @QueryParam("max") Integer max
    ) {
        start = start != null ? start : 0;
        boolean doWarnMax = false;
        if (max == null || max > 500) {
            max = 500;
        }
        return Response.ok(datasetHandler.findFeatured(start, max))
                .status(Response.Status.OK)
                .header("total", datasetHandler.countFeatured())
                .build();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/features")
    @ApiOperation(value = "Finds Features of Dataset by Id",
            notes = "Finds specified Dataset's features")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Dataset.class, message = "Dataset's features found and are listed in the response body"),
            @ApiResponse(code = 404, response = ErrorReport.class, message = "Dataset was not found in the system"),
            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response getDatasetFeatures(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @PathParam("id") String id
    ) {
        Dataset dataset = datasetHandler.find(id);
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }
        return Response.ok(dataset.getFeatures()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/meta")
    @ApiOperation(value = "Finds MetaData of Dataset by Id",
            notes = "Finds specified Dataset's MetaData")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Dataset.class, message = "Dataset's meta data found and are listed in the response body"),
            @ApiResponse(code = 404, response = ErrorReport.class, message = "Dataset was not found in the system"),
            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response getDatasetMeta(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @PathParam("id") String id) {
        Dataset dataset = datasetHandler.find(id);
        dataset.setDataEntry(new ArrayList<>());
        if (dataset == null) {
            throw new NotFoundException("Could not find Dataset with id:" + id);
        }

        return Response.ok(dataset).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({"text/uri-list", MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new Dataset",
            notes = "The new Dataset created will be assigned on a random generated Id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Dataset.class, message = "Dataset was created succesfully"),
            @ApiResponse(code = 403, response = ErrorReport.class, message="Dataset quota has been exceeded"),
            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response createDataset(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            Dataset dataset) throws QuotaExceededException, URISyntaxException {

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long datasetCount = datasetHandler.countAllOfCreator(user.getId());
        int maxAllowedDatasets = new UserFacade(user).getMaxDatasets();

        if (datasetCount > maxAllowedDatasets) {
            LOG.info(String.format("User %s has %d datasets while maximum is %d",
                    user.getId(), datasetCount, maxAllowedDatasets));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + datasetCount + " datasets. "
                    + "No more than " + maxAllowedDatasets + " are allowed with your subscription.");
        }

        ROG randomStringGenerator = new ROG(true);
        dataset.setId(randomStringGenerator.nextString(14));
        dataset.setFeatured(Boolean.FALSE);
        if (dataset.getMeta() == null) {
            dataset.setMeta(new MetaInfo());
        }
        dataset.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
        dataset.setVisible(Boolean.TRUE);
        datasetHandler.create(dataset);

        return Response.created(new URI(dataset.getId())).entity(dataset).build();

    }

    @POST
    @Path("/empty")
    @Produces({"text/uri-list", MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new empty Dataset",
            notes = "The new empty Dataset created will be assigned on a random generated Id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Dataset.class, message = "Dataset was created succesfully"),
            @ApiResponse(code = 403, response = ErrorReport.class, message="Dataset quota has been exceeded"),
            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response createEmptyDataset(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @FormParam("title") String title,
            @FormParam("description") String description) throws URISyntaxException, QuotaExceededException {

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long datasetCount = datasetHandler.countAllOfCreator(user.getId());
        int maxAllowedDatasets = new UserFacade(user).getMaxDatasets();

        if (datasetCount > maxAllowedDatasets) {
            LOG.info(String.format("User %s has %d datasets while maximum is %d",
                    user.getId(), datasetCount, maxAllowedDatasets));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + datasetCount + " datasets. "
                    + "No more than " + maxAllowedDatasets + " are allowed with your subscription.");
        }

        Dataset emptyDataset = DatasetFactory.createEmpty(0);
        ROG randomStringGenerator = new ROG(true);
        emptyDataset.setId(randomStringGenerator.nextString(14));
        emptyDataset.setFeatured(Boolean.FALSE);
        emptyDataset.setMeta(MetaInfoBuilder.builder()
                .addTitles(title)
                .addDescriptions(description)
                .build()
        );

        emptyDataset.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
        emptyDataset.setVisible(Boolean.TRUE);
        datasetHandler.create(emptyDataset);

        return Response.created(new URI(emptyDataset.getId())).entity(emptyDataset).build();
    }

    @POST
    @Path("/merge")
    @ApiOperation(value = "Merges Datasets",
            notes = "The new intersected Dataset created will be assigned on a random generated Id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Dataset.class, message = "Dataset was created succesfully"),
            @ApiResponse(code = 403, response = ErrorReport.class, message="Dataset quota has been exceeded"),
            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response mergeDatasets(
            @FormParam("dataset_uris") String datasetURIs,
            @HeaderParam("subjectid") String subjectId) throws URISyntaxException, QuotaExceededException {

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long datasetCount = datasetHandler.countAllOfCreator(user.getId());
        int maxAllowedDatasets = new UserFacade(user).getMaxDatasets();

        if (datasetCount > maxAllowedDatasets) {
            LOG.info(String.format("User %s has %d datasets while maximum is %d",
                    user.getId(), datasetCount, maxAllowedDatasets));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + datasetCount + " datasets. "
                    + "No more than " + maxAllowedDatasets + " are allowed with your subscription.");
        }

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
        dataset.setVisible(true);
        if (dataset.getMeta() == null) {
            dataset.setMeta(new MetaInfo());
        }
        dataset.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));

        datasetHandler.create(dataset);

        return Response.created(new URI(dataset.getId())).entity(dataset).build();

    }

    @DELETE
    @Path("{id}")
    @Authorize
    @ApiOperation("Deletes dataset")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Dataset was succesfully deleted"),
            @ApiResponse(code = 404, response = ErrorReport.class, message = "Dataset was not found in the system"),
            @ApiResponse(code = 403, response = ErrorReport.class, message="Dataset quota has been exceeded"),
            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response deleteDataset(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @PathParam("id") String id) throws JaqpotForbiddenException {
        Dataset ds = datasetHandler.find(id);
        if (ds == null) {
            throw new NotFoundException("Dataset with id:" + id + " was not found on the server.");
        }
        MetaInfo metaInfo = ds.getMeta();
        if (metaInfo.getLocked())
            throw new JaqpotForbiddenException("You cannot delete a Dataset that is locked.");

        String userName = securityContext.getUserPrincipal().getName();
        if (!ds.getMeta().getCreators().contains(userName)) {
            return Response.status(Response.Status.FORBIDDEN).entity("You cannot delete a Dataset that was not created by you.").build();
        }
        datasetHandler.remove(ds);
        return Response.ok().build();
    }

    @POST
    @Path("{id}/qprf")
    @Authorize
    @ApiOperation("Creates QPRF Report")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Dataset was succesfully deleted"),
            @ApiResponse(code = 400, response = ErrorReport.class, message = "Bad Request. More details can be found in details of ErrorReport"),
            @ApiResponse(code = 404, response = ErrorReport.class, message = "Dataset was not found in the system"),
            @ApiResponse(code = 403, response = ErrorReport.class, message="Dataset quota has been exceeded"),
            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })

    public Response createQPRFReport(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @PathParam("id") String id,
            @FormParam("substance_uri") String substanceURI,
            @FormParam("title") String title,
            @FormParam("description") String description
    ) throws QuotaExceededException, ExecutionException, InterruptedException {

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long reportCount = reportHandler.countAllOfCreator(user.getId());
        int maxAllowedReports = new UserFacade(user).getMaxReports();

        if (reportCount > maxAllowedReports) {
            LOG.info(String.format("User %s has %d algorithms while maximum is %d",
                    user.getId(), reportCount, maxAllowedReports));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + reportCount + " reports. "
                    + "No more than " + maxAllowedReports + " are allowed with your subscription.");
        }

        Dataset ds = datasetHandler.find(id);
        if (ds == null) {
            throw new NotFoundException("Dataset with id:" + id + " was not found on the server.");
        }
        if (ds.getByModel() == null || ds.getByModel().isEmpty()) {
            throw new BadRequestException("Selected dataset was not produced by a valid model.");
        }
        Model model = modelHandler.find(ds.getByModel());
        if (model == null) {
            throw new BadRequestException("Selected dataset was not produced by a valid model.");
        }
        String datasetURI = model.getDatasetUri();
        if (datasetURI == null || datasetURI.isEmpty()) {
            throw new BadRequestException("The model that created this dataset does not point to a valid training dataset.");
        }
        Dataset trainingDS = client.target(datasetURI)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("subjectid", subjectId)
                .get(Dataset.class);
        if (trainingDS == null) {
            throw new BadRequestException("The model that created this dataset does not point to a valid training dataset.");
        }

        if (model.getTransformationModels() != null) {
            for (String transModelURI : model.getTransformationModels()) {
                Model transModel = modelHandler.find(transModelURI.split("model/")[1]);
                if (transModel == null) {
                    throw new NotFoundException("Transformation model with id:" + transModelURI + " was not found.");
                }
                try {
                    trainingDS = jpdiClient.predict(trainingDS, transModel, trainingDS.getMeta(), UUID.randomUUID().toString()).get();
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, "JPDI Training procedure interupted", ex);
                    throw new InternalServerErrorException("JPDI Training procedure interupted", ex);
                } catch (ExecutionException ex) {
                    LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
                    throw new InternalServerErrorException("JPDI Training procedure error", ex.getCause());
                } catch (CancellationException ex) {
                    throw new InternalServerErrorException("Procedure was cancelled");
                }
            }
        }

        List<String> retainableFeatures = new ArrayList<>(model.getIndependentFeatures());
        retainableFeatures.addAll(model.getDependentFeatures());

        trainingDS.getDataEntry().parallelStream()
                .forEach(dataEntry -> {
                    dataEntry.getValues().keySet().retainAll(retainableFeatures);
                });

        DataEntry dataEntry = ds.getDataEntry().stream()
                .filter(de -> de.getCompound().getURI().equals(substanceURI))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(""));

        trainingDS.getDataEntry().add(dataEntry);
        trainingDS.getMeta().setCreators(new HashSet<>(Arrays.asList(user.getId())));

        Map<String, Object> parameters = new HashMap<>();

        UrlValidator urlValidator = new UrlValidator();
        if (urlValidator.isValid(substanceURI)) {

            String substanceId = substanceURI.split("substance/")[1];

            Dataset structures = ambitClient.getDatasetStructures(substanceId,subjectId);

            List<Map<String, String>> structuresList = structures.getDataEntry()
                    .stream()
                    .map(de -> {
                        String compound = de.getCompound().getURI();
                        String casrn = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23CASRNDefault")).orElse("").toString();
                        String einecs = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23EINECSDefault")).orElse("").toString();
                        String iuclid5 = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23IUCLID5_UUIDDefault")).orElse("").toString();
                        String inchi = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23InChI_stdDefault")).orElse("").toString();
                        String reach = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23REACHRegistrationDateDefault")).orElse("").toString();
                        String iupac = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23IUPACNameDefault")).orElse("").toString();

                        Map<String, String> structuresMap = new HashMap<>();
                        structuresMap.put("Compound", compound);
                        structuresMap.put("CasRN", casrn);
                        structuresMap.put("EC number", einecs);
                        structuresMap.put("REACH registration date", reach);
                        structuresMap.put("IUCLID 5 Reference substance UUID", iuclid5);
                        structuresMap.put("Std. InChI", inchi);
                        structuresMap.put("IUPAC name", iupac);

                        return structuresMap;
                    })
                    .collect(Collectors.toList());
            parameters.put("structures", structuresList);
        } else {
            List<Map<String, String>> structuresList = new ArrayList<>();
            Map<String, String> structuresMap = new HashMap<>();
            structuresMap.put("Compound", "");
            structuresMap.put("CasRN", "");
            structuresMap.put("EC number", "");
            structuresMap.put("REACH registration date", "");
            structuresMap.put("IUCLID 5 Reference substance UUID", "");
            structuresMap.put("Std. InChI", "");
            structuresMap.put("IUPAC name", "");
            structuresList.add(structuresMap);
            parameters.put("structures", structuresList);
        }

        parameters.put("predictedFeature",
                model
                .getPredictedFeatures()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Model does not have a valid predicted feature")));

        parameters.put("algorithm", algorithmHandler.find(model.getAlgorithm().getId()));
        parameters.put("substanceURI", substanceURI);
        if (model.getLinkedModels() != null && !model.getLinkedModels().isEmpty()) {
            Model doa = modelHandler.find(model.getLinkedModels().get(0).split("model/")[1]);
            if (doa != null) {
                parameters.put("doaURI", doa.getPredictedFeatures().get(0));
                parameters.put("doaMethod", doa.getAlgorithm().getId());
            }
        }
        TrainingRequest request = new TrainingRequest();

        request.setDataset(trainingDS);
        request.setParameters(parameters);
        request.setPredictionFeature(model.getDependentFeatures()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Model does not have a valid prediction feature")));

        Report report = client.target("http://147.102.82.32:8094/pws/qprf")
                .request()
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(request), Report.class);

        report.setMeta(MetaInfoBuilder.builder()
                .addTitles(title)
                .addDescriptions(description)
                .addCreators(securityContext.getUserPrincipal().getName())
                .build()
        );
        report.setId(new ROG(true).nextString(15));
        report.setVisible(Boolean.TRUE);
        reportHandler.create(report);

        return Response.ok(report).build();
    }

    @POST
    @Path("{id}/qprf-dummy")
    @Authorize
    @ApiOperation("Creates QPRF Dummy Report")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Dataset was succesfully deleted"),
            @ApiResponse(code = 400, response = ErrorReport.class, message = "Bad Request. More details can be found in details of ErrorReport"),
            @ApiResponse(code = 404, response = ErrorReport.class, message = "Dataset was not found in the system"),
            @ApiResponse(code = 403, response = ErrorReport.class, message="Dataset quota has been exceeded"),
            @ApiResponse(code = 401, response = ErrorReport.class, message = "You are not authorized to access this resource"),
            @ApiResponse(code = 403, response = ErrorReport.class, message = "This request is forbidden (e.g., no authentication token is provided)"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response createQPRFReportDummy(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @PathParam("id") String id,
            @FormParam("substance_uri") String substanceURI,
            @FormParam("title") String title,
            @FormParam("description") String description
    ) {

        Dataset ds = datasetHandler.find(id);
        if (ds == null) {
            throw new NotFoundException("Dataset with id:" + id + " was not found on the server.");
        }
        if (ds.getByModel() == null || ds.getByModel().isEmpty()) {
            throw new BadRequestException("Selected dataset was not produced by a valid model.");
        }
        Model model = modelHandler.find(ds.getByModel());
        if (model == null) {
            throw new BadRequestException("Selected dataset was not produced by a valid model.");
        }
        String datasetURI = model.getDatasetUri();
        if (datasetURI == null || datasetURI.isEmpty()) {
            throw new BadRequestException("The model that created this dataset does not point to a valid training dataset.");
        }
        Dataset trainingDS = client.target(datasetURI)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("subjectid", subjectId)
                .get(Dataset.class);
        if (trainingDS == null) {
            throw new BadRequestException("The model that created this dataset does not point to a valid training dataset.");
        }

        if (model.getTransformationModels() != null) {
            for (String transModelURI : model.getTransformationModels()) {
                Model transModel = modelHandler.find(transModelURI.split("model/")[1]);
                if (transModel == null) {
                    throw new NotFoundException("Transformation model with id:" + transModelURI + " was not found.");
                }
                try {
                    trainingDS = jpdiClient.predict(trainingDS, transModel, trainingDS.getMeta(), UUID.randomUUID().toString()).get();
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, "JPDI Training procedure interupted", ex);
                    throw new InternalServerErrorException("JPDI Training procedure interupted", ex);
                } catch (ExecutionException ex) {
                    LOG.log(Level.SEVERE, "Training procedure execution error", ex.getCause());
                    throw new InternalServerErrorException("JPDI Training procedure error", ex.getCause());
                } catch (CancellationException ex) {
                    throw new InternalServerErrorException("Procedure was cancelled");
                }
            }
        }

        List<String> retainableFeatures = new ArrayList<>(model.getIndependentFeatures());
        retainableFeatures.addAll(model.getDependentFeatures());

        trainingDS.getDataEntry().parallelStream()
                .forEach(dataEntry -> {
                    dataEntry.getValues().keySet().retainAll(retainableFeatures);
                });

        DataEntry dataEntry = ds.getDataEntry().stream()
                .filter(de -> de.getCompound().getURI().equals(substanceURI))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(""));

        trainingDS.getDataEntry().add(dataEntry);

        Map<String, Object> parameters = new HashMap<>();

        UrlValidator urlValidator = new UrlValidator();
        if (urlValidator.isValid(substanceURI)) {
            Dataset structures = client.target(substanceURI + "/structures")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("subjectid", subjectId)
                    .get(Dataset.class);
            List<Map<String, String>> structuresList = structures.getDataEntry()
                    .stream()
                    .map(de -> {
                        String compound = de.getCompound().getURI();
                        String casrn = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23CASRNDefault")).orElse("").toString();
                        String einecs = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23EINECSDefault")).orElse("").toString();
                        String iuclid5 = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23IUCLID5_UUIDDefault")).orElse("").toString();
                        String inchi = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23InChI_stdDefault")).orElse("").toString();
                        String reach = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23REACHRegistrationDateDefault")).orElse("").toString();
                        String iupac = Optional.ofNullable(de.getValues().get("https://apps.ideaconsult.net/enmtest/feature/http%3A%2F%2Fwww.opentox.org%2Fapi%2F1.1%23IUPACNameDefault")).orElse("").toString();

                        Map<String, String> structuresMap = new HashMap<>();
                        structuresMap.put("Compound", compound);
                        structuresMap.put("CasRN", casrn);
                        structuresMap.put("EC number", einecs);
                        structuresMap.put("REACH registration date", reach);
                        structuresMap.put("IUCLID 5 Reference substance UUID", iuclid5);
                        structuresMap.put("Std. InChI", inchi);
                        structuresMap.put("IUPAC name", iupac);

                        return structuresMap;
                    })
                    .collect(Collectors.toList());
            parameters.put("structures", structuresList);
        } else {
            List<Map<String, String>> structuresList = new ArrayList<>();
            Map<String, String> structuresMap = new HashMap<>();
            structuresMap.put("Compound", "");
            structuresMap.put("CasRN", "");
            structuresMap.put("EC number", "");
            structuresMap.put("REACH registration date", "");
            structuresMap.put("IUCLID 5 Reference substance UUID", "");
            structuresMap.put("Std. InChI", "");
            structuresMap.put("IUPAC name", "");
            structuresList.add(structuresMap);
            parameters.put("structures", structuresList);
        }

        parameters.put("predictedFeature",
                model
                .getPredictedFeatures()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Model does not have a valid predicted feature")));

        parameters.put("algorithm", algorithmHandler.find(model.getAlgorithm().getId()));
        parameters.put("substanceURI", substanceURI);
        if (model.getLinkedModels() != null && !model.getLinkedModels().isEmpty()) {
            Model doa = modelHandler.find(model.getLinkedModels().get(0).split("model/")[1]);
            if (doa != null) {
                parameters.put("doaURI", doa.getPredictedFeatures().get(0));
                parameters.put("doaMethod", doa.getAlgorithm().getId());
            }
        }
        TrainingRequest request = new TrainingRequest();

        request.setDataset(trainingDS);
        request.setParameters(parameters);
        request.setPredictionFeature(model.getDependentFeatures()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Model does not have a valid prediction feature")));

        return Response.ok(request).build();
//        Report report = client.target("http://147.102.82.32:8094/pws/qprf")
//                .request()
//                .header("Content-Type", MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .post(Entity.json(request), Report.class);
//
//        report.setMeta(MetaInfoBuilder.builder()
//                .addTitles(title)
//                .addDescriptions(description)
//                .addCreators(securityContext.getUserPrincipal().getName())
//                .build()
//        );
//        report.setId(new ROG(true).nextString(15));
//        report.setVisible(Boolean.TRUE);
//        reportHandler.create(report);
//
//        return Response.ok(report).build();
    }
}
