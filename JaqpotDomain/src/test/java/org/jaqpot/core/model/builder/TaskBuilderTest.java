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
package org.jaqpot.core.model.builder;

import org.jaqpot.core.model.Task;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class TaskBuilderTest {

    public TaskBuilderTest() {
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
    public void testBuildTask() {
        System.out.println("buildTask");
        TaskBuilder instance = TaskBuilder.builderRandomId();
        String taskCreator = "some.user@jaqpot.org";
        String taskTitle = "Task for Model 2324123";
        Task queuedTask = instance.addProgressComments("Task is queued", "Task is about to start").
                addTitles(taskTitle).
                setCurrentDate().
                setDuration(0L).
                setHttpStatus(202).
                setPercentageCompleted(0).
                setStatus(Task.Status.QUEUED).build();
        assertNotNull(queuedTask);
        assertEquals(Task.Status.QUEUED, queuedTask.getStatus());
        assertNotNull(queuedTask.getMeta());
        assertTrue(queuedTask.getMeta().getTitles().contains(taskTitle));
        assertEquals(1, queuedTask.getMeta().getTitles().size());
    }

    @Test
    public void testBuildTask_nullComments() {
        System.out.println("buildTaskNullComments");
        TaskBuilder instance = TaskBuilder.builderRandomId();
        String[] x = null;
        Task queuedTask = instance.addProgressComments(x).build();
        assertNotNull(queuedTask);
        assertNull(queuedTask.getMeta());
    }

    @Test
    public void testBuildTask_nullComments2() {
        System.out.println("buildTaskNullComments2");
        TaskBuilder instance = TaskBuilder.builderRandomId();
        String[] x = null;
        String description = "abcd";
        Task queuedTask = instance.
                addDescription(description).
                addProgressComments(x).build();
        assertNotNull(queuedTask);
        assertNotNull(queuedTask.getMeta());
        assertTrue(queuedTask.getMeta().getDescriptions().contains(description));
    }

}
