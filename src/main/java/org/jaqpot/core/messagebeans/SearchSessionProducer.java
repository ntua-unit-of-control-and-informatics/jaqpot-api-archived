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

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.properties.PropertyManager;

/**
 *
 * @author pantelispanka
 */
@Startup
@DependsOn("OnAppInit")
@Singleton
public class SearchSessionProducer {

    Logger logger = Logger.getLogger(SearchSessionProducer.class.getName());

    @EJB
    PropertyManager pm;

    private KafkaProducer kafkaProducer;
    private final static String DEVTOPIC = "SearchSessionDev";
    private final static String PRODTOPIC = "SearchSessionProd";

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_EXISTS));
        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_EXISTS).equals("true")) {
            logger.log(Level.INFO, "Starting kafka SearchSessionProducer");
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_BOOTSTRAP));
            props.put(ProducerConfig.CLIENT_ID_CONFIG, "SearchSessionProducer");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    LongSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class.getName());
            this.kafkaProducer = new KafkaProducer<>(props);
            AdminClient admin = AdminClient.create(props);

            Map<String, String> configs = new HashMap<>();
            int partitions = Integer.parseInt(pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_REPLICATION));
            short replication = 2;
            if (pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_ENV).equals("dev")) {
                admin.createTopics(asList(new NewTopic(DEVTOPIC, partitions, replication).configs(configs)));
            } else {
                admin.createTopics(asList(new NewTopic(PRODTOPIC, partitions, replication).configs(configs)));
            }
        }
    }

    public void startSearchSession(String userId, String sessionId, String term) {
        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_ENV).equals("dev")) {
            ProducerRecord<Long, String> record = new ProducerRecord<>(DEVTOPIC, 
                    "Search_session_" + sessionId + "_user_" +
                    userId + "_term:" + term);
            try {
                this.kafkaProducer.send(record).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.SEVERE, "Cannot send {0} search session", sessionId);
            } finally {
                this.kafkaProducer.flush();
            }
        } else {
            ProducerRecord<Long, String> record = new ProducerRecord<>(PRODTOPIC, 
                    "Search_session_" + sessionId + "_user_" +
                    userId + "_term:" + term);
            try {
                this.kafkaProducer.send(record).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.SEVERE, "Cannot start {0} search session", sessionId);
            } finally {
                this.kafkaProducer.flush();
            }
        }

    }

    @PreDestroy
    public void destroy() {
        if(this.kafkaProducer!= null){
            this.kafkaProducer.close();
        }
    }

}
