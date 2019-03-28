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
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.properties.PropertyManager;

/**
 *
 * @author pantelispanka
 */
@Startup
@DependsOn("OnAppInit")
@Singleton
public class IndexEntityProducer {

    Logger logger = Logger.getLogger(IndexEntityConsumer.class.getName());

    @EJB
    PropertyManager pm;

    @EJB
    ModelHandler modelHandler;

    private KafkaProducer kafkaProducer;
    private final static String DEVTOPIC = "IndexModelDev";
    private final static String PRODTOPIC = "IndexModelProd";

    public enum EntityType {

        MODEL("model", "entity is model"),
        DATASET("dataset", "entity is dataset");

        private final String name;
        private final String description;

        private EntityType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }

    }

    public enum IndexTransaction {

        INDEX("index", "index the entity"),
        UPDATE("update", "update the entity");

        private final String name;
        private final String description;

        private IndexTransaction(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }

    }

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_EXISTS));
        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_EXISTS).equals("true")) {
            logger.log(Level.INFO, "Starting kafka IndexEnityProducer");
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    pm.getPropertyOrDefault(PropertyManager.PropertyType.KAFKA_BOOTSTRAP));
            props.put(ProducerConfig.CLIENT_ID_CONFIG, "IndexEnityProducer");
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

            Map<String, Object> prope = new HashMap();
            prope.put("indexed", null);
            List<String> fields = new ArrayList();
            fields.add("_id");
            List<Model> unindexed = modelHandler.find(prope, fields, Integer.SIZE, Integer.SIZE);
            if (unindexed.size() > 0) {
                logger.log(Level.INFO, "Starting indexing {0} unindexd models ", String.valueOf(unindexed.size()));
                unindexed.forEach(m -> {
                    this.sendJaqpotModelIDForIndex(m.getId(), EntityType.MODEL, IndexTransaction.INDEX);
                });
            }

        }
    }

    public void sendJaqpotModelIDForIndex(String enityId, EntityType et, IndexTransaction it) {
        switch (et) {
            case MODEL:
                if (pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_ENV).equals("dev")) {
                    ProducerRecord<Long, String> record = null;
                    if (it.equals(IndexTransaction.INDEX)) {
                        record = new ProducerRecord<>(DEVTOPIC, "Index_model_" + enityId);
                    }
                    if (it.equals(IndexTransaction.UPDATE)) {
                        record = new ProducerRecord<>(DEVTOPIC, "Update_model_" + enityId);
                    }

                    try {
                        this.kafkaProducer.send(record).get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.SEVERE, "Cannot send {0}", enityId);
                    } finally {
                        this.kafkaProducer.flush();
                    }
                } else {
                    ProducerRecord<Long, String> record = null;
                    if (it.equals(IndexTransaction.INDEX)) {
                        record = new ProducerRecord<>(DEVTOPIC, "Index_model_" + enityId);
                    }
                    if (it.equals(IndexTransaction.UPDATE)) {
                        record = new ProducerRecord<>(DEVTOPIC, "Update_model_" + enityId);
                    }
                    try {
                        this.kafkaProducer.send(record).get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.SEVERE, "Cannot send {0}", enityId);
                    } finally {
                        this.kafkaProducer.flush();
                    }
                }
                break;
            case DATASET:
                if (pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_ENV).equals("dev")) {
                    ProducerRecord<Long, String> record
                            = new ProducerRecord<>(DEVTOPIC, "Index_dataset_" + enityId);
                    try {
                        this.kafkaProducer.send(record).get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.SEVERE, "Cannot send {0}", enityId);
                    } finally {
                        this.kafkaProducer.flush();
                    }
                } else {
                    ProducerRecord<Long, String> record = new ProducerRecord<>(PRODTOPIC, "Index_dataset_" + enityId);
                    try {
                        this.kafkaProducer.send(record).get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.SEVERE, "Cannot send {0}", enityId);
                    } finally {
                        this.kafkaProducer.flush();
                    }
                }
                break;
        }

    }

    @PreDestroy
    public void destroy() {
        this.kafkaProducer.close();
    }
}
