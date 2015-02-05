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
package org.jaqpot.core.db.entitymanager;

import org.jaqpot.core.annotations.MongoDB;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import org.jaqpot.core.data.serialize.EntityJSONSerializer;
import org.jaqpot.core.annotations.Jackson;

/**
 *
 * @author chung
 */
@MongoDB
public class MongoDBEntityManager implements JaqpotEntityManager {

    private static final Logger LOG = Logger.getLogger(JaqpotEntityManager.class.getName());

    @Inject
    EntityJSONSerializer serializer;

    MongoClient mongoClient;

    public MongoDBEntityManager() {
        try {
            mongoClient = new MongoClient("");
        } catch (UnknownHostException ex) {
            //TODO: create bundle with messages
            LOG.log(Level.SEVERE, "JaqpotEntityManager could not be crated properly. Please check your database configuration settings.", ex);
        }
    }

    @Override
    public void persist(Object entity) {
        DB db = mongoClient.getDB("test");
        String entityJSON = serializer.write(entity);

        System.out.println(entityJSON);
        DBObject taskDBObj = (DBObject) JSON.parse(entityJSON);
        DBCollection collection = db.getCollection("tasks");
        WriteResult result = collection.insert(taskDBObj);
        System.out.println(result.getN());

    }

    @Override
    public <T> T merge(T entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Object entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
