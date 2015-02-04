/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalambos Chomenides, Pantelis Sopasakis)
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
package org.jaqpot.core.model.builder;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.jaqpot.core.model.User;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chung
 */
public class UserBuilderTest {

    public UserBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testNormalBuildUser() {
        String userID = "john@jaqpot.org",
                hashedPass = "ajsdhkash",
                email = "me@my.email.com";
        Integer nMaxModels = 100,
                nMaxBibTeX = 10000,
                nMaxSubstances = 666;
        UserBuilder uBuilder = UserBuilder.builder(userID);
        User u = uBuilder.setHashedPassword(hashedPass).
                setMail(email).
                setMaxBibTeX(nMaxBibTeX).
                setMaxModels(nMaxModels).
                setMaxSubstances(nMaxSubstances).build();
        assertNotNull(u);
        assertEquals(userID, u.getId());
        assertEquals(hashedPass, u.getHashedPass());
        assertEquals(email, u.getMail());
        assertEquals(nMaxBibTeX, u.getMaxBibTeX());
        assertEquals(nMaxModels, u.getMaxModels());
        assertEquals(nMaxSubstances, u.getMaxSubstances());               
    }

    @Test
    public void testProperSerializationJackson() throws Exception {
        UserBuilder builder = UserBuilder.builder("random@jaqpot.org");
        User u = builder.setHashedPassword("skjhfkjdshkfjs").
                setMail("random@gmail.com").
                setMaxBibTeX(100).
                setMaxModels(1000).
                setMaxSubstances(10000).
                setName("Random Person").
                setParallelTasks(6).build();

        /* Create an object mapper to serialize the user object */
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT); // optional

        try (PipedInputStream in = new PipedInputStream(1024) // Piped IS
                ) {
            PipedOutputStream out = new PipedOutputStream(in); // Piped OS

            mapper.writeValue(out, u); // Write user to the OS

            User recovered = mapper.readValue(in, User.class); // Reads user object from IS
            assertNotNull(recovered);
            assertNotNull(recovered.getHashedPass());
            assertNotNull(recovered.getMail());
            assertNotNull(recovered.getMaxBibTeX());
            assertNotNull(recovered.getMaxSubstances());
            assertNotNull(recovered.getName());
            assertNotNull(recovered.getId());
            assertEquals(u.getHashedPass(), recovered.getHashedPass());
            assertEquals(u.getMaxModels(), recovered.getMaxModels());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEmail() {
        UserBuilder uBuilder = UserBuilder.builder("chung@jaqpot.org");
        uBuilder.setHashedPassword("password");
        uBuilder.setMail("invalidemail");
        fail("execution shouldn't have reached here!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEmail() {
        UserBuilder uBuilder = UserBuilder.builder("chung@jaqpot.org");
        uBuilder.setMail(null);
        fail("execution shouldn't have reached here!");
    }
    
    @Test
    public void testMarshaller() throws JAXBException{
        UserBuilder builder = UserBuilder.builder("random@jaqpot.org");
        User u = builder.setHashedPassword("skjhfkjdshkfjs").
                setMail("random@gmail.com").
                setMaxBibTeX(100).
                setMaxModels(1000).
                setMaxSubstances(10000).
                setName("Random Person").
                setParallelTasks(6).build();
        JAXBContext jc = JAXBContext.newInstance(User.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        marshaller.marshal(u, System.out);
    }
}
