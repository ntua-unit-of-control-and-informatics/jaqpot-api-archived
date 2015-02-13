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

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.jaqpot.core.annotations.MongoDB;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import org.bson.Document;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.JaqpotEntity;
import org.reflections.Reflections;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@MongoDB
public class MongoDBEntityManager implements JaqpotEntityManager {

    private static final Logger LOG = Logger.getLogger(MongoDBEntityManager.class.getName());

    @Inject
    JSONSerializer serializer;

    private final MongoClient mongoClient;
    private String database;

    public static final Map<Class, String> collectionNames;

    static {
        collectionNames = new HashMap<>();
        try {
            Reflections reflections = new Reflections("org.jaqpot.core.model");
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(XmlRootElement.class);
            for (Class c : annotatedClasses) {
                Annotation xmlRootElement = c.getAnnotation(XmlRootElement.class);
                String value = (String) xmlRootElement.annotationType().getMethod("name").invoke(xmlRootElement);
                collectionNames.put(c, !value.equals("##default") ? value : c.getSimpleName());
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            String errorMessage = "Improper data model - all elementary POJOs ###MUST### be annotated with @XmlRootElement!";
            LOG.log(Level.SEVERE, errorMessage, ex);
            throw new RuntimeException(ex); // terrible case: No @XmlRootElement annotation in our data model! 
        }
    }

    public MongoDBEntityManager() {
        mongoClient = new MongoClient();
    }

    @Override
    public void persist(Object entity) {
        MongoDatabase db = mongoClient.getDatabase(database);
        String entityJSON = serializer.write(entity);
        MongoCollection collection = db.getCollection(collectionNames.get(entity.getClass()));
        Document entityBSON = Document.valueOf(entityJSON);
        collection.insertOne(entityBSON);
    }

    @Override
    public <T> T merge(T entity) {
        MongoDatabase db = mongoClient.getDatabase(database);
        Class<T> entityClass = (Class<T>) entity.getClass();
        String entityJSON = serializer.write(entity);
        Document entityBSON = Document.valueOf(entityJSON);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entity.getClass()));
        Document oldEntity = collection.findOneAndReplace(new Document("_id", ((JaqpotEntity) entity).getId()), entityBSON);
        return serializer.parse(JSON.serialize(oldEntity), entityClass);
    }

    @Override
    public void remove(Object entity) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entity.getClass()));
        collection.deleteOne(new Document("_id", ((JaqpotEntity) entity).getId()));
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        Document retrieved = collection.find(new Document("_id", primaryKey)).first();
        return serializer.parse(JSON.serialize(retrieved), entityClass);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass, Integer start, Integer max) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        List<T> result = new ArrayList<>();
        collection.find()
                .skip(start)
                .limit(max)
                .map(document -> serializer.parse(JSON.serialize(document), entityClass))
                .into(result);
        return result;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

}
