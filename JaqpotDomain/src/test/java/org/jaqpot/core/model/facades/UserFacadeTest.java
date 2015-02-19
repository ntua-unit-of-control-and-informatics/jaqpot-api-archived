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
package org.jaqpot.core.model.facades;

import java.util.HashMap;
import java.util.Random;
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
public class UserFacadeTest {

    private static final Random rng = new Random(System.currentTimeMillis());

    public UserFacadeTest() {
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
    public void testGetMaxSubstnaces_EmptyUser() {
        User u = new User();
        UserFacade userFacade = new UserFacade(u);
        int maxSubstances = userFacade.getMaxSubstnaces();
        int expectedMaxSubstances = -1;
        assertEquals(expectedMaxSubstances, maxSubstances);
    }

    @Test
    public void testGetMaxSubstnaces() {
        User u = new User();
        int expectedMaxSubstances = 1234;
        u.setCapabilities(new HashMap<>());
        u.getCapabilities().put("substances", expectedMaxSubstances);
        UserFacade userFacade = new UserFacade(u);
        int maxSubstances = userFacade.getMaxSubstnaces();
        assertEquals(expectedMaxSubstances, maxSubstances);
    }

    @Test
    public void testGetMaxFeatures_emptyUser() {
        User u = new User();
        UserFacade userFacade = new UserFacade(u);
        int maxFeatures = userFacade.getMaxFeatures();
        int expectedMaxFeatures = -1;
        assertEquals(expectedMaxFeatures, maxFeatures);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiateFacade_nullUser() {
        User u = null;
        UserFacade userFacade = new UserFacade(u);
        userFacade.getMaxFeatures();
    }

    @Test
    public void testGetMaxFeatures() {
        User u = new User();
        int expectedMaxFeatures = rng.nextInt();
        u.setCapabilities(new HashMap<>());
        u.getCapabilities().put("features", expectedMaxFeatures);
        UserFacade userFacade = new UserFacade(u);
        int maxFeatures = userFacade.getMaxFeatures();
        assertEquals(expectedMaxFeatures, maxFeatures);
    }

    @Test
    public void testGetMaxBibTeX() {
        User u = new User();
        int expectedMaxBibTeX = rng.nextInt();
        u.setCapabilities(new HashMap<>());
        u.getCapabilities().put("bibtex", expectedMaxBibTeX);
        UserFacade userFacade = new UserFacade(u);
        int maxBibTeX = userFacade.getMaxBibTeX();
        assertEquals(expectedMaxBibTeX, maxBibTeX);
    }

    @Test
    public void testGetMaxBibTeX_emptyUser() {
        User u = new User();
        int expectedMaxBibTeX = -1;
        UserFacade userFacade = new UserFacade(u);
        int maxBibTeX = userFacade.getMaxBibTeX();
        assertEquals(expectedMaxBibTeX, maxBibTeX);
    }

    @Test
    public void testGetMaxDatasets() {
        User u = new User();
        int expectedMaxDatasets = rng.nextInt();
        u.setCapabilities(new HashMap<>());
        u.getCapabilities().put("datasets", expectedMaxDatasets);
        UserFacade userFacade = new UserFacade(u);
        int maxDatasets = userFacade.getMaxDatasets();
        assertEquals(expectedMaxDatasets, maxDatasets);
    }

    @Test
    public void testGetMaxDatasets_emptyUser() {
        User u = new User();
        int expectedMaxDatasets = -1;
        UserFacade userFacade = new UserFacade(u);
        int maxDatasets = userFacade.getMaxDatasets();
        assertEquals(expectedMaxDatasets, maxDatasets);
    }

    @Test
    public void testGetMaxAlgorithms() {
        User u = new User();
        int expectedMaxAlgorithms = rng.nextInt();
        u.setCapabilities(new HashMap<>());
        u.getCapabilities().put("algorithms", expectedMaxAlgorithms);
        UserFacade userFacade = new UserFacade(u);
        int maxAlgorithms = userFacade.getMaxAlgorithms();
        assertEquals(expectedMaxAlgorithms, maxAlgorithms);
    }

    @Test
    public void testGetMaxAlgorithms_emptyUser() {
        User u = new User();
        int expectedMaxAlgorithms = -1;
        UserFacade userFacade = new UserFacade(u);
        int maxAlgorithms = userFacade.getMaxAlgorithms();
        assertEquals(expectedMaxAlgorithms, maxAlgorithms);
    }

    @Test
    public void testGetMaxCapabilityX() {
        User u = new User();
        int expectedMaxAlgorithms = rng.nextInt();
        u.setCapabilities(new HashMap<>());
        u.getCapabilities().put("x", expectedMaxAlgorithms);
        UserFacade userFacade = new UserFacade(u);
        int maxAlgorithms = userFacade.getMaxCapability("x");
        assertEquals(expectedMaxAlgorithms, maxAlgorithms);
    }

    @Test(expected = NullPointerException.class)
    public void testGetMaxCapability_nullString() {
        User u = new User();
        UserFacade userFacade = new UserFacade(u);
        userFacade.getMaxCapability(null);
    }

    @Test
    public void testMaxPublishedSubstancesPerWeek() {
        String capability = "substances";
        User u = new User();
        int expectedMaxCapability = rng.nextInt();
        u.setPublicationRatePerWeek(new HashMap<>());
        u.getPublicationRatePerWeek().put(capability, expectedMaxCapability);
        UserFacade userFacade = new UserFacade(u);
        int maxCapability = userFacade.getMaxPublishedSubstancesPerWeek();
        assertEquals(expectedMaxCapability, maxCapability);
    }

    @Test
    public void testMaxPublishedSubstancesPerWeek_emptyUser() {
        User u = new User();
        int expectedMaxCapability = -1;        
        UserFacade userFacade = new UserFacade(u);
        int maxCapability = userFacade.getMaxPublishedSubstancesPerWeek();
        assertEquals(expectedMaxCapability, maxCapability);
    }

    @Test
    public void testMaxPublishedFeaturesPerWeek() {
        String capability = "features";
        User u = new User();
        int expectedMaxCapability = rng.nextInt();
        u.setPublicationRatePerWeek(new HashMap<>());
        u.getPublicationRatePerWeek().put(capability, expectedMaxCapability);
        UserFacade userFacade = new UserFacade(u);
        int maxCapability = userFacade.getMaxPublishedFeaturesPerWeek();
        assertEquals(expectedMaxCapability, maxCapability);
    }

    @Test
    public void testMaxPublishedFeaturesPerWeek_emptyUser() {
        User u = new User();
        int expectedMaxCapability = -1;        
        UserFacade userFacade = new UserFacade(u);
        int maxCapability = userFacade.getMaxPublishedFeaturesPerWeek();
        assertEquals(expectedMaxCapability, maxCapability);
    }

    @Test
    public void testMaxPublishedDatasetsPerWeek() {
        String capability = "datasets";
        User u = new User();
        int expectedMaxCapability = rng.nextInt();
        u.setPublicationRatePerWeek(new HashMap<>());
        u.getPublicationRatePerWeek().put(capability, expectedMaxCapability);
        UserFacade userFacade = new UserFacade(u);
        int maxCapability = userFacade.getMaxPublishedDatasetsPerWeek();
        assertEquals(expectedMaxCapability, maxCapability);
    }

    @Test
    public void testMaxPublishedDatasetsPerWeek_emptyUser() {
        User u = new User();
        int expectedMaxCapability = -1;        
        UserFacade userFacade = new UserFacade(u);
        int maxCapability = userFacade.getMaxPublishedDatasetsPerWeek();
        assertEquals(expectedMaxCapability, maxCapability);
    }

    @Test
    public void testMaxPublishedAlgorithmsPerWeek() {
        String capability = "algorithms";
        User u = new User();
        int expectedMaxCapability = rng.nextInt();
        u.setPublicationRatePerWeek(new HashMap<>());
        u.getPublicationRatePerWeek().put(capability, expectedMaxCapability);
        UserFacade userFacade = new UserFacade(u);
        int maxCapability = userFacade.getMaxPublishedAlgorithmsPerWeek();
        assertEquals(expectedMaxCapability, maxCapability);
    }

    @Test
    public void testMaxPublishedAlgorithmsPerWeek_emptyUser() {
        User u = new User();
        int expectedMaxCapability = -1;        
        UserFacade userFacade = new UserFacade(u);
        int maxCapability = userFacade.getMaxPublishedAlgorithmsPerWeek();
        assertEquals(expectedMaxCapability, maxCapability);
    }

}
