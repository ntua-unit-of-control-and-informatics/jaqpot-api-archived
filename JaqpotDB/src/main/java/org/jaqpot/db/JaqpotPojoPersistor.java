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
package org.jaqpot.db;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jaqpot.core.model.serialize.PojoJsonSerializer;

/**
 *
 * @author chung
 */
public class JaqpotPojoPersistor {

    
    private Object pojo;
    
    public JaqpotPojoPersistor() {
    }

    public JaqpotPojoPersistor(Object pojo) {
        this.pojo = pojo;
    }
    
    public int persist() throws IOException{
        MongoClient mongoClient = JaqpotMongoClient.getInstance().getMongoClient();
        DB db = mongoClient.getDB("test");
        ObjectMapper mapper = ObjectMapperSingleton.getInstance().getObjectMapper();
        PojoJsonSerializer serializer = new PojoJsonSerializer(pojo, mapper);
        String pojoJSON = serializer.toJsonString();
        
        System.out.println(pojoJSON);
        DBObject taskDBObj = (DBObject) JSON.parse(pojoJSON);
        DBCollection collection = db.getCollection("tasks");
        WriteResult result = collection.insert(taskDBObj);
        return result.getN();
    }
    
    
    
}
