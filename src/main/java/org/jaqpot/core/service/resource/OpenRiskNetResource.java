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

//import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.data.wrappers.DatasetLegacyWrapper;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.FeatureBuilder;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.EntryId;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.CalculationService;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;
import org.jaqpot.core.service.exceptions.parameter.*;
import org.jaqpot.core.service.validator.ParameterValidator;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;

import static org.jaqpot.core.util.CSVUtils.parseLine;

@Path("/openrisknet")
@Tag(name = "openrisknet")
//@Api(value = "/openrisknet", description = "OpenRiskNet API")
public class OpenRiskNetResource {

    private static final Logger LOG = Logger.getLogger(EnanomapperResource.class.getName());

    @EJB
    CalculationService smilesService;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    DatasetLegacyWrapper datasetLegacyWrapper;

    @EJB
    UserHandler userHandler;

    @Context
    SecurityContext securityContext;

    @Inject
    ParameterValidator parameterValidator;

    @Inject
    @Jackson
    JSONSerializer serializer;

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

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/upload")
    @Consumes("multipart/form-data")
    //@ApiImplicitParams({
    //        @ApiImplicitParam(name = "smilesFile", value = "xls[m,x] file", required = true, dataType = "file", paramType = "formData"),
    //        @ApiImplicitParam(name = "title", value = "Title of dataset", required = true, dataType = "string", paramType = "formData"),
    //        @ApiImplicitParam(name = "description", value = "Description of dataset", required = true, dataType = "string", paramType = "formData"),
    //        @ApiImplicitParam(name = "algorithm-uri", value = "Algorithm URI", required = true, dataType = "string", paramType = "formData"),
    //        @ApiImplicitParam(name = "parameters", value = "Parameters for algorithm", required = false, dataType = "string", paramType = "formData")

    //})
    @RequestBody(content = {
        @Content(mediaType = "multipart/form-data", schema = @Schema(type = "string", format = "binary")),
        @Content(mediaType = "text/plain", schema = @Schema(type = "string"))})
    @Parameters({
        @Parameter(name = "smilesFile", required = true, schema = @Schema( type = "string", format = "binary"), description = "xls[m,x] file", in = ParameterIn.QUERY),
        @Parameter(name = "title", required = true, description = "Title of dataset", schema = @Schema(type = "string"), in = ParameterIn.QUERY),
        @Parameter(name = "description", description = "Description of dataset", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
        @Parameter(name = "algorithm-uri", description = "Algorithm URI", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
        @Parameter(name = "parameters", description = "Parameters for algorithm", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY)
    })
    @Operation(summary = "Creates Dataset By SMILES document"
            )
    //@ApiOperation(value = "Creates Dataset By SMILES document",
    //        notes = "Calculates descriptors from SMILES document, returns Dataset",
    //        response = Task.class
    //)
    @org.jaqpot.core.service.annotations.Task
    public Response uploadFile(
            @HeaderParam("Authorization") String api_key,
            //@ApiParam(value = "multipartFormData input", hidden = true) MultipartFormDataInput input)
            //@Parameter(hidden = true, schema = @Schema(name = "multipartFormData", description = "multipartFormData input"))
            MultipartFormDataInput input)
            throws ParameterIsNullException, ParameterInvalidURIException, QuotaExceededException, IOException, ParameterScopeException, ParameterRangeException, ParameterTypeException, JaqpotDocumentSizeExceededException {
        UrlValidator urlValidator = new UrlValidator();
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
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

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("smilesFile");
        String filename = getFileName(inputParts.get(0).getHeaders());

        byte[] bytes = new byte[0];
        bytes = getBytesFromInputParts(inputParts);

        String title = uploadForm.get("title").get(0).getBody(String.class, null);
        String description = uploadForm.get("description").get(0).getBody(String.class, null);
        String algorithmURI = uploadForm.get("algorithm-uri").get(0).getBody(String.class, null);

        String parameters = null;
        if (uploadForm.get("parameters") != null) {
            parameters = uploadForm.get("parameters").get(0).getBody(String.class, null);
        }

        if (algorithmURI == null) {
            throw new ParameterIsNullException("algorithmURI");
        }

        if (!urlValidator.isValid(algorithmURI)) {
            throw new ParameterInvalidURIException("Not valid Algorithm URI.");
        }
        String algorithmId = algorithmURI.split("algorithm/")[1];

        Algorithm algorithm = algorithmHandler.find(algorithmId);
        if (algorithm == null) {
            throw new NotFoundException("Could not find Algorithm with id:" + algorithmId);
        }

        parameterValidator.validate(parameters, algorithm.getParameters());

        Map<String, Object> options = new HashMap<>();
        options.put("title", title);
        options.put("description", description);
        options.put("api_key", apiKey);
        options.put("file", bytes);
        options.put("filename", filename);
        options.put("mode", "PREPARATION");
        options.put("parameters", parameters);
        options.put("algorithmId", algorithmId);

        Task task = smilesService.initiatePreparation(options, securityContext.getUserPrincipal().getName());

        return Response.ok(task).build();

    }

    private static byte[] getBytesFromInputParts(List<InputPart> inputParts) {
        for (InputPart inputPart : inputParts) {
            try {
                //convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                return getBytesFromInputStream(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len; (len = is.read(buffer)) != -1;) {
            os.write(buffer, 0, len);
        }
        os.flush();
        return os.toByteArray();
    }

}
