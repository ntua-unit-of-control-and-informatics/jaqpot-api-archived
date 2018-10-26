package org.jaqpot.core.service.resource;

import io.swagger.annotations.*;
import org.apache.commons.validator.routines.UrlValidator;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.DescriptorHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.*;
import org.jaqpot.core.model.builder.DescriptorBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.descriptor.DescriptorReqDTO;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.data.DescriptorService;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;
import org.jaqpot.core.service.exceptions.QuotaExceededException;
import org.jaqpot.core.service.exceptions.parameter.*;
import org.jaqpot.core.service.validator.ParameterValidator;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.logging.Logger;

@Path("descriptor")
@Api(value = "/descriptor", description = "Descriptors API")
@Produces({"application/json", "text/uri-list"})
public class DescriptorResource {

    @EJB
    DescriptorService descriptorService;

    @EJB
    DescriptorHandler descriptorHandler;

    @Context
    SecurityContext securityContext;

    @EJB
    UserHandler userHandler;

    @EJB
    DatasetHandler datasetHandler;

    @Inject
    @Jackson
    JSONSerializer serializer;


    @Inject
    ParameterValidator parameterValidator;

    @Context
    UriInfo uriInfo;

    private static final Logger LOG = Logger.getLogger(DescriptorResource.class.getName());

    private static final String DEFAULT_DESCRIPTOR = "{\n"
            + "  \"descriptorService\":\"http://z.ch/t/a\",\n"
            + "  \"parameters\": [\n"
            + "    {\n"
            + "       \"name\":\"alpha\",\n"
            + "       \"scope\":\"OPTIONAL\",\n"
            + "       \"value\":101.635\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(
            value = "Finds all Descriptors",
            notes = "Finds all Descriptors JaqpotQuattro supports",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "orn-@type", value = "x-orn:Descriptor"),
                    }
                    ),
                    @Extension(name = "orn:returns",properties={
                            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:DescriptorList")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 401, response=  ErrorReport.class , message = "Wrong, missing or insufficient credentials. Error report is produced."),
            @ApiResponse(code = 200,  response = Descriptor.class, responseContainer = "List" , message = "A list of descriptors in the Jaqpot framework"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")

    })
    public Response getDescriptors(
            @ApiParam(value = "Authorization token") @HeaderParam("apiKey") String apiKey,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max", defaultValue = "10") @QueryParam("max") Integer max) {
        return Response
                .ok(descriptorHandler.findAll(start != null ? start : 0, max != null ? max : Integer.MAX_VALUE))
                .header("total", descriptorHandler.countAll())
                .build();
    }

    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(
            value = "Creates Desciptor",
            notes = "Registers a new JPDI-compliant descriptor service.",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "orn-@type", value = "x-orn:Descriptor"),
                    }
                    ),
                    @Extension(name = "orn:expects",properties={
                            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Descriptor")
                    }),
                    @Extension(name = "orn:returns",properties={
                            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Status")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 403, response = ErrorReport.class, message="Descriptor quota has been exceeded"),
            @ApiResponse(code = 401, response = ErrorReport.class , message = "Wrong, missing or insufficient credentials. Error report is produced."),
            @ApiResponse(code = 200,  response = Descriptor.class, message = "Descriptor successfully registered in the system"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")

    })
    public Response createDescriptor(
            @ApiParam(value = "Descriptor in JSON", defaultValue = DEFAULT_DESCRIPTOR, required = true) Descriptor descriptor,
            @ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @ApiParam(value = "Title of your descriptor") @HeaderParam("title") String title,
            @ApiParam(value = "Short description of your descriptor") @HeaderParam("description") String description,
            @ApiParam(value = "Tags for your descriptor (in a comma separated list) to facilitate look-up") @HeaderParam("tags") String tags
    ) throws JaqpotDocumentSizeExceededException {
        if (descriptor.getId() == null) {
            ROG rog = new ROG(true);
            descriptor.setId(rog.nextString(14));
        }

        DescriptorBuilder descriptorBuilder = DescriptorBuilder.builder(descriptor);
        if (title != null)
            descriptorBuilder.addTitles(title);
        if (description != null)
            descriptorBuilder.addDescriptions(description);
        if (tags != null)
            descriptorBuilder.addTagsCSV(tags);

        descriptor = descriptorBuilder.build();
        if (descriptor.getMeta() == null)
            descriptor.setMeta(new MetaInfo());

        descriptor.getMeta().setCreators(new HashSet<>(Arrays.asList(securityContext.getUserPrincipal().getName())));
        descriptorHandler.create(descriptor);
        return Response
                .status(Response.Status.OK)
                .header("Location", uriInfo.getBaseUri().toString() + "descriptor/" + descriptor.getId())
                .entity(descriptor).build();
    }

    @GET
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list", "application/ld+json"})
    @ApiOperation(value = "Finds Descriptor",
            notes = "Finds Descriptor with provided id",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "orn-@type", value = "x-orn:Descriptor"),
                    }
                    ),
                    @Extension(name = "orn:expects",properties={
                            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:DescriptorId")
                    }),
                    @Extension(name = "orn:returns",properties={
                            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Descriptor")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 401, response=  ErrorReport.class , message = "Wrong, missing or insufficient credentials. Error report is produced."),
            @ApiResponse(code = 404, response = ErrorReport.class , message = "Descriptor was not found"),
            @ApiResponse(code = 200,  response = Descriptor.class, message = "Descriptor was found in the system"),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
    })
    public Response getDescriptor(
            @ApiParam(value = "Authorization token")  @HeaderParam("Authorization") String api_key,
            @PathParam("id") String descriptorId) throws ParameterIsNullException {
        if (descriptorId == null) {
            throw new ParameterIsNullException("descriptorId");
        }

        Descriptor descriptor = descriptorHandler.find(descriptorId);
        if (descriptor == null) {
            throw new NotFoundException("Could not find Descriptor with id:" + descriptorId);
        }
        return Response.ok(descriptor).build();
    }


    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/{id}")
    @ApiOperation(value = "Apply Descriptor",
            notes = "Applies Descriptor on Dataset and creates a new Dataset."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, response=  ErrorReport.class , message = "Bad request. More info can be found in details of Error Report."),
            @ApiResponse(code = 401, response=  ErrorReport.class , message = "Wrong, missing or insufficient credentials. Error report is produced."),
            @ApiResponse(code = 404, response = ErrorReport.class , message = "Descriptor was not found."),
            @ApiResponse(code = 200,  response = Task.class, message = "The process has successfully been started. A task URI is returned."),
            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")

    })
    @org.jaqpot.core.service.annotations.Task
    public Response applydescriptor(
            DescriptorReqDTO descriptorReqDTO,
            @PathParam("id")  String descriptorId,
            @HeaderParam("Authorization") String api_key) throws QuotaExceededException, ParameterIsNullException, ParameterInvalidURIException, ParameterTypeException, ParameterRangeException, ParameterScopeException, JaqpotDocumentSizeExceededException  {
        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        Descriptor descriptor = descriptorHandler.find(descriptorId);
        if (descriptor == null) {
            throw new NotFoundException("Could not find Descriptor with id:" + descriptorId);
        }

        if (descriptorReqDTO.getDatasetURI() == null) {
            throw new ParameterIsNullException("datasetURI");
        }

        if (!urlValidator.isValid(descriptorReqDTO.getDatasetURI())) {
            throw new ParameterInvalidURIException("Not valid Dataset URI.");
        }

        String datasetId = descriptorReqDTO.getDatasetURI().split("dataset/")[1];
        Dataset datasetMeta = datasetHandler.findMeta(datasetId);

        if (datasetMeta.getTotalRows() != null && datasetMeta.getTotalRows() < 1) {
            throw new BadRequestException("Cannot apply descriptor on dataset with no rows.");
        }

        if (descriptorReqDTO.getTitle() == null) {
            throw new ParameterIsNullException("title");
        }
        if (descriptorReqDTO.getDescription() == null) {
            throw new ParameterIsNullException("description");
        }

        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long datasetCount = datasetHandler.countAllOfCreator(user.getId());
        int maxAllowedDatasets= new UserFacade(user).getMaxDatasets();

        if (datasetCount > maxAllowedDatasets) {
            LOG.info(String.format("User %s has %d datasets while maximum is %d",
                    user.getId(), datasetCount, maxAllowedDatasets));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + datasetCount + " datasets. "
                    + "No more than " + maxAllowedDatasets + " are allowed with your subscription.");
        }

        Map<String, Object> options = new HashMap<>();
        options.put("title", descriptorReqDTO.getTitle());
        options.put("description", descriptorReqDTO.getDescription());
        options.put("datasetURI", descriptorReqDTO.getDatasetURI());
        options.put("featureURIs",serializer.write(descriptorReqDTO.getFeatureURIs()));
        options.put("api_key", apiKey);
        options.put("descriptorId", descriptorId);
        options.put("parameters", descriptorReqDTO.getParameters());
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("creator", securityContext.getUserPrincipal().getName());

        parameterValidator.validate(descriptorReqDTO.getParameters(), descriptor.getParameters());

        Task task = descriptorService.initiateDescriptor(options, securityContext.getUserPrincipal().getName());

        return Response.ok(task).build();
    }
}