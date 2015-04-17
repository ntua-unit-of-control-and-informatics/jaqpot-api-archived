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
package org.kinkydesign.jaqpotjanitor.tests;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Task;
import org.kinkydesign.jaqpotjanitor.core.Testable;
import static org.kinkydesign.jaqpotjanitor.core.JanitorUtils.*;

/**
 *
 * @author chung
 */
@Testable
@XmlRootElement
public class BehaviouralTest {

    private static final Logger LOG = Logger.getLogger(BehaviouralTest.class.getName());

    private String authToken = null;

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("config");

    private static final long STANDARD_DURATION_LIMIT = 1200l;

    public BehaviouralTest() {
        Client client = ClientBuilder.newClient();
        try {
            String loginService = resourceBundle.getString("janitor.target") + "aa/login";
            LOG.log(Level.FINEST, "Login service : {0}", loginService);
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.putSingle("username", resourceBundle.getString("janitor.username"));
            formData.putSingle("password", resourceBundle.getString("janitor.password"));
            Response response = client.target(loginService)
                    .request()
                    .accept(MediaType.TEXT_PLAIN)
                    .post(Entity.form(formData));
            if (200 == response.getStatus()) {
                authToken = response.readEntity(String.class);
                LOG.log(Level.FINER, "TOKEN : {0}", authToken);
            } else {
                LOG.log(Level.SEVERE, "Authorization status : {0} (cannot log in)", response.getStatus());
            }
        } finally {
            client.close();
        }

    }

    @Testable(name = "aa validation", description = "validates the AA token", maxDuration = STANDARD_DURATION_LIMIT)
    public void validateToken() {
        Client client = ClientBuilder.newClient();
        try {
            String validationService = resourceBundle.getString("janitor.target") + "aa/validate";
            Response response = client.target(validationService)
                    .request()
                    .header("subjectid", authToken)
                    .post(Entity.form(new MultivaluedHashMap<String, String>()));
            assertEquals("Token is not valid!", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "authorize", description = "tests authorization service", maxDuration = STANDARD_DURATION_LIMIT)
    public void testAuthorize() {
        Client client = ClientBuilder.newClient();
        try {
            String authorizationService = resourceBundle.getString("janitor.target") + "aa/authorize";
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.putSingle("uri", "http://opentox.ntua.gr:8080/bibtex");
            formData.putSingle("method", "GET");
            Response response = client.target(authorizationService)
                    .request()
                    .header("subjectid", authToken)
                    .post(Entity.form(formData));
            String outcome = response.readEntity(String.class);
            int status = response.getStatus();
            assertEquals("Status " + status, 200, status);
            assertNotNull("No response", outcome);
            assertEquals("Not authorized!", "true", outcome.trim());
        } finally {
            client.close();
        }
    }

    @Testable(name = "fetch weka-mlr algorithm", maxDuration = STANDARD_DURATION_LIMIT)
    public void getWekaAlgorithm() {
        Client client = ClientBuilder.newClient();
        try {
            String wekaAlgorithm = resourceBundle.getString("janitor.target") + "algorithm/weka-mlr";
            LOG.log(Level.FINER, "Weka algorithm URI : {0}", wekaAlgorithm);
            Response response = client.target(wekaAlgorithm)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("subjectid", authToken)
                    .get();
            LOG.log(Level.FINER, "Response status : {0}", response.getStatus());
            assertEquals("Fetching algorithm fails", 200, response.getStatus());
            Algorithm wekaMlrAlgorithm = response.readEntity(Algorithm.class);
            assertNotNull("WekaMLR algorithm has no training service", wekaMlrAlgorithm.getTrainingService());
        } finally {
            client.close();
        }
    }

    @Testable(name = "connectivity test", maxDuration = STANDARD_DURATION_LIMIT)
    public void testConnetivity() throws UnknownHostException, IOException {
        Client client = ClientBuilder.newClient();
        try {
            String testURI = "http://www.ntua.gr";
            Response response = client.target(testURI)
                    .request().get();
            assertEquals("Fetching algorithm fails", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "list BibTeX", maxDuration = STANDARD_DURATION_LIMIT)
    public void testListBibTeX() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "bibtex?start=0&max=20";
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .get();
            assertEquals("List of bibtex failed", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "list algorithms", maxDuration = STANDARD_DURATION_LIMIT)
    public void testListAlgorithms() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "algorithm?start=0&max=20";
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .get();
            assertEquals("List of algorithms failed", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "list models", maxDuration = STANDARD_DURATION_LIMIT)
    public void testListModels() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "model?start=0&max=20";
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .get();
            assertEquals("List of algorithms failed", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "list datasets", maxDuration = STANDARD_DURATION_LIMIT)
    public void testListDatasets() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "dataset?start=0&max=20";
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .get();
            assertEquals("List of algorithms failed", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "list pmml")
    public void testListPmml() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "pmml?start=0&max=20";
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .get();
            assertEquals("List of features failed", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "list features")
    public void testListFeatures() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "feature?start=0&max=20";
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .get();
            int status = response.getStatus();
            assertEquals("List of features failed with status " + status, 200, status);
        } finally {
            client.close();
        }
    }

    @Testable(name = "list tasks")
    public void testLisTasks() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "task?start=0&max=1";
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .get();
            int status = response.getStatus();
            assertEquals("request /task failed with status " + status, 200, status);
        } finally {
            client.close();
        }
    }

    @Testable(name = "CORS test", maxDuration = STANDARD_DURATION_LIMIT)
    public void testCORS() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "algorithm?start=0&max=1";
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .get();
            assertNotNull("access-control-allow-origin=null", response.getHeaderString("access-control-allow-origin"));
            assertNotNull("access-control-allow-methods=null", response.getHeaderString("access-control-allow-methods"));
            assertNotNull("access-control-allow-headers=null", response.getHeaderString("access-control-allow-headers"));
            assertEquals("request /algorithm?start=0&max=1 failed", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "list users", maxDuration = STANDARD_DURATION_LIMIT)
    public void testLisUsers() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "user?start=0&max=20";
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .get();
            assertEquals("List of users failed", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "long running",
            maxDuration = 3000l,
            description = "this test should be interrupted by the runner "
            + "because it is taking too long to comlete (20s) while its "
            + "maximum duration is set to 3s.")
    public void longRunningTask() {
        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() - now < 20000) {
            // do nothing and wait!
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }
        LOG.severe("Test was allowed to continue running after timeout!!!");
    }

    @Testable(name = "train mlr model",
            maxDuration = 3000l,
            description = "train a model")
    public void trainModel() {
        Client client = ClientBuilder.newClient();
        try {
            String uri = resourceBundle.getString("janitor.target") + "algorithm/weka-mlr";
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.putSingle("dataset_uri", "http://enanomapper.ntua.gr:8880/jaqpot/services/dataset/ca8da7f6-ee9f-4a61-9ae4-b1d1525cef88");
            formData.putSingle("prediction_feature", "property/TOX/UNKNOWN_TOXICITY_SECTION/Total+surface+area++SAtot+/52D93BC3B68F26C8E787CC7A05E5130A23164405/3ed642f9-1b42-387a-9966-dea5b91e5f8a");
            formData.putSingle("doa", "http://enanomapper.ntua.gr:8880/jaqpot/services/algorithm/leverage");
            Response response = client.target(uri)
                    .request()
                    .accept("text/uri-list")
                    .header("subjectid", authToken)
                    .post(Entity.form(formData));
            assertEquals("Task OK 202", 202, response.getStatus());
            Task modellingTask = response.readEntity(Task.class);
            assertEquals("Task is not QUEUED", Task.Status.QUEUED, modellingTask.getStatus());
        } finally {
            client.close();
        }
    }
}
