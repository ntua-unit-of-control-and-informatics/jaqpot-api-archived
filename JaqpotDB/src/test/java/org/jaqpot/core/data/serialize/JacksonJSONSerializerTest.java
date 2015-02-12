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
package org.jaqpot.core.data.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class JacksonJSONSerializerTest {

    private static JacksonJSONSerializer instance;
    private static final String uuid= UUID.randomUUID().toString();
    private static Task taskPojo;
    private static String taskJSON = "{\"meta\":{\"comments\":[\"dataset downloaded\",\"task started\",\"this task does training\"],"
                + "\"descriptions\":[\"oh, and it's very useful too\",\"this is a very nice task\"],"
                + "\"hasSources\":[\"http://jaqpot.org/algorithm/wonk\"]},\"percentageCompleted\":0.95,"
                + "\"httpStatus\":202,\"createdBy\":\"random-user@jaqpot.org\",\"duration\":1534,"
                + "\"status\":\"RUNNING\",\"_id\":\"%s\"}";
    private BufferedReader reader;
    private BufferedOutputStream out;

    public JacksonJSONSerializerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        instance = new JacksonJSONSerializer();
        taskJSON = String.format(taskJSON, uuid);
        System.out.println(taskJSON);
        MetaInfoBuilder metaBuilder = MetaInfoBuilder.builder();
        MetaInfo meta = metaBuilder.
                addComments("task started", "this task does training", "dataset downloaded").
                addDescriptions("this is a very nice task", "oh, and it's very useful too").
                addSources("http://jaqpot.org/algorithm/wonk").build();
        taskPojo = new Task(uuid);
        taskPojo.setCreatedBy("random-user@jaqpot.org");
        taskPojo.setPercentageCompleted(0.95f);
        taskPojo.setDuration(1534l);
        taskPojo.setMeta(meta);
        taskPojo.setHttpStatus(202);
        taskPojo.setStatus(Task.Status.RUNNING);

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void before() throws IOException {
        PipedInputStream pipeInput = new PipedInputStream();
        reader = new BufferedReader(new InputStreamReader(pipeInput));
        out = new BufferedOutputStream(new PipedOutputStream(pipeInput));
    }

    @After
    public void after() {

    }

    /**
     * Test of write method, of class JacksonJSONSerializer.
     * @throws java.io.IOException
     */
    @Test
    public void testWrite_Object_OutputStream() throws IOException {
        Object entity = taskPojo;
        // OutputStream out = Mockito.mock(OutputStream.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        instance.write(entity, baos);
        byte[] bytes = baos.toByteArray();
        assertNotNull("ByteArrayOutputStream returned a null byte[]",bytes);
        assertTrue("Nothing tunneled to the output stream",bytes.length>0);
        String jsonString = new String(bytes,Charset.defaultCharset());
        assertNotNull("jsonString generated from OutputStream is null", jsonString);
        assertTrue("_id not found in serialized string",jsonString.contains("\"_id\":\""+uuid+"\""));
        assertTrue("percentageCompleted not found in serialized string",jsonString.contains("\"percentageCompleted\":"+taskPojo.getPercentageCompleted()));
        assertTrue("createdBy not found in serialized string",jsonString.contains("\"createdBy\":\""+taskPojo.getCreatedBy()+"\""));
        assertTrue("status not found in serialized string",jsonString.contains("\"status\":\""+taskPojo.getStatus()+"\""));
        assertTrue("duration not found in serialized string",jsonString.contains("\"duration\":"+taskPojo.getDuration()));
        assertTrue("meta not found", jsonString.contains("\"meta\":{"));        
    }

    /**
     * Test of write method, of class JacksonJSONSerializer.
     */
    @Test
    public void testWrite_Object_Writer() throws IOException {
        Object entity = taskPojo;
        Writer writer = new OutputStreamWriter(out);
        instance.write(entity, writer);
        assertEquals(taskJSON, reader.readLine());
    }

    /**
     * Test of write method, of class JacksonJSONSerializer.
     */
    @Test
    public void testWrite_Object() {
        Object entity = taskPojo;
        String expResult = "";
        try {
            expResult = new ObjectMapper().writeValueAsString(entity);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        String result = instance.write(entity);
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class JacksonJSONSerializer.
     */
    @Test
    public void testParse_String_Class() {

        Task expResult = taskPojo;
        Task result = instance.parse(taskJSON, Task.class);
        assertEquals(expResult, result);
        assertEquals("not the same ID", expResult.getId(), result.getId());
        assertEquals("not the same createdBy", expResult.getCreatedBy(), result.getCreatedBy());
        assertEquals("not the same percentageComplete", expResult.getPercentageCompleted(), result.getPercentageCompleted());
        assertEquals("not the same duration", expResult.getDuration(), result.getDuration());
        assertEquals("not the same HTTP status", expResult.getHttpStatus(), result.getHttpStatus());
        assertEquals("not the same status", expResult.getStatus(), result.getStatus());
        assertEquals("not the same comments", expResult.getMeta().getComments(), result.getMeta().getComments());
        assertEquals("not the same descriptions", expResult.getMeta().getDescriptions(), result.getMeta().getDescriptions());
    }

    /**
     * Test of parse method, of class JacksonJSONSerializer.
     */
    @Test
    public void testParse_InputStream_Class() {
        Task expResult = taskPojo;
        InputStream stream = new ByteArrayInputStream(taskJSON.getBytes(StandardCharsets.UTF_8));
        Task result = instance.parse(stream, Task.class);
        assertEquals(expResult, result);
        assertEquals("not the same ID", expResult.getId(), result.getId());
        assertEquals("not the same createdBy", expResult.getCreatedBy(), result.getCreatedBy());
        assertEquals("not the same percentageComplete", expResult.getPercentageCompleted(), result.getPercentageCompleted());
        assertEquals("not the same duration", expResult.getDuration(), result.getDuration());
        assertEquals("not the same HTTP status", expResult.getHttpStatus(), result.getHttpStatus());
        assertEquals("not the same status", expResult.getStatus(), result.getStatus());
        assertEquals("not the same comments", expResult.getMeta().getComments(), result.getMeta().getComments());
        assertEquals("not the same descriptions", expResult.getMeta().getDescriptions(), result.getMeta().getDescriptions());
    }

}
