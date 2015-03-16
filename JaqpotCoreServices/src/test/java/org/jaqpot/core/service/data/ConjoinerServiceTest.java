/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.data;

import java.util.TreeMap;
import javax.ejb.embeddable.EJBContainer;
import org.jaqpot.core.service.dto.dataset.DataEntry;
import org.jaqpot.core.service.dto.dataset.Dataset;
import org.jaqpot.core.service.dto.study.Effect;
import org.jaqpot.core.service.dto.study.Result;
import org.jaqpot.core.service.dto.study.Studies;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Georgios Drakakis
 */
public class ConjoinerServiceTest {
    
    @InjectMocks
    ConjoinerService service;
    
    public ConjoinerServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of prepareDataset method, of class ConjoinerService.
     */
    //@Test
    public void testPrepareDataset() throws Exception {
        System.out.println("prepareDataset");
        String bundleURI = "";
        String subjectId = "";
        EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        ConjoinerService instance = (ConjoinerService)container.getContext().lookup("java:global/classes/ConjoinerService");
        Dataset expResult = null;
        Dataset result = instance.prepareDataset(bundleURI, subjectId);
        assertEquals(expResult, result);
        container.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createDataEntry method, of class ConjoinerService.
     */
    //@Test
    public void testCreateDataEntry() throws Exception {
        System.out.println("createDataEntry");
        Studies studies = null;
        EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        ConjoinerService instance = (ConjoinerService)container.getContext().lookup("java:global/classes/ConjoinerService");
        DataEntry expResult = null;
        DataEntry result = instance.createDataEntry(studies);
        assertEquals(expResult, result);
        container.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * BEGIN TESTS  
     */
    @Test
    public void testCalculateValue() throws Exception {
        Effect effect = new Effect();
        Result result = new Result();
        result.setLoValue(5);
        result.setLoQualifier("=");
        result.setUpValue(7);
        result.setUpQualifier("=");
        effect.setResult(result);    
        
        // qualifiers ok, error not existing, calculate avg(5,7)
        Object expResult = 6.0;
        Object response = service.calculateValue(effect);
        assertEquals(expResult, response);
    }
    
    @Test
    public void testCalculateValueWithGoodError() throws Exception {
        Effect effect = new Effect();
        Result result = new Result();
        result.setLoValue(5);
        result.setLoQualifier("=");
        result.setUpValue(7);
        result.setUpQualifier("=");
        result.setErrorValue(1);
        effect.setResult(result);   
        
        // qualifiers ok, error ok, calculate avg(5,7)
        Object expResult = 6.0;
        Object response = service.calculateValue(effect);
        assertEquals(expResult, response);
    }
    
    @Test
    public void testCalculateValueWithBadError() throws Exception {
        Effect effect = new Effect();
        Result result = new Result();
        result.setLoValue(5);
        result.setLoQualifier("=");
        result.setUpValue(7);
        result.setUpQualifier("=");
        result.setErrorValue(2);
        effect.setResult(result);     
        
        // qualifiers ok, error too big, return null
        Object expResult = null;
        Object response = service.calculateValue(effect);
        assertEquals(expResult, response);
    }
    
    @Test
    public void testCalculateValueOnlyLo() throws Exception {
        Effect effect = new Effect();
        //effect.setEndpoint("not_relevant");
        Result result = new Result();
        result.setLoValue(5);
        result.setLoQualifier("<");
        result.setErrorValue(2);
        effect.setResult(result);
        
        // only loValue exists, qualifier ok, return loValue
        Object expResult =5.0;
        Object response = service.calculateValue(effect);
        assertEquals(expResult, response);
    }
    
    @Test
    public void testCalculateValueOnlyUp() throws Exception {
        Effect effect = new Effect();
        //effect.setEndpoint("not_relevant");
        Result result = new Result();
        result.setUpValue(5);
        result.setUpQualifier(">=");
        result.setErrorValue(2);
        effect.setResult(result);
        
        // only upValue exists, qualifier ok, return upValue
        Object expResult =5.0;
        Object response = service.calculateValue(effect);
        assertEquals(expResult, response);
    }
    
    @Test
    public void testCalculateValueWithInappropriateQualifiers() throws Exception {
        Effect effect = new Effect();
        Result result = new Result();
        
        result.setLoValue(5);
        result.setLoQualifier("");
        result.setUpValue(7);
        result.setUpQualifier("");
        result.setErrorValue(1.5);
        
        // qualifiers in disallowed list, return null
        effect.setResult(result);
        Object expResult = null;
        Object response = service.calculateValue(effect);
        assertEquals(expResult, response);
    }

    
    /**
     * Test of getRelativeURI method, of class ConjoinerService.
     */
    //@Test
    public void testGetRelativeURI() throws Exception {
        System.out.println("getRelativeURI");
        String name = "";
        String topcategory = "";
        String endpointcategory = "";
        String identifier = "";
        Boolean extendedURI = null;
        String guideline = "";
        EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        ConjoinerService instance = (ConjoinerService)container.getContext().lookup("java:global/classes/ConjoinerService");
        String expResult = "";
        String result = instance.getRelativeURI(name, topcategory, endpointcategory, identifier, extendedURI, guideline);
        assertEquals(expResult, result);
        container.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createHashedIdentifier method, of class ConjoinerService.
     */
    //@Test
    public void testCreateHashedIdentifier() throws Exception {
        System.out.println("createHashedIdentifier");
        String name = "";
        String units = "";
        String conditions = "";
        EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        ConjoinerService instance = (ConjoinerService)container.getContext().lookup("java:global/classes/ConjoinerService");
        String expResult = "";
        String result = instance.createHashedIdentifier(name, units, conditions);
        assertEquals(expResult, result);
        container.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
