/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.ReportHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Report;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.jpdi.TrainingRequest;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import xyz.euclia.euclia.accounts.client.models.User;

/**
 *
 * @author hampos
 */
@Path("/readacross")
//@Api(value = "/readacross", description = "Read Across API")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "readacross")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class ReadAcrossResource {

    private static final Logger LOG = Logger.getLogger(ReadAcrossResource.class.getName());

    @Inject
    @UnSecure
    Client client;

    @Inject
    @Jackson
    JSONSerializer jsonSerializer;

    @EJB
    ReportHandler reportHandler;

    @EJB
    UserHandler userHandler;

    @Context
    SecurityContext securityContext;

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})    
    @Operation(summary = "Creates Read Across Report",
               description = "Creates Read Across Report",
               responses = {
                   @ApiResponse(content = @Content(schema = @Schema(implementation = Report.class)))
               })
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    public Response readAcrossReport(
            @Parameter(name = "title", description = "title", required = true, schema = @Schema(implementation = String.class)) @FormParam("title") String title,
            @Parameter(name = "descriptions", description = "descriptions", required = true, schema = @Schema(implementation = String.class)) @FormParam("descriptions") String description,
            @Parameter(name = "dataset_uri", description = "dataset_uri", schema = @Schema(implementation = String.class)) @FormParam("dataset_uri") String datasetURI,
            @Parameter(name = "prediction_feature", description = "prediction_feature", schema = @Schema(type = "string")) @FormParam("prediction_feature") String predictionFeature,
            @Parameter(name = "parameters", description = "parameters", schema = @Schema(implementation = String.class)) @FormParam("parameters") String parameters,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key
    ) throws QuotaExceededException, JaqpotDocumentSizeExceededException  {

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        User user = userHandler.find(securityContext.getUserPrincipal().getName(), apiKey);

        Dataset dataset = client.target(datasetURI)
                .request()
                .header("Authorization", "Bearer " + apiKey)
                .accept(MediaType.APPLICATION_JSON)
                .get(Dataset.class);
        dataset.setDatasetURI(datasetURI);

        TrainingRequest trainingRequest = new TrainingRequest();
        trainingRequest.setDataset(dataset);
        trainingRequest.setPredictionFeature(predictionFeature);

        if (parameters != null && !parameters.isEmpty()) {
            HashMap<String, Object> parameterMap = jsonSerializer.parse(parameters, new HashMap<String, Object>().getClass());
            trainingRequest.setParameters(parameterMap);
        }

        Report report = client.target("http://jaqpot.org:8093/pws/readacross")
                .request()
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(trainingRequest), Report.class);

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
}
