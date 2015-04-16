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

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import org.jaqpot.core.model.Algorithm;
import org.kinkydesign.jaqpotjanitor.core.TestResult;
import org.kinkydesign.jaqpotjanitor.core.Testable;
import static org.kinkydesign.jaqpotjanitor.core.JanitorUtils.*;

/**
 *
 * @author chung
 */
@Testable
@XmlRootElement
public class BehaviouralTest {

    Client client = ClientBuilder.newClient();

    private static final Logger LOG = Logger.getLogger(BehaviouralTest.class.getName());

    private String authToken = null;

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("config");

    public BehaviouralTest() {
        String loginService = resourceBundle.getString("janitor.target") + "aa/login";
        LOG.log(Level.INFO, "Login service : {0}", loginService);
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("username", "guest");
        formData.putSingle("password", "guest");
        Response response = client.target(loginService)
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.form(formData));
        if (200 == response.getStatus()) {
            authToken = response.readEntity(String.class);
            LOG.log(Level.INFO, "TOKEN : {0}", authToken);
        } else {
            LOG.log(Level.SEVERE, "Authorization status : {0} (cannot log in)", response.getStatus());
        }

    }

    @Testable(name = "fetch weka-mlr algorithm")
    public void getAlgorithm() {
        String wekaAlgorithm = resourceBundle.getString("janitor.target") + "algorithm/weka-mlr";
        LOG.log(Level.INFO, "AA validation service : {0}", wekaAlgorithm);
        Response response = client.target(wekaAlgorithm)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("subjectid", authToken)
                .get();
        assertEquals("Fetching algorithm fails", 200, response.getStatus());
        Algorithm wekaMlrAlgorithm = response.readEntity(Algorithm.class);
        assertNotNull("WekaMLR algorithm has no training service", wekaMlrAlgorithm.getTrainingService());
    }
    
    @Testable(name = "assertion fails")
    public void testAssertion() {        
        Object o = null;
        assertNotNull("Opps... assertion failed", o);
    }

}
