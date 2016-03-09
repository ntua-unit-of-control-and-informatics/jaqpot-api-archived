/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenides, Pantelis Sopasakis)
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
package org.jaqpot.core.model.builder;

import java.util.Random;
import java.util.UUID;
import org.jaqpot.core.model.User;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class UserBuilderTest {
    
    private static final Random RNG = new Random(System.currentTimeMillis());

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
        assertEquals(nMaxBibTeX, u.getCapabilities().get("bibtex"));
        assertEquals(nMaxModels, u.getCapabilities().get("models"));
        assertEquals(nMaxSubstances, u.getCapabilities().get("substances"));
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
    public void testSetName() {
        String name = UUID.randomUUID().toString();
        User u = UserBuilder.builder("user:::"+name).
                setName(name).build();
        assertEquals(name, u.getName());
        
    }

    @Test
    public void testSetMail() {
        String mail = UUID.randomUUID().toString()+"@jaqpot.org";
        User u = UserBuilder.builder("user1234").
                setMail(mail).build();
        assertEquals(mail, u.getMail());
    }

    @Test
    public void testSetMaxCapability() {
        String capability = "resource.xyz";
        int maxCapabilityX = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxCapability(capability, maxCapabilityX).build();
        assertNotNull(u.getCapabilities());
        assertEquals(maxCapabilityX, (int) u.getCapabilities().get(capability));
    }

    @Test
    public void testSetMaxWeeklyCapability() {
        String capability = "resource.xyz";
        int maxCapabilityX = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxWeeklyCapability(capability, maxCapabilityX).build();
        assertNotNull(u.getPublicationRatePerWeek());
        assertEquals(maxCapabilityX, (int) u.getPublicationRatePerWeek().get(capability));
    }

    @Test
    public void testSetMaxWeeklyPublishedBibTeX() {
        int maxPublishedbibtex = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxWeeklyPublishedBibTeX(maxPublishedbibtex).build();
        assertNotNull(u.getPublicationRatePerWeek());
        assertEquals(maxPublishedbibtex, (int) u.getPublicationRatePerWeek().get("bibtex"));
    }

    @Test
    public void testSetMaxBibTeX() {
        int maxBibTeX = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxBibTeX(maxBibTeX).build();
        assertNotNull(u.getCapabilities());
        assertEquals(maxBibTeX, (int) u.getCapabilities().get("bibtex"));
    }

    @Test
    public void testSetMaxWeeklyPublishedSubstances() {
        int maxPublishedSubstances = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxWeeklyPublishedSubstances(maxPublishedSubstances).build();
        assertNotNull(u.getPublicationRatePerWeek());
        assertEquals(maxPublishedSubstances, (int) u.getPublicationRatePerWeek().get("substances"));
    }

    @Test
    public void testSetMaxSubstances() {
        int maxSubstances = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxSubstances(maxSubstances).build();
        assertNotNull(u.getCapabilities());
        assertEquals(maxSubstances, (int) u.getCapabilities().get("substances"));
    }

    @Test
    public void testSetMaxWeeklyPublishedModels() {
        int maxPublishedModels = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxWeeklyPublishedModels(maxPublishedModels).build();
        assertNotNull(u.getPublicationRatePerWeek());
        assertEquals(maxPublishedModels, (int) u.getPublicationRatePerWeek().get("models"));
    }

    @Test
    public void testSetMaxModels() {
        int maxModels = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxModels(maxModels).build();
        assertNotNull(u.getCapabilities());
        assertEquals(maxModels, (int) u.getCapabilities().get("models"));
    }

    @Test
    public void testSetMaxWeeklyPublishedFeatures() {
        int maxPublishedFeatures = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxWeeklyPublishedFeatures(maxPublishedFeatures).build();
        assertNotNull(u.getPublicationRatePerWeek());
        assertEquals(maxPublishedFeatures, (int) u.getPublicationRatePerWeek().get("features"));
    }

    @Test
    public void testSetMaxFeatures() {
        int maxFeatures = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxFeatures(maxFeatures).build();
        assertNotNull(u.getCapabilities());
        assertEquals(maxFeatures, (int) u.getCapabilities().get("features"));
    }

    @Test
    public void testSetMaxParallelTasks() {        
        int maxTasks = RNG.nextInt();
        User u = UserBuilder.builder("user"+RNG.nextInt()).
                setMaxParallelTasks(maxTasks).build();
        assertNotNull(u.getCapabilities());
        assertEquals(maxTasks, (int) u.getCapabilities().get("tasks.parallel"));
    }

    @Test
    public void testSetHashedPassword() {        
        String hashedPassword = UUID.randomUUID().toString();
        User u = UserBuilder.builder("asdf").
                setHashedPassword(hashedPassword).build();
        assertEquals(hashedPassword, u.getHashedPass());
    }

}
