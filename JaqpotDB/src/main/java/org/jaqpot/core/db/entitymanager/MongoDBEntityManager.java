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

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.io.InputStream;
import org.jaqpot.core.annotations.MongoDB;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import org.bson.Document;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.JaqpotEntity;
import org.reflections.Reflections;

/**
 * A JaqpotEntityManager implementation for MongoDB.
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@MongoDB
@ApplicationScoped
public class MongoDBEntityManager implements JaqpotEntityManager {

    private static final Logger LOG = Logger.getLogger(MongoDBEntityManager.class.getName());
    private static final Integer DEFAULT_PAGE_SIZE = 10;

    @Inject
    @MongoDB
    JSONSerializer serializer;

    private final MongoClient mongoClient;
    private String database;
    private static Properties dbProperties = new Properties();

    private static final Map<Class, String> collectionNames;

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
        LOG.log(Level.INFO, "Initializing MongoDB EntityManager");

        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream("config/db.properties");
        String dbName = "production"; // Default DB name in case no properties file is found!
        String dbHost = "localhost"; // Default DB host
        int dbPort = 27017; // Default DB port
        try {
            dbProperties.load(is);
            dbName = dbProperties.getProperty("db.name");
            dbHost = dbProperties.getProperty("db.host", dbHost);
            dbPort = Integer.parseInt(dbProperties.getProperty("db.port", Integer.toString(dbPort)));
            LOG.log(Level.INFO, "Database host : {0}", dbHost);
            LOG.log(Level.INFO, "Database port : {0}", dbPort);
            LOG.log(Level.INFO, "Database name : {0}", dbName);
        } catch (IOException ex) {
            String errorMessage = "No DB properties file found!";
            LOG.log(Level.SEVERE, errorMessage, ex); // Log the event (but use the default properties)
        } finally {
            database = dbName;
            mongoClient = new MongoClient(dbHost, dbPort); // Connect to the DB  
            LOG.log(Level.INFO, "Database configured and connection established successfully!");
        }

    }

    @Override
    public void close() {
        mongoClient.close();
    }

    @Override
    public void persist(JaqpotEntity entity) {
        MongoDatabase db = mongoClient.getDatabase(database);
        String entityJSON = serializer.write(entity);
        MongoCollection collection = db.getCollection(collectionNames.get(entity.getClass()));
        Document entityBSON = Document.parse(entityJSON);
        try {
            collection.insertOne(entityBSON);
        } catch (final MongoWriteException ex) {
            String errorMessage = "Entity with ID " + entity.getId() + " is already registered and will not be overwritten!";
            LOG.log(Level.FINE, errorMessage, ex);
            throw ex;
        }
    }

    @Override
    public <T extends JaqpotEntity> T merge(T entity) {
        MongoDatabase db = mongoClient.getDatabase(database);
        Class<T> entityClass = (Class<T>) entity.getClass();
        String entityJSON = serializer.write(entity);
        Document entityBSON = Document.parse(entityJSON);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entity.getClass()));
        Document oldEntity = collection.findOneAndReplace(new Document("_id", entity.getId()), entityBSON);
        return serializer.parse(JSON.serialize(oldEntity), entityClass);
    }

    @Override
    public void remove(JaqpotEntity entity) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entity.getClass()));
        collection.deleteOne(new Document("_id", entity.getId()));
    }

    @Override
    public <T extends JaqpotEntity> T find(Class<T> entityClass, Object primaryKey) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        Document retrieved = collection.find(new Document("_id", primaryKey)).first();
        return serializer.parse(JSON.serialize(retrieved), entityClass);
    }

    @Override
    public <T extends JaqpotEntity> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        properties.put("_id", primaryKey);
        Document document = new Document(properties);
        Document retrieved = collection.find(document).first();
        return serializer.parse(JSON.serialize(retrieved), entityClass);
    }

    @Override
    public <T extends JaqpotEntity> List<T> find(Class<T> entityClass, Map<String, Object> properties, Integer start, Integer max) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        properties.entrySet()
                .stream()
                .filter(e -> {
                    return e.getValue() instanceof List;
                })
                .forEach(e -> {
                    Map<String, Object> all = new HashMap<>();
                    all.put("$all", e.getValue());
                    properties.put(e.getKey(), all);
                });

        List<T> result = new ArrayList<>();
        collection.find(new Document(properties))
                .skip(start != null ? start : 0)
                .limit(max != null ? max : DEFAULT_PAGE_SIZE)
                .map(document -> serializer.parse(JSON.serialize(document), entityClass))
                .into(result);
        return result;
    }

    @Override
    public <T extends JaqpotEntity> Long count(Class<T> entityClass, Map<String, Object> properties) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        properties.entrySet()
                .stream()
                .filter(e -> {
                    return e.getValue() instanceof List;
                })
                .forEach(e -> {
                    Map<String, Object> all = new HashMap<>();
                    all.put("$all", e.getValue());
                    properties.put(e.getKey(), all);
                });
        return collection.count(new Document(properties));
    }

    @Override
    public <T extends JaqpotEntity> List<T> find(Class<T> entityClass, List<String> keys, List<String> fields) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        List<T> result = new ArrayList<>();
        Document query = new Document("_id", new Document("$in", keys));
        Document filter = new Document();
        fields.stream().forEach(f -> filter.put(f, 1));
        collection.find(query).projection(filter)
                .map(document -> serializer.parse(JSON.serialize(document), entityClass))
                .into(result);
        return result;
    }

    @Override
    public <T extends JaqpotEntity> List<T> find(Class<T> entityClass, Map<String, Object> properties, List<String> fields, Integer start, Integer max) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        properties.entrySet()
                .stream()
                .filter(e -> {
                    return e.getValue() instanceof List;
                })
                .forEach(e -> {
                    Map<String, Object> all = new HashMap<>();
                    all.put("$all", e.getValue());
                    properties.put(e.getKey(), all);
                });

        Document filter = new Document();
        fields.stream().forEach(f -> filter.put(f, 1));
        List<T> result = new ArrayList<>();
        collection.find(new Document(properties))
                .projection(filter)
                .skip(start != null ? start : 0)
                .limit(max != null ? max : DEFAULT_PAGE_SIZE)
                .map(document -> serializer.parse(JSON.serialize(document), entityClass))
                .into(result);
        return result;
    }

    @Override
    public <T extends JaqpotEntity> List<T> findAll(Class<T> entityClass, Integer start, Integer max) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        List<T> result = new ArrayList<>();
        collection.find()
                .skip(start != null ? start : 0)
                .limit(max != null ? max : DEFAULT_PAGE_SIZE)
                .map(document -> serializer.parse(JSON.serialize(document), entityClass))
                .into(result);
        return result;
    }

    @Override
    public <T extends JaqpotEntity> List<T> findAll(Class<T> entityClass, List<String> fields, Integer start, Integer max) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        List<T> result = new ArrayList<>();
        Document filter = new Document();
        fields.stream().forEach(f -> filter.put(f, 1));
        collection.find()
                .projection(filter)
                .skip(start != null ? start : 0)
                .limit(max != null ? max : DEFAULT_PAGE_SIZE)
                .map(document -> serializer.parse(JSON.serialize(document), entityClass))
                .into(result);
        return result;
    }

    @Override
    public <T extends JaqpotEntity> Long countAll(Class<T> entityClass) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionNames.get(entityClass));
        return collection.count();
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

}
