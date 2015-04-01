/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.jaqpot.core.model.builder;

import org.jaqpot.core.model.BibTeX;
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
public class BibTeXBuilderTest {
    
    public BibTeXBuilderTest() {
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
    public void testBuild() {
        System.out.println("build");
        BibTeXBuilder instance = BibTeXBuilder.builder("id");
        assertNotNull(instance);
        BibTeX result = instance.build();
        assertNotNull(result.getId());
    }   

    @Test
    public void testSetAddress() {
        System.out.println("setAddress");
        String address = "asdf";
        BibTeXBuilder instance = BibTeXBuilder.builderRandomUuid();
        assertNotNull(instance);
        BibTeXBuilder result = instance.setAddress(address);
        assertNotNull(result);
        BibTeX bibtex = result.build();
        assertNotNull(bibtex);
        assertNotNull(bibtex.getAddress());        
        assertEquals(address, bibtex.getAddress());
    }

    @Test
    public void testSetTitle() {
        System.out.println("setTitle");
        String title = "asdf";
        BibTeXBuilder instance = BibTeXBuilder.builderRandomUuid();
        assertNotNull(instance);
        BibTeXBuilder result = instance.setTitle(title);
        assertNotNull(result);
        BibTeX bibtex = result.build();
        assertNotNull(bibtex);
        assertNotNull(bibtex.getTitle());        
        assertEquals(title, bibtex.getTitle());
    }

   @Test
    public void testYear() {
        System.out.println("setTitle");
        String year = "asdf";
        BibTeXBuilder instance = BibTeXBuilder.builderRandomUuid();
        assertNotNull(instance);
        BibTeXBuilder result = instance.setYear(year);
        assertNotNull(result);
        BibTeX bibtex = result.build();
        assertNotNull(bibtex);
        assertNotNull(bibtex.getYear());        
        assertEquals(year, bibtex.getYear());
    }
    
}
