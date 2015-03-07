/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.factory;

import org.jaqpot.core.model.Task;
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
public class TaskFactoryTest {
    
    public TaskFactoryTest() {
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
    public void testQueuedTask() {
        System.out.println("queuedTask");
        String title = "myTitle";
        String descirption="what a nice task";
        String creator = "someuser";
        Task t = TaskFactory.queuedTask(title, descirption, creator);
        assertEquals(201, (int)t.getHttpStatus());
        assertEquals(creator, t.getCreatedBy());
        assertNotNull(t.getId());
        assertEquals(Task.Status.QUEUED, t.getStatus());
    }
    
}
