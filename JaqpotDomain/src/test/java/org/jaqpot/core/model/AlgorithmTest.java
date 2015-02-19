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
package org.jaqpot.core.model;

import java.util.Set;
import org.jaqpot.core.model.util.ROG;
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
public class AlgorithmTest {
    
    static ROG rog = new ROG(false);
    
    public AlgorithmTest() {
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
    public void testAlgorithmCloning_deepCopy() {
        
        Algorithm alg_old = rog.nextAlgorithm();
        
        assertNotNull(alg_old.getBibtex());
        assertNotNull(alg_old.getCreatedBy());
        assertNotNull(alg_old.getParameters());
        
        Algorithm alg_copied = new Algorithm(alg_old); // Copy-construct!
        
        alg_old.setBibtex(null);
        alg_old.setCreatedBy(null);
        alg_old.setParameters(null);
        
        assertNotNull(alg_copied.getBibtex());
        assertNotNull(alg_copied.getCreatedBy());
        assertNotNull(alg_copied.getParameters());
    }
    
    @Test
    public void testAlgorithmCloning_deepCopy2() {
        
        Algorithm alg_old = rog.nextAlgorithm();
        
        assertNotNull(alg_old.getBibtex());
        assertNotNull(alg_old.getCreatedBy());
        assertNotNull(alg_old.getParameters());
        
        Algorithm alg_copied = new Algorithm(alg_old); // Copy-construct!
        
        alg_copied.setBibtex(null);
        alg_copied.setCreatedBy(null);
        alg_copied.setParameters(null);
        
        assertNotNull(alg_old.getBibtex());
        assertNotNull(alg_old.getCreatedBy());
        assertNotNull(alg_old.getParameters());
    }
    
    @Test(expected = NullPointerException.class)
    public void testAlgorithmCloning_null() {        
        Algorithm a = new Algorithm((Algorithm)null);
    }
    
    @Test
    public void testAlgorithmCloning_allCopied() {
        
        Algorithm alg_old = rog.nextAlgorithm();       
        Algorithm alg_copied = new Algorithm(alg_old); // Copy-construct!
              
        assertNotNull(alg_copied);
        assertNotNull(alg_copied.getBibtex());
        assertNotNull(alg_copied.getCreatedBy());
        assertNotNull(alg_copied.getId());
        assertNotNull(alg_copied.getMeta());
        assertNotNull(alg_copied.getOntologicalClasses());
        assertNotNull(alg_copied.getParameters());
        assertNotNull(alg_copied.getRanking());
        assertEquals(alg_copied.getMeta().getAudiences(), alg_old.getMeta().getAudiences());
        assertEquals(alg_copied.getMeta().getComments(), alg_old.getMeta().getComments());
        assertEquals(alg_copied.getBibtex(), alg_old.getBibtex());
        Set<BibTeX> bibs = alg_copied.getBibtex();
        for (BibTeX bib: bibs){
            assertTrue(alg_old.getBibtex().contains(bib));
            assertNotNull(bib.getMeta());
            assertNotNull(bib.getAnnotation());
            assertNotNull(bib.getKey());
        }
        
        
    }
    
}
