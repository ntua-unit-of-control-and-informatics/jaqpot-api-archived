/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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
package org.jaqpot.core.messagebeans;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.search.engine.JaqpotElasticSearch;

/**
 *
 * @author pantelispanka
 */
@Startup
@DependsOn("OnAppInit")
@Singleton
public class DeleteIndexedEntityConsumer {

    Logger logger = Logger.getLogger(DeleteIndexedEntityConsumer.class.getName());

    @EJB
    PropertyManager pm;

    @EJB
    JaqpotElasticSearch elc;

    private KafkaConsumer kafkaConsumer;
    private final static String DEVTOPIC = "DeleteIndexedEntityDev";
    private final static String PRODTOPIC = "DeleteIndexedEntityProd";

    @PostConstruct
    public void init() {
        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_EXISTS).equals("true")) {
            logger.log(Level.INFO, "Starting kafka IndexModelConsumer");
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_BOOTSTRAP));
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "IndexModelConsumer");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    LongDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    StringDeserializer.class.getName());
            this.kafkaConsumer = new KafkaConsumer<>(props);

        }
    }

    public class deleteIndexedEntityConsumer implements Runnable {

        @Override
        public void run() {
            start();
        }
    }

    public void start() {
        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_ENV).equals("dev")) {
            this.kafkaConsumer.subscribe(Collections.singletonList(DEVTOPIC));
        } else {
            this.kafkaConsumer.subscribe(Collections.singletonList(PRODTOPIC));
        }
        ConsumerRecords<Long, String> consumerRecords = this.kafkaConsumer.poll(Duration.ofMillis(1000));
        consumerRecords.forEach(r -> {
            String[] value = r.value().split("_");
            String entityId = value[2];
            String path = null;
            if(pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_ENV).equals("dev")){
                path = String.format("jaqpotindexdev/meta_doc/%s", entityId);
            }
            else{
            path = String.format("jaqpotindexprod/meta_doc/%s", entityId);
            }
            Request req = new Request("DELETE", path);
            this.elc.getClient().performRequestAsync(req, indexListener(entityId));

        });
    }

    public ResponseListener indexListener(String entityId) {
        ResponseListener responseListener = new ResponseListener() {

            @Override
            public void onFailure(Exception e) {
                logger.log(Level.SEVERE, "Entity with id : {0} could not be deleted", entityId);
                logger.log(Level.SEVERE, "Exception, {0}", e.getMessage().toString());
            }

            @Override
            public void onSuccess(Response rspns) {

            }

        };
        return responseListener;
    }

    @PreDestroy
    public void destroy() {
        this.kafkaConsumer.close();
    }

}
