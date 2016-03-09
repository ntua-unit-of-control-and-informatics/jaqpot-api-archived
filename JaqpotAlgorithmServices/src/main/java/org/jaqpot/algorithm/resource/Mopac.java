/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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
package org.jaqpot.algorithm.resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.jaqpot.core.model.dto.ambit.AmbitTask;
import org.jaqpot.core.model.dto.ambit.AmbitTaskArray;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@Path("mopac")
@Produces(MediaType.APPLICATION_JSON)
public class Mopac {

    @POST
    @Path("calculate")
    public Response calculate(
            @FormParam("pdbfile") String pdbFile,
            @HeaderParam("subjectid") String subjectId) {

        try {
            byte[] file;
            if (pdbFile.startsWith("data:")) {
                String base64pdb = pdbFile.split(",")[1];
                file = Base64.getDecoder().decode(base64pdb.getBytes());
            } else {
                URL pdbURL = new URL(pdbFile);
                file = IOUtils.toByteArray(pdbURL.openStream());
            }
            ResteasyClient client = new ResteasyClientBuilder().disableTrustManager().build();
            ResteasyWebTarget target = client.target("https://apps.ideaconsult.net/enmtest/dataset");
            String fileName = UUID.randomUUID().toString() + ".pdb";
            MultipartFormDataOutput mdo = new MultipartFormDataOutput();
            mdo.addFormData("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE, fileName);
            GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(
                    mdo) {
            };

            Response response = target
                    .request()
                    .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));

            AmbitTaskArray ambitTaskArray = response.readEntity(AmbitTaskArray.class);

            AmbitTask ambitTask = ambitTaskArray.getTask().get(0);
            String ambitTaskUri = ambitTask.getUri();
            System.out.println("Task POST Dataset:" + ambitTaskUri);
            while (ambitTask.getStatus().equals("Running") || ambitTask.getStatus().equals("Queued")) {
                ambitTaskArray = client.target(ambitTaskUri)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("subjectid", subjectId)
                        .get(AmbitTaskArray.class);
                ambitTask = ambitTaskArray.getTask().get(0);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    continue;
                }
            }
            String datasetUri;
            if (ambitTask.getStatus().equals("Completed")) {
                datasetUri = ambitTask.getResult();
            } else {
                return Response
                        .status(Response.Status.BAD_GATEWAY)
                        .entity(ErrorReportFactory.remoteError(ambitTaskUri, ErrorReportFactory.internalServerError(), null))
                        .build();
            }

            System.out.println("New Dataset:" + datasetUri);

            MultivaluedMap algorithmFormData = new MultivaluedHashMap();
            algorithmFormData.add("dataset_uri", datasetUri);
            algorithmFormData.add("mopac_commands", "PM3 NOINTER MMOK BONDS MULLIK GNORM=1.0 T=30.00M");

            response = client.target("https://apps.ideaconsult.net/enmtest/algorithm/ambit2.mopac.MopacOriginalStructure")
                    .request()
                    .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.form(algorithmFormData));

            ambitTaskArray = response.readEntity(AmbitTaskArray.class);

            ambitTask = ambitTaskArray.getTask().get(0);
            ambitTaskUri = ambitTask.getUri();
            System.out.println("Task POST Dataset:" + ambitTaskUri);
            while (ambitTask.getStatus().equals("Running") || ambitTask.getStatus().equals("Queued")) {
                ambitTaskArray = client.target(ambitTaskUri)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("subjectid", subjectId)
                        .get(AmbitTaskArray.class);
                ambitTask = ambitTaskArray.getTask().get(0);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    continue;
                }
            }
            if (ambitTask.getStatus().equals("Completed")) {
                datasetUri = ambitTask.getResult();
            } else {
                return Response
                        .status(Response.Status.BAD_GATEWAY)
                        .entity(ErrorReportFactory.remoteError(ambitTaskUri, ErrorReportFactory.internalServerError(), null))
                        .build();
            }

            System.out.println(datasetUri);

            response = client.target(datasetUri)
                    .request()
                    .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get();

            Dataset dataset = response.readEntity(Dataset.class);

            return Response.ok(dataset.getDataEntry().get(0).getValues()).build();
        } catch (MalformedURLException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorReportFactory.badRequest(ex.getMessage(), "Bad pdb file:" + pdbFile))
                    .build();
        } catch (IOException ex) {
            return Response
                    .status(Response.Status.BAD_GATEWAY)
                    .entity(ErrorReportFactory.remoteError(ex.getMessage(), ErrorReportFactory.internalServerError(), ex))
                    .build();
        }
    }

}
