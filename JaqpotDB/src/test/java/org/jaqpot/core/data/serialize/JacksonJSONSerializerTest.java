/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data.serialize;

import java.io.OutputStream;
import java.io.Writer;
import org.jaqpot.core.model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hampos
 */
public class JacksonJSONSerializerTest {
    
    private static JacksonJSONSerializer instance;
    
    public JacksonJSONSerializerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        instance = new JacksonJSONSerializer();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of write method, of class JacksonJSONSerializer.
     */
    @Test
    public void testWrite_Object_OutputStream() {
        System.out.println("write");
        Object entity = null;
        OutputStream out = null;
        instance.write(entity, out);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class JacksonJSONSerializer.
     */
    @Test
    public void testWrite_Object_Writer() {
        System.out.println("write");
        Object entity = null;
        Writer writer = null;
        instance.write(entity, writer);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class JacksonJSONSerializer.
     */
    @Test
    public void testWrite_Object() {
        System.out.println("write");
        User entity = new User("1");
        String expResult = "{\"_id\":\"1\"}";
        String result = instance.write(entity);
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class JacksonJSONSerializer.
     */
    @Test
    public void testParse_String_Class() {
        System.out.println("parse");
        Object expResult = null;
        Object result = instance.parse("",null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class JacksonJSONSerializer.
     */
    @Test
    public void testParse_InputStream_Class() {
        System.out.println("parse");
        Object expResult = null;
        Object result = instance.parse("",null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
