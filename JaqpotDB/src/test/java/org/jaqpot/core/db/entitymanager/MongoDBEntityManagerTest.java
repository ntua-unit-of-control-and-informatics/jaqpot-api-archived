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
package org.jaqpot.core.db.entitymanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.bson.Document;
import org.jaqpot.core.data.serialize.JacksonJSONSerializer;
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
import org.mockito.invocation.InvocationOnMock;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class MongoDBEntityManagerTest {

    @Mock
    private JacksonJSONSerializer serializer;

    ObjectMapper mapper;
    Task taskPojo;
    Task taskPojo2;
    String taskJSON;

    private static final MongoClient mongoClient;
    private static final Properties dbProperties;

    static {
        ClassLoader classLoader = MongoDBEntityManager.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream("config/db.properties");
        String dbHost = "localhost"; // Default DB host
        int dbPort = 27017; // Default DB port
        dbProperties = new Properties();

        try {
            dbProperties.load(is);
            dbHost = dbProperties.getProperty("db.host", dbHost);
            dbPort = Integer.parseInt(dbProperties.getProperty("db.port", Integer.toString(dbPort)));
        } catch (IOException ex) {
            String errorMessage = "No DB properties file found!";
            System.out.println(errorMessage);
        } finally {
        }
        mongoClient = new MongoClient(dbHost, dbPort);
    }

    @InjectMocks
    private MongoDBEntityManager em;

    public MongoDBEntityManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {

        mongoClient.dropDatabase("test");

        MetaInfoBuilder metaBuilder = MetaInfoBuilder.builder();
        MetaInfo meta = metaBuilder.
                addComments("task started", "this task does training", "dataset downloaded").
                addDescriptions("this is a very nice task", "oh, and it's very useful too").
                addSources("http://jaqpot.org/algorithm/wonk").build();

        taskPojo = new Task("115a0da8-92cc-4ec4-845f-df643ad607ee");
        taskPojo.setCreatedBy("random-user@jaqpot.org");
        taskPojo.setPercentageCompleted(0.95f);
        taskPojo.setDuration(1534l);
        taskPojo.setMeta(meta);
        taskPojo.setHttpStatus(202);
        taskPojo.setStatus(Task.Status.RUNNING);

        taskPojo2 = new Task("215a0da8-92cc-4ec4-845f-df643ad607ee");
        taskPojo2.setCreatedBy("random-user@jaqpot.org");
        taskPojo2.setPercentageCompleted(0.95f);
        taskPojo2.setDuration(1534l);
        taskPojo2.setMeta(meta);
        taskPojo2.setHttpStatus(202);
        taskPojo2.setStatus(Task.Status.COMPLETED);

        mapper = new ObjectMapper();
        taskJSON = mapper.writeValueAsString(taskPojo);

        MockitoAnnotations.initMocks(this);
        Mockito.when(serializer.write(Matchers.any())).thenAnswer((InvocationOnMock invocation) -> {
            Object obj = invocation.getArguments()[0];
            return mapper.writeValueAsString(obj);
        });
        Mockito.when(serializer.parse(Matchers.anyString(), Matchers.any(Class.class))).thenAnswer((InvocationOnMock invocation) -> {
            String pojo = (String) invocation.getArguments()[0];
            Class clazz = (Class) invocation.getArguments()[1];
            return mapper.readValue(pojo, clazz);
        });

        em.setDatabase("test");

    }

    @After
    public void tearDown() {
        em.close();
    }

    /**
     * Writes a task to mongodb and retrieves it by ID.
     *
     * @throws IOException
     */
    @Test
    public void testSaveTask() throws IOException {
        /* Persist entity using EntityManager */
        em.persist(taskPojo);

        //Now find the object in the database:
        BasicDBObject query = new BasicDBObject("_id", taskPojo.getId()); // Find with ID

        // Now find it in the DB...
        DB db = mongoClient.getDB("test");
        DBCollection coll = db.getCollection(taskPojo.getClass().getSimpleName());
        DBCursor cursor = coll.find(query);

        assertTrue("nothing found", cursor.hasNext());
        DBObject retrieved = cursor.next();

        Task objFromDB = (Task) mapper.readValue(retrieved.toString(), Task.class);

        assertEquals(taskPojo, objFromDB);
        assertEquals("not the same ID", taskPojo.getId(), objFromDB.getId());
        assertEquals("not the same createdBy", taskPojo.getCreatedBy(), objFromDB.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), objFromDB.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), objFromDB.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), objFromDB.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), objFromDB.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), objFromDB.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), objFromDB.getMeta().getDescriptions());
    }

    @Test
    public void testFindTask() throws UnknownHostException {
        DB db = mongoClient.getDB("test");
        DBCollection coll = db.getCollection(taskPojo.getClass().getSimpleName());
        DBObject taskDBObj = (DBObject) JSON.parse(taskJSON);
        coll.insert(taskDBObj);

        Task foundTask = em.find(Task.class, taskPojo.getId());

        assertEquals(foundTask, taskPojo);
        assertEquals("not the same ID", taskPojo.getId(), foundTask.getId());
        assertEquals("not the same createdBy", taskPojo.getCreatedBy(), foundTask.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), foundTask.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), foundTask.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), foundTask.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), foundTask.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), foundTask.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), foundTask.getMeta().getDescriptions());
    }

    @Test
    public void testMergeTask() throws UnknownHostException, IOException {
        DB db = mongoClient.getDB("test");
        DBCollection coll = db.getCollection(taskPojo.getClass().getSimpleName());
        DBObject taskDBObj = (DBObject) JSON.parse(taskJSON);
        coll.insert(taskDBObj);

        MetaInfoBuilder metaBuilder = MetaInfoBuilder.builder();
        MetaInfo meta = metaBuilder.
                addComments("task started", "this task does training", "dataset downloaded").
                addDescriptions("this is a very cool task", "oh, and it's super useful too").
                addSources("http://jaqpot.org/algorithm/wonk").build();

        Task mergeTask = new Task("115a0da8-92cc-4ec4-845f-df643ad607ee");
        mergeTask.setCreatedBy("random-user@jaqpot.org");
        mergeTask.setPercentageCompleted(0.95f);
        mergeTask.setDuration(1534l);
        mergeTask.setMeta(meta);
        mergeTask.setHttpStatus(202);
        mergeTask.setStatus(Task.Status.RUNNING);

        Task oldTask = em.merge(mergeTask);

        BasicDBObject query = new BasicDBObject("_id", taskPojo.getId()); // Find with ID
        DBCursor cursor = coll.find(query);

        assertTrue("nothing found", cursor.hasNext());
        DBObject retrieved = cursor.next();

        Task objFromDB = (Task) mapper.readValue(retrieved.toString(), Task.class);

        assertEquals(mergeTask, objFromDB);
        assertEquals("not the same ID", mergeTask.getId(), objFromDB.getId());
        assertEquals("not the same createdBy", mergeTask.getCreatedBy(), objFromDB.getCreatedBy());
        assertEquals("not the same percentageComplete", mergeTask.getPercentageCompleted(), objFromDB.getPercentageCompleted());
        assertEquals("not the same duration", mergeTask.getDuration(), objFromDB.getDuration());
        assertEquals("not the same HTTP status", mergeTask.getHttpStatus(), objFromDB.getHttpStatus());
        assertEquals("not the same status", mergeTask.getStatus(), objFromDB.getStatus());
        assertEquals("not the same comments", mergeTask.getMeta().getComments(), objFromDB.getMeta().getComments());
        assertEquals("not the same descriptions", mergeTask.getMeta().getDescriptions(), objFromDB.getMeta().getDescriptions());

        assertEquals(oldTask, taskPojo);
        assertEquals("not the same ID", taskPojo.getId(), oldTask.getId());
        assertEquals("not the same createdBy", taskPojo.getCreatedBy(), oldTask.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), oldTask.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), oldTask.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), oldTask.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), oldTask.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), oldTask.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), oldTask.getMeta().getDescriptions());

    }

    @Test
    public void testFindALl() throws JsonProcessingException {
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> coll = db.getCollection(taskPojo.getClass().getSimpleName());
        Document taskDBObj = Document.parse(mapper.writeValueAsString(taskPojo));
        Document taskDBObj2 = Document.parse(mapper.writeValueAsString(taskPojo2));
        coll.insertOne(taskDBObj);
        coll.insertOne(taskDBObj2);

        List<Task> result = em.findAll(Task.class, 0, 5);
        Task foundTask = result.get(0);
        Task foundTask2 = result.get(1);

        assertEquals(foundTask, taskPojo);
        assertEquals("not the same ID", taskPojo.getId(), foundTask.getId());
        assertEquals("not the same createdBy", taskPojo.getCreatedBy(), foundTask.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), foundTask.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), foundTask.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), foundTask.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), foundTask.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), foundTask.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), foundTask.getMeta().getDescriptions());

        assertEquals(foundTask2, taskPojo2);
        assertEquals("not the same ID", taskPojo2.getId(), foundTask2.getId());
        assertEquals("not the same createdBy", taskPojo2.getCreatedBy(), foundTask2.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo2.getPercentageCompleted(), foundTask2.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo2.getDuration(), foundTask2.getDuration());
        assertEquals("not the same HTTP status", taskPojo2.getHttpStatus(), foundTask2.getHttpStatus());
        assertEquals("not the same status", taskPojo2.getStatus(), foundTask2.getStatus());
        assertEquals("not the same comments", taskPojo2.getMeta().getComments(), foundTask2.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo2.getMeta().getDescriptions(), foundTask2.getMeta().getDescriptions());

    }

    @Test
    public void testRemove() throws JsonProcessingException {
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> coll = db.getCollection(taskPojo.getClass().getSimpleName());
        Document taskDBObj = Document.parse(mapper.writeValueAsString(taskPojo));
        coll.insertOne(taskDBObj);

        em.remove(taskPojo);

        MongoCollection<Document> collection = db.getCollection("Task");
        Document foundTask = collection.find(new Document("_id", taskPojo.getId())).first();
        assertNull(foundTask);

    }

    @Test
    public void testFindByProperties() throws JsonProcessingException {
        System.out.println(taskJSON);
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> coll = db.getCollection(taskPojo.getClass().getSimpleName());
        Document taskDBObj = Document.parse(taskJSON);
        coll.insertOne(taskDBObj);

        Map<String, Object> properties = new HashMap<>();
        List<String> comments = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        List<String> sources = new ArrayList<>();

        comments.add("task started");
        comments.add("dataset downloaded");
        comments.add("this task does training");

        descriptions.add("this is a very nice task");
        descriptions.add("oh, and it's very useful too");

        sources.add("http://jaqpot.org/algorithm/wonk");

        properties.put("meta.hasSources", sources);
        properties.put("meta.comments", comments);
        properties.put("meta.descriptions", descriptions);

        properties.put("duration", 1534l);
        List<Task> result = em.find(Task.class, properties, 0, 5);

        Task foundTask = result.get(0);
        assertEquals(foundTask, taskPojo);
        assertEquals("not the same ID", taskPojo.getId(), foundTask.getId());
        assertEquals("not the same createdBy", taskPojo.getCreatedBy(), foundTask.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), foundTask.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), foundTask.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), foundTask.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), foundTask.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), foundTask.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), foundTask.getMeta().getDescriptions());

        properties = new HashMap<>();
        properties.put("duration", 1535l);
        result = em.find(Task.class, properties, 0, 5);

        assertTrue(result.isEmpty());

    }

    @Test
    public void testFindByIds() throws JsonProcessingException {
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> coll = db.getCollection(taskPojo.getClass().getSimpleName());
        Document taskDBObj = Document.parse(mapper.writeValueAsString(taskPojo));
        Document taskDBObj2 = Document.parse(mapper.writeValueAsString(taskPojo2));
        coll.insertOne(taskDBObj);
        coll.insertOne(taskDBObj2);

        List<String> keys = new ArrayList<>();
        keys.add("115a0da8-92cc-4ec4-845f-df643ad607ee");
        keys.add("215a0da8-92cc-4ec4-845f-df643ad607ee");

        List<String> fields = new ArrayList<>();
        fields.add("duration");
        fields.add("status");

        List<Task> result = em.find(Task.class, keys, fields);

        Task foundTask = result.get(0);
        Task foundTask2 = result.get(1);

        assertEquals(foundTask, taskPojo);
        assertEquals("not the same ID", taskPojo.getId(), foundTask.getId());
        assertNull(foundTask.getCreatedBy());
        assertNull(foundTask.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), foundTask.getDuration());
        assertEquals("not the same status", taskPojo.getStatus(), foundTask.getStatus());

        assertEquals(foundTask2, taskPojo2);
        assertEquals("not the same ID", taskPojo2.getId(), foundTask2.getId());
        assertNull(foundTask2.getCreatedBy());
        assertNull(foundTask2.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo2.getDuration(), foundTask2.getDuration());
        assertEquals("not the same status", taskPojo2.getStatus(), foundTask2.getStatus());

    }

    @Test
    public void testCount() throws JsonProcessingException {
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> coll = db.getCollection(taskPojo.getClass().getSimpleName());
        Document taskDBObj = Document.parse(mapper.writeValueAsString(taskPojo));
        Document taskDBObj2 = Document.parse(mapper.writeValueAsString(taskPojo2));
        coll.insertOne(taskDBObj);
        coll.insertOne(taskDBObj2);

        Map<String, Object> properties = new HashMap<>();
        List<String> comments = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        List<String> sources = new ArrayList<>();

        comments.add("task started");
        comments.add("dataset downloaded");
        comments.add("this task does training");

        descriptions.add("this is a very nice task");
        descriptions.add("oh, and it's very useful too");

        sources.add("http://jaqpot.org/algorithm/wonk");

        properties.put("meta.hasSources", sources);
        properties.put("meta.comments", comments);
        properties.put("meta.descriptions", descriptions);

        properties.put("duration", 1534l);
        Long result = em.count(Task.class, properties);

        assertEquals(result, new Long(2));
    }

    @Test
    public void testCountAll() throws JsonProcessingException {
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> coll = db.getCollection(taskPojo.getClass().getSimpleName());
        Document taskDBObj = Document.parse(mapper.writeValueAsString(taskPojo));
        Document taskDBObj2 = Document.parse(mapper.writeValueAsString(taskPojo2));
        coll.insertOne(taskDBObj);
        coll.insertOne(taskDBObj2);

        Map<String, Object> properties = new HashMap<>();
        List<String> comments = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        List<String> sources = new ArrayList<>();

        comments.add("task started");
        comments.add("dataset downloaded");
        comments.add("this task does training");

        descriptions.add("this is a very nice task");
        descriptions.add("oh, and it's very useful too");

        sources.add("http://jaqpot.org/algorithm/wonk");

        properties.put("meta.hasSources", sources);
        properties.put("meta.comments", comments);
        properties.put("meta.descriptions", descriptions);

        properties.put("duration", 1534l);
        Long result = em.countAll(Task.class);

        assertEquals(result, new Long(2));
    }

}
