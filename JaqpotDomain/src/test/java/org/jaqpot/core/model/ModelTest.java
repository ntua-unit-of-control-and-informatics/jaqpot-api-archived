/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
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
        assertEquals(m, copy);
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
    
    @Test
    public void testClone_actualModel(){
        Model m = rog.nextModel();
        Object originalActualModel = m.getActualModel();
        Model copy = new Model(m);
        copy.setActualModel(null);
        assertNotNull(m.getActualModel());
        assertEquals(originalActualModel, m.getActualModel());
    }
    
    @Test
    public void testClone_pmmlModel(){
        Model m = rog.nextModel();
        Object originalPMML = m.getPmmlModel();
        Model copy = new Model(m);
        copy.setPmmlModel(null);
        assertNotNull(m.getPmmlModel());
        assertEquals(originalPMML, m.getPmmlModel());
    }
    
    @Test
    public void testClone_pmmlTransformations(){
        Model m = rog.nextModel();
        String originalPMML = m.getPmmlTransformations();
        Model copy = new Model(m);
        copy.setPmmlTransformations(null);
        assertNotNull(m.getPmmlTransformations());
        assertEquals(originalPMML, m.getPmmlTransformations());      
    }
    
    @Test
    public void testClone_createdBy(){
        Model m = rog.nextModel();
        String originalCreatedBy = m.getCreatedBy();
        Model copy = new Model(m);
        copy.setCreatedBy(null);
        assertNotNull(m.getCreatedBy());
        assertEquals(originalCreatedBy, m.getCreatedBy());
    }
    
    @Test
    public void testClone_datasetUri(){
        Model m = rog.nextModel();
        String originalDatasetUri = m.getDatasetUri();
        Model copy = new Model(m);
        copy.setDatasetUri(rog.nextString(10));
        assertFalse(copy.getDatasetUri().equals(m.getDatasetUri()));
        assertEquals(originalDatasetUri, m.getDatasetUri());
    }
    
    @Test
    public void testClone_algorithm(){
        Model m = rog.nextModel();
        Algorithm originalAlgorithm = m.getAlgorithm();
        Model copy = new Model(m);
        copy.setAlgorithm(rog.nextAlgorithm());
        assertFalse(copy.getAlgorithm().equals(m.getAlgorithm()));
        assertEquals(originalAlgorithm, m.getAlgorithm());
    }
    
  
    
}
