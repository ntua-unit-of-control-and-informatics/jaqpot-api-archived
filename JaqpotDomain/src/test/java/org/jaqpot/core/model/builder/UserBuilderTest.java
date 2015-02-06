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
    
    
}
