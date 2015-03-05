/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licenced
 * with the aforementioned licence. 
 */
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.service.dto.dataset.Dataset;
import org.jaqpot.core.service.dto.study.Studies;

/**
 *
 * @author hampos
 */
@Path("algorithm")
@Api(value = "/algorithm", description = "Operations about Algorithms")
@Produces({"application/json", "text/uri-list"})
public class AlgorithmResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Algorithms",
            notes = "Finds all Algorithms JaqpotQuattro supports",
            response = Algorithm.class,
            responseContainer = "List")
    public Response getAlgorithms() {
        List<Algorithm> algorithms = new ArrayList<>();
        algorithms.add(new Algorithm("asdfasdf"));
        algorithms.add(new Algorithm("qwerqwer"));
        return Response.ok(algorithms).build();
    }

    @POST
    @Produces("text/uri-list")
    public Response createAlgorithm() {
        try {
            return Response.created(new URI("")).build();
        } catch (URISyntaxException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{name}")
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds Algorithm",
            notes = "Finds Algorithm with provided name",
            response = Algorithm.class
    )
    public Response getAlgorithm(@PathParam("name") String algorithmName) {
        return Response.ok(new Algorithm(algorithmName)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}")
    public Response trainModel(
            @FormParam("dataset_uri") String datasetURI,
            @FormParam("prediction_feature") String predictionFeature,
            @FormParam("parameters") String parameters,
            @PathParam("name") String algorithmName,
            @HeaderParam("subjectid") String subjectId) {

        System.out.println(subjectId);
        try {
            Client client = buildUnsecureRestClient();
            Dataset dataset = client.target(datasetURI)
                    .request()
                    .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Dataset.class);

            return Response.ok(dataset).build();
        } catch (GeneralSecurityException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/studies/1")
    @ApiOperation(value = "Finds Studies",
            notes = "Finds Studies test",
            response = Studies.class
    )
    public Response testStudies() {
        try {
            Client client = buildUnsecureRestClient();
            Studies studies = client.target("https://svn.code.sf.net/p/ambit/code/trunk/ambit2-all/ambit2-core/src/test/resources/ambit2/core/data/json/matrixupdate.json")
                    .request()
                    // .header("subjectid", subjectId)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Studies.class);

            return Response.ok(studies).build();
        } catch (GeneralSecurityException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    public Client buildUnsecureRestClient() throws GeneralSecurityException {
        SSLContext context = SSLContext.getInstance("TLSv1");
        TrustManager[] trustManagerArray = {
            new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
        };
        context.init(null, trustManagerArray, null);
        return ClientBuilder.newBuilder()
                .hostnameVerifier((String string, SSLSession ssls) -> true)
                .sslContext(context)
                .build();
    }
}
