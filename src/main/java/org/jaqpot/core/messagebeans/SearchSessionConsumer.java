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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.User;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.search.dto.ElasticResponse;
import org.jaqpot.core.search.engine.JaqpotSearch;
import org.jaqpot.core.service.httphandlers.Rights;
import org.jaqpot.core.sessions.SessionClient;

/**
 *
 * @author pantelispanka
 */
@Startup
@DependsOn("OnAppInit")
@Singleton
public class SearchSessionConsumer {

    Logger logger = Logger.getLogger(SearchSessionConsumer.class.getName());

    private KafkaConsumer kafkaConsumer;
    private final static String DEVTOPIC = "SearchSessionDev";
    private final static String PRODTOPIC = "SearchSessionProd";

    @EJB
    PropertyManager pm;

    @EJB
    SessionClient redis;

    @EJB
    JaqpotSearch jSearch;

    @EJB
    DatasetHandler datasetHandler;

    @EJB
    ModelHandler modelHandler;

    @EJB
    UserHandler userHandler;

    @EJB
    Rights rights;

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

    public class searchSessionConsumer implements Runnable {

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
            this.createSession(r);
        });
    }

    public void createSession(ConsumerRecord<Long, String> consumerRecord) {

        try {
            String[] first = consumerRecord.value().split("_");
            String sessionId = first[2];
            String userId = first[4];
            String[] seccond = consumerRecord.value().split(":");
            String term = seccond[1];
            User user = userHandler.find(userId);

            int from = 0;
            int size = 20;
            long started = System.currentTimeMillis();
            ElasticResponse elResp = jSearch.search(term, from, size);
            List<String> foundEntities = new ArrayList();

//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                logger.log(Level.INFO, "trying to see the elResp");
//                logger.log(Level.INFO, mapper.writeValueAsString(elResp));
//            } catch (Exception e) {
//                logger.log(Level.SEVERE, e.getLocalizedMessage());
//            }          
            elResp.getHits().getHits().forEach(h -> {

                if (h.getSource().getEntity_type().equals("Model")) {
                    Model model = modelHandler.findMetaAndTrash(h.getId());
                    if (model != null) {
                        try {
                            if (rights.canView(model.getMeta(), user) && model.getOnTrash() == null || model.getOnTrash() != true) {
                                foundEntities.add("model/" + h.getId());
                            }
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, e.getMessage());
                        }

                    }
                }
                if (h.getSource().getEntity_type().equals("Dataset")) {
                    Dataset dataset = datasetHandler.findMetaAndTrash(h.getId());
                    if (rights.canView(dataset.getMeta(), user) && dataset.getOnTrash() == null || dataset.getOnTrash() != true) {
                        foundEntities.add("dataset/" + h.getId());
                    }
                }
            });

            redis.searchSession(sessionId, foundEntities, Boolean.FALSE);

            Integer total = elResp.getHits().getTotal();
            List<String> foundEntities2 = new ArrayList();
            if (size < total) {
                while (size < total) {
                    from = size;
                    size = size + 20;
                    elResp = jSearch.search(term, from, size);
                    total = elResp.getHits().getTotal();
                    elResp.getHits().getHits().forEach(h -> {
                        if (h.getSource().getEntity_type().equals("Model")) {
                            Model model = modelHandler.findMeta(h.getId());
                            if (model != null) {
                                
                                try {
                                    if (rights.canView(model.getMeta(), user) && model.getOnTrash() == null || rights.canView(model.getMeta(), user) && model.getOnTrash() != true) {
                                        foundEntities.add("model/" + h.getId());
                                    }
                                } catch (Exception e) {
                                    logger.log(Level.SEVERE, e.getMessage());
                                }

                            }
                        }
                        if (h.getSource().getEntity_type().equals("Dataset")) {
                            Dataset dataset = datasetHandler.findMeta(h.getId());
                            if (rights.canView(dataset.getMeta(), user)) {
                                foundEntities.add("dataset/" + h.getId());
                            }
                        }
                    });
                }
            }
            long finished = System.currentTimeMillis();
            long duration = finished - started;
            redis.searchSession(sessionId, foundEntities2, Boolean.TRUE);
            redis.setSearchDurration(sessionId, duration);

        } catch (IOException e) {
            logger.log(Level.SEVERE, consumerRecord.value());
        }

    }

    @PreDestroy
    public void destroy() {
        if(this.kafkaConsumer!= null){
            this.kafkaConsumer.close();
        }
    }
}
