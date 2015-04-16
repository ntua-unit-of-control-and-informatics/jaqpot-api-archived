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
import java.net.InetAddress;
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

    public BehaviouralTest() {
        Client client = ClientBuilder.newClient();
        try {
            String loginService = resourceBundle.getString("janitor.target") + "aa/login";
            LOG.log(Level.FINEST, "Login service : {0}", loginService);
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.putSingle("username", "guest");
            formData.putSingle("password", "guest");
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

    @Testable(name = "aa validation")
    public void validateToken() {
        Client client = ClientBuilder.newClient();
        try {
            String validationService = resourceBundle.getString("janitor.target") + "aa/validate";
            LOG.log(Level.FINEST, "AA validation service : {0}", validationService);
            Response response = client.target(validationService)
                    .request()
                    .header("subjectid", authToken)
                    .post(Entity.form(new MultivaluedHashMap<String, String>()));
            assertEquals("Token is not valid!", 200, response.getStatus());
        } finally {
            client.close();
        }
    }

    @Testable(name = "fetch weka-mlr algorithm")
    public void getAlgorithm() {
        Client client = ClientBuilder.newClient();
        try {
            String wekaAlgorithm = resourceBundle.getString("janitor.target") + "algorithm/weka-mlr";
            LOG.log(Level.FINER, "AA validation service : {0}", wekaAlgorithm);
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

    @Testable(name = "connectivity test")
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

    @Testable(name = "another test")
    public void test1() {
        assertTrue("untrue", true);
    }

    @Testable(name = "more test")
    public void test2() {
        assertTrue("untrue", true);
    }

    @Testable(name = "awesome test")
    public void test3() {
        assertTrue("untrue", true);
    }

}
