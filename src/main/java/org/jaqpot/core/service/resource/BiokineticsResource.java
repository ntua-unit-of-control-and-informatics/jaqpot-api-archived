package org.jaqpot.core.service.resource;

import io.swagger.annotations.*;
import org.apache.commons.validator.routines.UrlValidator;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.facades.UserFacade;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.data.AAService;
import org.jaqpot.core.service.data.TrainingService;
import org.jaqpot.core.service.exceptions.QuotaExceededException;
import org.jaqpot.core.service.exceptions.parameter.*;
import org.jaqpot.core.service.validator.ParameterValidator;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Base64;
/**
 * Created by Angelos Valsamis on 23/10/2017.
 */
@Path("biokinetics")
@Api(value = "/biokinetics", description = "Biokinetics API")
@Produces({"application/json", "text/uri-list"})
@Authorize

public class BiokineticsResource {
    @EJB
    AAService aaService;

    @Context
    SecurityContext securityContext;

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    UserHandler userHandler;

    @EJB
    ModelHandler modelHandler;

    @Inject
    ParameterValidator parameterValidator;

    @EJB
    TrainingService trainingService;

    @Context
    UriInfo uriInfo;


    @Inject
    @Jackson
    JSONSerializer serializer;

    private static final Logger LOG = Logger.getLogger(BiokineticsResource.class.getName());

    @POST
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Path("/train")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "xml[m,x] file", required = true, dataType = "file", paramType = "formData"),
            @ApiImplicitParam(name = "dataset-uri", value = "Dataset uri to be trained upon", required =true, dataType = "string", paramType = "formData"),
            @ApiImplicitParam(name = "title", value = "Title of model", required = true, dataType = "string", paramType = "formData"),
            @ApiImplicitParam(name = "description", value = "Description of model", required = true, dataType = "string", paramType = "formData"),
            @ApiImplicitParam(name = "algorithm-uri", value = "Algorithm URI", required = true, dataType = "string", paramType = "formData"),
            @ApiImplicitParam(name = "parameters", value = "Parameters for algorithm", required = false, dataType = "string", paramType = "formData")

    })
    @ApiOperation(value = "Creates Biokinetics model",
            notes = "Trains biokinetics model given a .xml file and demographic data",
            response = Task.class
    )
    @org.jaqpot.core.service.annotations.Task
    public Response trainBiokineticsModel(
            @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "multipartFormData input", hidden = true) MultipartFormDataInput input)
            throws ParameterIsNullException, ParameterInvalidURIException, QuotaExceededException, IOException, ParameterScopeException, ParameterRangeException, ParameterTypeException {

        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

        byte[] bytes;
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");
        String title = uploadForm.get("title").get(0).getBody(String.class, null);
        String description = uploadForm.get("description").get(0).getBody(String.class, null);
        String algorithmURI = uploadForm.get("algorithm-uri").get(0).getBody(String.class, null);
        String datasetUri = uploadForm.get("dataset-uri").get(0).getBody(String.class, null);
        
        User user = userHandler.find(securityContext.getUserPrincipal().getName());
        long modelCount = modelHandler.countAllOfCreator(user.getId());
        int maxAllowedModels = new UserFacade(user).getMaxModels();

        if (modelCount > maxAllowedModels) {
            LOG.info(String.format("User %s has %d model while maximum is %d",
                    user.getId(), modelCount, maxAllowedModels));
            throw new QuotaExceededException("Dear " + user.getId()
                    + ", your quota has been exceeded; you already have " + modelCount + " models. "
                    + "No more than " + maxAllowedModels + " are allowed with your subscription.");
        }

        String parameters =null;
        if(uploadForm.get("parameters")!=null)
            parameters = uploadForm.get("parameters").get(0).getBody(String.class, null);

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

        String encodedString="";
        for (InputPart inputPart : inputParts) {
            try {
                //Convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                bytes = getBytesFromInputStream(inputStream);

                //Base64 encode
                byte[] encoded = java.util.Base64.getEncoder().encode(bytes);
                encodedString = new String(encoded);
                System.out.println(encodedString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Append XML file (Base64 string) in parameters
        parameters  = parameters.substring(0,parameters.length() - 1);
        parameters +=",\"xml_file\":[\""+encodedString+"\"]}";

        Map<String, Object> options = new HashMap<>();
        options.put("title", title);
        options.put("description", description);
        options.put("subjectid", subjectId);
        options.put("parameters", parameters);
        options.put("base_uri", uriInfo.getBaseUri().toString());
        options.put("dataset_uri", datasetUri);
        options.put("algorithmId", algorithmId);
        options.put("creator", securityContext.getUserPrincipal().getName());
        Task task = trainingService.initiateTraining(options, securityContext.getUserPrincipal().getName());
        return Response.ok(task).build();
    }

    private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len; (len = is.read(buffer)) != -1;)
            os.write(buffer, 0, len);
        os.flush();
        return os.toByteArray();
    }
}
