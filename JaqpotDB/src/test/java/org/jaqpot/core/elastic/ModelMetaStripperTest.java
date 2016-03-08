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
package org.jaqpot.core.elastic;

import org.jaqpot.core.model.Model;
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
public class ModelMetaStripperTest {

    public ModelMetaStripperTest() {
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
    public void testStrip() {
        ROG rog = new ROG(false);
        Model m = rog.nextModel();
        assertNotNull(m.getBibtex().getMeta());
        
        ModelMetaStripper stripper = new ModelMetaStripper(m);
        Model strippedModel = stripper.strip();
        
        /* First make sure that the original model is not modified */
        assertNotNull(m);
        assertNotNull(m.getActualModel());
        assertNotNull(m.getAlgorithm());
        assertNotNull(m.getAlgorithm().getOntologicalClasses());
        assertNotNull(m.getPmmlModel());
        assertNotNull(m.getPmmlTransformations());
        assertNotNull(m.getParameters());
        assertNotNull(m.getReliability());
        assertNotNull(m.getAlgorithm().getMeta());
        assertNotNull(m.getBibtex().getMeta());
        
        /* Make sure that strippedModel has been stripped */
        assertNotNull(strippedModel);
        assertNull(strippedModel.getActualModel());     
        assertNull(strippedModel.getPmmlModel());     
        assertNull(strippedModel.getPmmlTransformations());                  
        assertNull(strippedModel.getBibtex().getMeta());
        
        /* Make sure certain info are still there in the stripped model */
        assertNotNull(strippedModel.getBibtex());
        assertNotNull(strippedModel.getDatasetUri());
        assertNotNull(strippedModel.getDependentFeatures());
        assertNotNull(strippedModel.getId());
        assertNotNull(strippedModel.getIndependentFeatures());
        assertNotNull(strippedModel.getMeta());
        assertNotNull(strippedModel.getOntologicalClasses());
        assertNotNull(strippedModel.getParameters());
        assertNotNull(strippedModel.getPredictedFeatures());
        assertNotNull(strippedModel.getReliability());
        
        /* 
         * Make sure that stripped model is the same as model 
         * regarding non-stripped fields.
         */
        assertEquals(m.getId(), strippedModel.getId());
        assertEquals(m.getDependentFeatures(), strippedModel.getDependentFeatures());
        assertEquals(m.getIndependentFeatures(), strippedModel.getIndependentFeatures());
        assertEquals(m.getPredictedFeatures(), strippedModel.getPredictedFeatures());
        assertEquals(m.getAlgorithm(), strippedModel.getAlgorithm());
        assertEquals(m.getAlgorithm().getMeta().getDate(), strippedModel.getAlgorithm().getMeta().getDate());
       
    }
    
    @Test(expected = NullPointerException.class)
    public void testStrip_nullEntity() {
        ModelMetaStripper stripper = new ModelMetaStripper(null);
    }

}
