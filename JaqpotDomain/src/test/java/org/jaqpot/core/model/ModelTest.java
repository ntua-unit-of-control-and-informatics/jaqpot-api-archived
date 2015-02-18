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

import java.util.ArrayList;
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
public class ModelTest {
    
    static ROG rog = new ROG(false);
    
    public ModelTest() {
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
    public void testClone() {        
        Model m = rog.nextModel();
        assertNotNull(m.getBibtex());
        assertNotNull(m.getBibtex().getMeta());
        
        Model copy = new Model(m);
        
        copy.getBibtex().setMeta(null);
        
        assertNotNull(m.getBibtex().getMeta());
    }
    
    @Test
    public void testClone_nullDependent() {        
        Model m = rog.nextModel();
        m.setDependentFeatures(null);
                        
        Model copy = new Model(m);
        
        assertNull(copy.getDependentFeatures());
    }
    
    @Test
    public void testClone_nullInependent() {        
        Model m = rog.nextModel();
        m.setIndependentFeatures(null);
                        
        Model copy = new Model(m);
        
        assertNull(copy.getIndependentFeatures());
        copy.setIndependentFeatures(new ArrayList<>());
        assertNull(m.getIndependentFeatures());
    }
    
    @Test
    public void testClone_emptyModel() {  
        Model m = new Model();
        Model copy = new Model(m);
        assertNull(copy.getActualModel());
        assertNull(copy.getId());
        assertNull(copy.getOntologicalClasses());        
    }
    
    
}
