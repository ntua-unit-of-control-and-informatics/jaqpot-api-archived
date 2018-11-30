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

import io.swagger.annotations.*;
import org.dmg.pmml.Application;
import org.dmg.pmml.*;
import org.jaqpot.core.data.PmmlHandler;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Pmml;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.model.util.ROG;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.annotations.TokenSecured;

import org.jaqpot.core.service.authentication.AAService;
import org.jaqpot.core.service.authentication.RoleEnum;

import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import org.jaqpot.core.service.exceptions.JaqpotNotAuthorizedException;
import org.jpmml.model.JAXBUtil;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author chung
 */
@Path("pmml")
@Api(value = "/pmml", description = "PMML API")
public class PmmlResource {
    
    @EJB
    AAService aaService;
    
    @Context
    SecurityContext securityContext;
    
    @Context
    UriInfo uriInfo;
    
    @EJB
    PmmlHandler pmmlHandler;
    
    @Context
    HttpHeaders httpHeaders;
    
    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @ApiOperation(value = "Creates a new PMML entry",
            notes = "Creates a new PMML entry which is assigned a random unique ID",
            response = Pmml.class)
    //TODO add code for user's quota exceeded
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "PMML entry was created successfully."),
        @ApiResponse(code = 400, message = "Bad request: malformed PMML (e.g., mandatory fields are missing)"),
        @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    @Authorize
    public Response createPMML(
            @ApiParam(value = "Clients need to authenticate in order to create resources on the server")
            @HeaderParam("Authorization") String subjectId,
            @ApiParam(value = "PMML in JSON representation.", required = true) String pmmlString,
            @ApiParam(value = "title") @FormParam("title") String title,
            @ApiParam(value = "description") @FormParam("description") String description
    ) throws JaqpotNotAuthorizedException, JaqpotDocumentSizeExceededException {
        // First check the subjectid:
//        if (subjectId == null || !aaService.validate(subjectId)) {
//            throw new JaqpotNotAuthorizedException("Invalid auth token");
//        }
        if (pmmlString == null) {
            ErrorReport report = ErrorReportFactory.badRequest("No PMML document provided; check out the API specs",
                    "Clients MUST provide a PMML document in JSON to perform this request");
            return Response.ok(report).status(Response.Status.BAD_REQUEST).build();
        }
        Pmml pmml = new Pmml();
        pmml.setPmml(pmmlString);
        if (pmml.getId() == null) {
            ROG rog = new ROG(true);
            pmml.setId(rog.nextString(10));
        }
        
        MetaInfo info = MetaInfoBuilder.builder()
                .addTitles(title)
                .addDescriptions(description)
                .addCreators(securityContext.getUserPrincipal().getName())
                .build();
        pmml.setMeta(info);
        pmml.setVisible(Boolean.TRUE);
        
        pmmlHandler.create(pmml);
        return Response
                .ok(pmml)
                .status(Response.Status.CREATED)
                .header("Location", uriInfo.getBaseUri().toString() + "pmml/" + pmml.getId())
                .build();
    }
    
    @POST
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Path("/selection")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Creates a new PMML entry",
            notes = "Creates a new PMML entry which is assigned a random unique ID",
            response = Pmml.class)
    public Response createPMMLSelection(
            @ApiParam(value = "Authorization token") @HeaderParam("Authorization") String subjectId,
            @FormParam("features") String featuresString) throws  JaqpotDocumentSizeExceededException {

        List<String> features = Arrays.asList(featuresString.split(","));
        try {
            PMML pmml = new PMML();
            pmml.setVersion("4.2");
            Header header = new Header();
            header.setCopyright("NTUA Chemical Engineering Control Lab 2015");
            header.setDescription("PMML created for feature selection");
            header.setTimestamp(new Timestamp());
            header.setApplication(new Application("Jaqpot Quattro"));
            pmml.setHeader(header);
            
            List<DataField> dataFields = features
                    .stream()
                    .map(feature -> {
                        DataField dataField = new DataField();
                        dataField.setName(new FieldName(feature));
                        dataField.setOpType(OpType.CONTINUOUS);
                        dataField.setDataType(DataType.DOUBLE);
                        return dataField;
                    })
                    .collect(Collectors.toList());
            DataDictionary dataDictionary = new DataDictionary(dataFields);
            pmml.setDataDictionary(dataDictionary);
            
            TransformationDictionary transformationDictionary = new TransformationDictionary();
            List<DerivedField> derivedFields = features
                    .stream()
                    .map(feature -> {
                        DerivedField derivedField = new DerivedField();
                        derivedField.setOpType(OpType.CONTINUOUS);
                        derivedField.setDataType(DataType.DOUBLE);
                        derivedField.setName(new FieldName(feature));
                        derivedField.setExpression(new FieldRef(new FieldName(feature)));
                        return derivedField;
                    })
                    .collect(Collectors.toList());
            transformationDictionary.withDerivedFields(derivedFields);
            
            pmml.setTransformationDictionary(transformationDictionary);
            
            ByteArrayOutputStream pmmlBaos = new ByteArrayOutputStream();
            JAXBUtil.marshalPMML(pmml, new StreamResult(pmmlBaos));
            
            Pmml pmmlResource = new Pmml();
            pmmlResource.setPmml(pmmlBaos.toString());
            ROG rog = new ROG(true);
            pmmlResource.setId(rog.nextString(10));
            
            MetaInfo info = MetaInfoBuilder.builder()
                    .addTitles("PMML")
                    .addDescriptions("PMML created for feature selection")
                    .addCreators(securityContext.getUserPrincipal().getName())
                    .build();
            pmmlResource.setMeta(info);
            
            pmmlHandler.create(pmmlResource);
            return Response
                    .ok(pmmlResource)
                    .status(Response.Status.CREATED)
                    .header("Location", uriInfo.getBaseUri().toString() + "pmml/" + pmmlResource.getId())
                    .build();
        } catch (JAXBException ex) {
            Logger.getLogger(PmmlResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

    }
    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list", MediaType.APPLICATION_XML, "text/xml"})
    @ApiOperation(value = "Returns PMML entry",
            notes = "Finds and returns a PMML document by ID",
            response = Pmml.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "BibTeX entries found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this user"),
        @ApiResponse(code = 404, message = "No such bibtex entry on the server (not found)"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getPmml(
            @ApiParam(value = "Authorization token") @HeaderParam("Authorization") String subjectId,
            @ApiParam(value = "ID of the BibTeX", required = true) @PathParam("id") String id
    ) {
        
        Pmml retrievedPmml = pmmlHandler.find(id);
        if (retrievedPmml == null) {
            throw new NotFoundException("PMML with ID " + id + " not found.");
        }
        // get the Accept header to judge how to format the PMML (JSON or XML)
        String accept = httpHeaders.getRequestHeader("Accept").stream().findFirst().orElse(null);
        if (accept != null && ("application/xml".equals(accept) || "text/xml".equals(accept))) {
            return Response.ok(retrievedPmml.getPmml(), accept).build();
        } else {
            return Response.ok(retrievedPmml).build();
        }
    }
    
    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all PMML entries",
            notes = "Finds all PMML entries in the DB of Jaqpot and returns them in a list",
            response = Pmml.class,
            responseContainer = "List",
            position = 1)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "PMML entries found and are listed in the response body"),
        @ApiResponse(code = 401, message = "You are not authorized to access this resource"),
        @ApiResponse(code = 403, message = "This request is forbidden (e.g., no authentication token is provided)"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listPmml(
            @ApiParam(value = "Authorization token") @HeaderParam("Authorization") String subjectId,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max", defaultValue = "10") @QueryParam("max") Integer max
    ) {
        String creator = securityContext.getUserPrincipal().getName();
        return Response
                .ok(pmmlHandler.listMetaOfCreator(creator, start != null ? start : 0, max != null ? max : Integer.MAX_VALUE))
                .status(Response.Status.OK)
                .header("total", pmmlHandler.countAllOfCreator(creator))
                .build();
    }
    
}