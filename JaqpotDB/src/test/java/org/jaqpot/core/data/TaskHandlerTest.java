/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data;

import java.util.ArrayList;
import java.util.List;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author hampos
 */
public class TaskHandlerTest {

    static Task taskPojo, taskPojo2;

    static List<Task> tasks = new ArrayList<>();

    @Mock
    private JaqpotEntityManager em;

    @InjectMocks
    private TaskHandler taskHandler;

    public TaskHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        MetaInfoBuilder metaBuilder = MetaInfoBuilder.builder();
        MetaInfo meta = metaBuilder.
                addComments("task started", "this task does training", "dataset downloaded").
                addDescriptions("this is a very nice task", "oh, and it's very useful too").
                addSources("http://jaqpot.org/algorithm/wonk").build();

        taskPojo = new Task("115a0da8-92cc-4ec4-845f-df643ad607ee");
        taskPojo.setPercentageCompleted(0.95f);
        taskPojo.setDuration(1534l);
        taskPojo.setMeta(meta);
        taskPojo.setHttpStatus(202);
        taskPojo.setStatus(Task.Status.RUNNING);

        taskPojo2 = new Task("215a0da8-92cc-4ec4-845f-df643ad607ee");
        taskPojo2.setPercentageCompleted(0.95f);
        taskPojo2.setDuration(1534l);
        taskPojo2.setMeta(meta);
        taskPojo2.setHttpStatus(202);
        taskPojo2.setStatus(Task.Status.COMPLETED);

        tasks.add(taskPojo);
        tasks.add(taskPojo2);

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        Mockito.when(em.find(Matchers.any(), Matchers.anyMap(), Matchers.anyInt(), Matchers.anyInt())).thenReturn(tasks);
        Mockito.when(em.count(Matchers.any(), Matchers.anyMap())).thenReturn(new Long(tasks.size()));
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of findByUser method, of class TaskHandler.
     */
    @Test
    public void testFindByUserName() {
        String userName = "random-user@jaqpot.org";
        Integer start = 0;
        Integer max = 10;

        List<Task> result = taskHandler.findByUser(userName, start, max);

        Task foundTask = result.get(0);
        Task foundTask2 = result.get(1);

        assertEquals(foundTask, taskPojo);
        assertEquals("not the same ID", taskPojo.getId(), foundTask.getId());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), foundTask.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), foundTask.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), foundTask.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), foundTask.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), foundTask.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), foundTask.getMeta().getDescriptions());

        assertEquals(foundTask2, taskPojo2);
        assertEquals("not the same ID", taskPojo2.getId(), foundTask2.getId());
        assertEquals("not the same percentageComplete", taskPojo2.getPercentageCompleted(), foundTask2.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo2.getDuration(), foundTask2.getDuration());
        assertEquals("not the same HTTP status", taskPojo2.getHttpStatus(), foundTask2.getHttpStatus());
        assertEquals("not the same status", taskPojo2.getStatus(), foundTask2.getStatus());
        assertEquals("not the same comments", taskPojo2.getMeta().getComments(), foundTask2.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo2.getMeta().getDescriptions(), foundTask2.getMeta().getDescriptions());
    }

    @Test
    public void testCountByUserName() {
        String userName = "random-user@jaqpot.org";
        Integer start = 0;
        Integer max = 10;

        Long result = taskHandler.countByUser(userName);

        assertEquals(result, new Long(2));
    }

    @Test
    public void testFindByStatus() {
        String userName = "random-user@jaqpot.org";
        Integer start = 0;
        Integer max = 10;

        List<Task> result = taskHandler.findByStatus(Task.Status.RUNNING, start, max);

        Task foundTask = result.get(0);
        Task foundTask2 = result.get(1);

        assertEquals(foundTask, taskPojo);
        assertEquals("not the same ID", taskPojo.getId(), foundTask.getId());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), foundTask.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), foundTask.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), foundTask.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), foundTask.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), foundTask.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), foundTask.getMeta().getDescriptions());

        assertEquals(foundTask2, taskPojo2);
        assertEquals("not the same ID", taskPojo2.getId(), foundTask2.getId());
        assertEquals("not the same percentageComplete", taskPojo2.getPercentageCompleted(), foundTask2.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo2.getDuration(), foundTask2.getDuration());
        assertEquals("not the same HTTP status", taskPojo2.getHttpStatus(), foundTask2.getHttpStatus());
        assertEquals("not the same status", taskPojo2.getStatus(), foundTask2.getStatus());
        assertEquals("not the same comments", taskPojo2.getMeta().getComments(), foundTask2.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo2.getMeta().getDescriptions(), foundTask2.getMeta().getDescriptions());
    }

    @Test
    public void testCountByStatus() {
        String userName = "random-user@jaqpot.org";
        Integer start = 0;
        Integer max = 10;

        Long result = taskHandler.countByStatus(Task.Status.RUNNING);

        assertEquals(result, new Long(2));
    }

}
