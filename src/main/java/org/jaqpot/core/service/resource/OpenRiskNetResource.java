package org.jaqpot.core.service.resource;
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

import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.client.ambit.Ambit;
import org.jaqpot.core.service.data.PredictionService;
import org.jaqpot.core.service.data.CalculationService;
import org.jaqpot.core.service.data.TrainingService;
import org.jaqpot.core.service.exceptions.QuotaExceededException;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@Path("openrisknet")
@Api(value = "/openrisknet", description = "OpenRiskNet API")
@Authorize
public class OpenRiskNetResource {

    private static final Logger LOG = Logger.getLogger(EnanomapperResource.class.getName());
    public static final String UPLOADED_FILE_PARAMETER_NAME = "file";

    @EJB
    CalculationService smilesService;

    @EJB
    TrainingService trainingService;

    @EJB
    PredictionService predictionService;

    @EJB
    ModelHandler modelHandler;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    UserHandler userHandler;

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    @Inject
    @UnSecure
    Client client;

    @Inject
    Ambit ambitClient;

    @Inject
    @Jackson
    JSONSerializer serializer;

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Path("/dataset")
    @ApiOperation(value = "Creates Dataset By SMILES document",
            notes = "Calculates descriptors from SMILES document, returns Dataset",
            response = Task.class
    )

    @org.jaqpot.core.service.annotations.Task
    @ApiImplicitParams(
            @ApiImplicitParam(dataType = "file", name = "file", value = "File to be uploaded", paramType = "formData")
    )
    public Response createDatasetBySmilesDocument(
            @HeaderParam("subjectid") String subjectId,
            MultipartFormDataInput file,
            @FormParam("title") String title,
            @FormParam("description") String description

    ) throws QuotaExceededException {
        Task task = null;
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

        Map<String, List<InputPart>> uploadForm = file.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get(UPLOADED_FILE_PARAMETER_NAME);

        for (InputPart inputPart : inputParts) {
            MultivaluedMap<String, String> headers = inputPart.getHeaders();
            try {
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                byte[] bytes = IOUtils.toByteArray(inputStream);
                String filename = getFileName(headers);
                System.out.println(filename);
                String str = new String(bytes);
                System.out.println(str);

                Map<String, Object> options = new HashMap<>();
                options.put("title", title);
                options.put("description", description);
                options.put("subjectid", subjectId);
                options.put("file", bytes);
                options.put("mode", "PREPARATION");
                //task = smilesService.initiatePreparation(options, securityContext.getUserPrincipal().getName());

            } catch (IOException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
        }
        return Response.ok(task).build();

    }
    private String getFileName(MultivaluedMap<String, String> headers) {
        String[] contentDisposition = headers.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = sanitizeFilename(name[1]);
                return finalFileName;
            }
        }
        return "unknown";
    }

    private String sanitizeFilename(String s) {
        return s.trim().replaceAll("\"", "");
    }
}


      /*  Map<String, Object> options = new HashMap<>();
        options.put("bundle_uri", bundleURI);
        options.put("title", datasetData.getTitle());
        options.put("description", datasetData.getDescription());
        options.put("descriptors", descriptorsString);
        options.put("intersect_columns", datasetData.getIntersectColumns() != null ? datasetData.getIntersectColumns() : true);
        options.put("subjectid", subjectId);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("mode", "PREPARATION");
        options.put("retain_null_values", datasetData.getRetainNullValues() != null ? datasetData.getRetainNullValues() : false);
        return Response.ok().build();//task
    }



*/
