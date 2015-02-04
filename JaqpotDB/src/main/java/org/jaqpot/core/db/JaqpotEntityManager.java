/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.db;

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
import org.codehaus.jackson.map.ObjectMapper;
import org.jaqpot.core.model.serialize.PojoJsonSerializer;

/**
 *
 * @author hampos
 */
public class JaqpotEntityManager {

    private static final Logger LOG = Logger.getLogger(JaqpotEntityManager.class.getName());

    MongoClient mongoClient;
    ObjectMapper mapper;

    public JaqpotEntityManager(String server) {
        try {
            mongoClient = new MongoClient(server);
            mapper = new ObjectMapper();
        } catch (UnknownHostException ex) {
            //TODO: create bundle with messages
            LOG.log(Level.SEVERE, "JaqpotEntityManager could not be crated properly. Please check your database configuration settings.", ex);
        }
    }

    public void persist(Object entity) {
        try {
            DB db = mongoClient.getDB("test");            
            String entityJSON = mapper.writeValueAsString(entity);

            System.out.println(entityJSON);
            DBObject taskDBObj = (DBObject) JSON.parse(entityJSON);
            DBCollection collection = db.getCollection("tasks");
            WriteResult result = collection.insert(taskDBObj);
            System.out.println(result.getN());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public <T> T merge(T entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void remove(Object entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
