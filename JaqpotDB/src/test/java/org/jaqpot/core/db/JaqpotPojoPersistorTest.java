/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalambos Chomenides, Pantelis Sopasakis)
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
package org.jaqpot.core.db;

import org.jaqpot.core.db.JaqpotPojoPersistor;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.IOException;
import java.util.UUID;
import org.codehaus.jackson.map.ObjectMapper;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
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
public class JaqpotPojoPersistorTest {

    public JaqpotPojoPersistorTest() {
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

    /**
     * Writes a task to mongodb and retrieves it by ID.
     * @throws IOException 
     */
    @Test
    public void testSaveTask() throws IOException {
        MetaInfoBuilder metaBuilder = MetaInfoBuilder.builder();
        MetaInfo meta = metaBuilder.
                addComments("task started", "this task does training", "dataset downloaded").
                addDescriptions("this is a very nice task", "oh, and it's very useful too").
                addSources("http://jaqpot.org/algorithm/wonk").build();

        Task taskPojo = new Task(UUID.randomUUID().toString());
        taskPojo.setCreatedBy("random-user@jaqpot.org");
        taskPojo.setPercentageCompleted(0.95f);
        taskPojo.setDuration(1534l);
        taskPojo.setMeta(meta);
        taskPojo.setHttpStatus(202);
        taskPojo.setStatus(Task.Status.RUNNING);

        /* Initializes a Persistor with default DB configuration */
        JaqpotPojoPersistor persistor = new JaqpotPojoPersistor(taskPojo);
        persistor.persist();

        //Now find the object in the database:
        BasicDBObject query = new BasicDBObject("_id", taskPojo.getId()); // Find with ID

        // Now find it in the DB...
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("test");
        DBCollection coll = db.getCollection("tasks");
        DBCursor cursor = coll.find(query);

        assertTrue("nothing found", cursor.hasNext());
        DBObject retrieved = cursor.next();

        ObjectMapper mapper = new ObjectMapper();
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
