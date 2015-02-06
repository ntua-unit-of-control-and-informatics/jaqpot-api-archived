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
package org.jaqpot.core.db.entitymanager;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.util.UUID;
import org.codehaus.jackson.map.ObjectMapper;
import org.jaqpot.core.data.serialize.EntityJSONSerializer;
import org.jaqpot.core.model.JaqpotEntity;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class MongoDBEntityManagerTest {

    @Mock
    private EntityJSONSerializer serializer;
    @Mock
    private JaqpotEntity entity;

    ObjectMapper mapper;
    Task taskPojo;

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
        MetaInfoBuilder metaBuilder = MetaInfoBuilder.builder();
        MetaInfo meta = metaBuilder.
                addComments("task started", "this task does training", "dataset downloaded").
                addDescriptions("this is a very nice task", "oh, and it's very useful too").
                addSources("http://jaqpot.org/algorithm/wonk").build();

        taskPojo = new Task("7b969020-1b86-4541-bc64-fb5027217043");
        taskPojo.setCreatedBy("random-user@jaqpot.org");
        taskPojo.setPercentageCompleted(0.95f);
        taskPojo.setDuration(1534l);
        taskPojo.setMeta(meta);
        taskPojo.setHttpStatus(202);
        taskPojo.setStatus(Task.Status.RUNNING);

        mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(taskPojo);
        MockitoAnnotations.initMocks(this);
        Mockito.when(serializer.write(taskPojo)).thenReturn(json);
    }

    @After
    public void tearDown() {
    }

    /**
     * Writes a task to mongodb and retrieves it by ID.
     *
     * @throws IOException
     */
    @Test
    public void testSaveTask() throws IOException {
        Object obj = taskPojo;
        System.out.println(obj.getClass().getSimpleName());
        /* Initializes a Persistor with default DB configuration */
        //    MongoDBEntityManager em = new MongoDBEntityManager();
        em.persist(taskPojo);

        //Now find the object in the database:
        BasicDBObject query = new BasicDBObject("_id", taskPojo.getId()); // Find with ID

        // Now find it in the DB...
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("test");
        DBCollection coll = db.getCollection("tasks");
        DBCursor cursor = coll.find(query);

        assertTrue("nothing found", cursor.hasNext());
        DBObject retrieved = cursor.next();

        Task objFromDB = (Task) mapper.readValue(retrieved.toString(), Task.class);

        assertEquals("not the same ID", taskPojo.getId(), objFromDB.getId());
        assertEquals("not the same createdBy", taskPojo.getCreatedBy(), objFromDB.getCreatedBy());
        assertEquals("not the same percentageComplete", taskPojo.getPercentageCompleted(), objFromDB.getPercentageCompleted());
        assertEquals("not the same duration", taskPojo.getDuration(), objFromDB.getDuration());
        assertEquals("not the same HTTP status", taskPojo.getHttpStatus(), objFromDB.getHttpStatus());
        assertEquals("not the same status", taskPojo.getStatus(), objFromDB.getStatus());
        assertEquals("not the same comments", taskPojo.getMeta().getComments(), objFromDB.getMeta().getComments());
        assertEquals("not the same descriptions", taskPojo.getMeta().getDescriptions(), objFromDB.getMeta().getDescriptions());
    }

}
