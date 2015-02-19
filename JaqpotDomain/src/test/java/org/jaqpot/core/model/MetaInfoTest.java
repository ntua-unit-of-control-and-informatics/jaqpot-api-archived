/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

import java.util.Date;
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
public class MetaInfoTest {

    static ROG rog = new ROG(false);

    public MetaInfoTest() {
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
    public void testCloning() {
        MetaInfo meta = rog.nextMeta();
        MetaInfo meta_copy = new MetaInfo(meta);
        
        assertNotNull(meta_copy);

        assertEquals(meta.getAudiences(), meta_copy.getAudiences());
        assertEquals(meta.getComments(), meta_copy.getComments());
        assertEquals(meta.getContributors(), meta_copy.getContributors());
        assertEquals(meta.getCreators(), meta_copy.getCreators());
        assertNotNull(meta_copy.getDate());
        assertEquals(meta.getDate(), meta_copy.getDate());
        assertEquals(meta.getDescriptions(), meta_copy.getDescriptions());
        assertEquals(meta.getHasSources(), meta_copy.getHasSources());
        assertEquals(meta.getIdentifiers(), meta_copy.getIdentifiers());
        assertEquals(meta.getPublishers(), meta_copy.getPublishers());
        assertEquals(meta.getRights(), meta_copy.getRights());
        assertEquals(meta.getSameAs(), meta_copy.getSameAs());
        assertEquals(meta.getSeeAlso(), meta_copy.getSeeAlso());
        assertEquals(meta.getSubjects(), meta_copy.getSubjects());
        assertEquals(meta.getTitles(), meta_copy.getTitles());                
    }
    
    @Test
    public void testCloning_deepCopy() {
        MetaInfo meta = rog.nextMeta();
        MetaInfo meta_copy = new MetaInfo(meta);
        
        
        meta.setDate(null);
        meta.setTitles(null);

        assertNotNull(meta_copy.getTitles());        
        assertNotNull(meta_copy.getDate());
                      
    }
    
    @Test(expected = NullPointerException.class)
    public void testCloning_null() {
        MetaInfo meta_copy = new MetaInfo(null);                        
    }
    
    @Test
    public void testCloning_emptyCopy() {
        MetaInfo m = new MetaInfo();
        MetaInfo copy = new MetaInfo(m);
        assertNull(copy.getAudiences());
        assertNull(copy.getDate());
        assertNull(copy.getTitles());
    }
    
    @Test
    public void testCloning_date() {
        Date originalDate = new Date();
        MetaInfo m = new MetaInfo();
        m.setDate(originalDate);
        MetaInfo copy = new MetaInfo(m);
        assertEquals(m.getDate(), copy.getDate());
        
        copy.setDate(new Date(System.currentTimeMillis() + 10000));
        assertEquals(originalDate, m.getDate());
        
    }

}
