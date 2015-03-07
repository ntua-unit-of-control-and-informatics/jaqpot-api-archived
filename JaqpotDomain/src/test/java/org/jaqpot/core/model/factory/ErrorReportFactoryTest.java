/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.factory;

import org.jaqpot.core.model.ErrorReport;
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
public class ErrorReportFactoryTest {
    
    public ErrorReportFactoryTest() {
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
    public void testNotFoundError() {
        System.out.println("notFoundError");
        String uriNotFound = "http://whatisthis.org/asdf/123";
        ErrorReport e = ErrorReportFactory.notFoundError(uriNotFound);
        assertEquals(404, (int)e.getHttpStatus());
    }

    @Test
    public void testInternalServerError() {
        System.out.println("internalServerError");
        String code = "asdf";
        String message = "dgdfgd";
        String details = "asdasdasda";
        ErrorReport result = ErrorReportFactory.internalServerError(code, message, details);
        assertEquals("server", result.getActor());
        assertEquals(message, result.getMessage());
        assertEquals(details, result.getDetails());
        assertEquals(500, (int)result.getHttpStatus());
    }

    @Test
    public void testBadRequest() {
        System.out.println("badRequest");
        String message = "sfjsldkjflskd";
        String details = "r3u2oiwfjdlkf";
        ErrorReport result = ErrorReportFactory.badRequest(message, details);
        assertEquals("client", result.getActor());
        assertEquals(400, (int)result.getHttpStatus());
    }

    @Test
    public void testUnauthorizedAccessError() {
        System.out.println("unauthorizedAccessError");
        String uri = "";
        ErrorReport expResult = null;
        ErrorReport result = ErrorReportFactory.unauthorizedAccessError(uri);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testAuthenticationRequired() {
        System.out.println("authenticationRequired");
        ErrorReport expResult = null;
        ErrorReport result = ErrorReportFactory.authenticationRequired();
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testRemoteError() {
        System.out.println("remoteError");
        String remoteUri = "";
        ErrorReport remoteException = null;
        ErrorReport expResult = null;
        ErrorReport result = ErrorReportFactory.remoteError(remoteUri, remoteException);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
    
}
