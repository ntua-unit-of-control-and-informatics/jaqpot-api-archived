/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jaqpot.core.db.entitymanager;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.gridfs.GridFS;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.JaqpotEntity;
import org.jaqpot.core.properties.PropertyManager;

/**
 *
 * @author pantelispanka
 */
@MongoDB
//@ApplicationScoped
@Startup
@Singleton
@DependsOn("PropertyManager")
public class MongoDBGridFS implements JaqpotGridFS {

    private static final Logger LOG = Logger.getLogger(MongoDBGridFS.class.getName());
    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private MongoClient mongoClient;
    private GridFSBucket gridFsBucket;
    private GridFS gridFS;
    private String database;
    private static Properties dbProperties = new Properties();

    @Inject
    PropertyManager propertyManager;

    @Inject
    @MongoDB
    JSONSerializer serializer;

    public MongoDBGridFS() {
    }

    @PostConstruct
    public void init() {
        LOG.log(Level.INFO, "Initializing MongoDB EntityManager");

        //ClassLoader classLoader = this.getClass().getClassLoader();
        //InputStream is = classLoader.getResourceAsStream("config/db.properties");
        String dbName = "production"; // Default DB name in case no properties file is found!
        String dbHost = "localhost"; // Default DB host
        int dbPort = 27017; // Default DB port
        String connectionString = "mongodb://localhost:27017";
        try {
            connectionString = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_DB_CONNECTION_STRING, connectionString);
            dbName = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_DB_NAME, dbName);
            LOG.log(Level.INFO, "Database connection string : {0}", connectionString);
            LOG.log(Level.INFO, "Database name : {0}", dbName);
        } catch (Exception ex) {
            String errorMessage = "No DB properties file found!";
            LOG.log(Level.SEVERE, errorMessage, ex); // Log the event (but use the default properties)
        } finally {
            database = dbName;
            String mongoUri = connectionString;
            mongoClient = new MongoClient(new MongoClientURI(mongoUri));
            LOG.log(Level.INFO, "Database configured and connection established successfully!");
            MongoDatabase myDatabase = mongoClient.getDatabase(dbName);
            this.gridFsBucket = GridFSBuckets.create(myDatabase, "Modelfs");
        }

    }

    @Override
    public void persist(InputStream stream, JaqpotEntity j) {
        String entityJSON = serializer.write(j);
        Document entityBSON = Document.parse(entityJSON);
        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(1048576 * 4)
                .metadata(entityBSON);
        String title = (String) j.getMeta().getTitles().toArray()[0];
        this.gridFsBucket.uploadFromStream(title, stream, options);
    }

    @Override
    public String getObject(String id) throws InternalServerErrorException {

        String model = null;
        List<ObjectId> result = new ArrayList<ObjectId>();
        Bson query = Filters.eq("metadata._id", id);
        this.gridFsBucket.find(query)
                .limit(1)
                .forEach(new Consumer<GridFSFile>() {

                    @Override
                    public void accept(final GridFSFile gridFSFile) {
                        ObjectId oid = gridFSFile.getObjectId();
                        result.add(oid);
                    }
                });

        if (result.isEmpty()) {

        } else {
            try ( GridFSDownloadStream downloadStream = this.gridFsBucket.openDownloadStream(result.get(0))) {
                int fileLength = (int) downloadStream.getGridFSFile().getLength();
                byte[] bytesToWriteTo = new byte[fileLength];
                downloadStream.read(bytesToWriteTo);
                model = new String(bytesToWriteTo, StandardCharsets.UTF_8);
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "GetModel from mongo grid fs error", e);
                throw new InternalServerErrorException("Error while loading model");
            }
        }

        return model;
    }

    @Override
    public void close() {
        mongoClient.close();
    }

}
