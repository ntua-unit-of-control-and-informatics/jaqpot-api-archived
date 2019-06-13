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
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.jaqpot.core.data.DatasetHandler;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.ModelHandler;
import org.jaqpot.core.messagebeans.IndexEntityProducer.EntityType;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.dto.dataset.Dataset;

import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.search.engine.ElasticQueries;
import org.jaqpot.core.search.engine.JaqpotElasticSearch;

/**
 *
 * @author pantelispanka
 */
@Startup
@DependsOn("OnAppInit")
@Singleton
public class IndexEntityConsumer {

    Logger logger = Logger.getLogger(IndexEntityConsumer.class.getName());

    private KafkaConsumer kafkaConsumer;
    private final static String DEVTOPIC = "IndexModelDev";
    private final static String PRODTOPIC = "IndexModelProd";

    @EJB
    PropertyManager pm;

    @EJB
    ModelHandler mh;

    @EJB
    DatasetHandler dh;

    @EJB
    FeatureHandler fh;

    @EJB
    ElasticQueries elq;

    @EJB
    JaqpotElasticSearch elc;

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

    public class indexModelConsumer implements Runnable {

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

            String[] message = r.value().toString().split("_");
            String entity = message[message.length - 2];
            switch (entity) {
                case "model":
                    this.indexModel(r);
                    break;
                case "dataset":
                    this.indexDataset(r);
                    break;
            }

        });
    }

    public void indexDataset(ConsumerRecord<Long, String> consumerRecord) {
        String[] message = consumerRecord.value().toString().split("_");
        String datasetId = message[message.length - 1];
        String indexType = message[0];
        Dataset dataset = dh.find(datasetId);
        StringBuilder sb = new StringBuilder();
        if (dataset.getMeta() != null) {
            if (dataset.getMeta().getTitles() != null) {
                dataset.getMeta().getTitles().forEach(t -> {
                    sb.append(t).append(" ");
                });
            }
            if (dataset.getMeta().getAudiences() != null) {
                dataset.getMeta().getAudiences().forEach(a -> {
                    sb.append(a).append(" ");
                });
            }
            if (dataset.getMeta().getComments() != null) {
                dataset.getMeta().getComments().forEach(c -> {
                    sb.append(c).append(" ");
                });
            }
            if (dataset.getMeta().getTags() != null) {
                dataset.getMeta().getTags().forEach(t -> {
                    sb.append(t).append(" ");
                });
            }
            if (dataset.getMeta().getDescriptions() != null) {
                dataset.getMeta().getDescriptions().forEach(d -> {
                    sb.append(d).append(" ");
                });
            }
            if (dataset.getMeta().getIdentifiers() != null) {
                dataset.getMeta().getIdentifiers().forEach(i -> {
                    sb.append(i).append(" ");
                });
            }
            if (dataset.getMeta().getMarkdown() != null) {
                String markString = dataset.getMeta().getMarkdown()
                        .replace("\n", "")
                        .replace("\r", " ")
                        .replace("#", " ")
                        .replace('"', ' ')
                        .replaceAll("(\\d+)-(?=\\d)", "$1_")
                        .replace("-", " ")
                        .replace(".", " ")
                        .replace(":", " ")
                        .replace("[", " ")
                        .replace("]", " ")
                        .replace(";", " ")
                        .replaceAll(":", " ")
                        .replace("<", " ")
                        .replace(">", " ")
                        .replace("*", " ");
                sb.append(markString).append(" ");
            }
            if (dataset.getMeta().getRead() != null) {
                dataset.getMeta().getRead().forEach(i -> {
                    sb.append(i).append(" ");
                });
            }
            dataset.getFeatures().forEach(depf -> {
                String[] featUri = depf.getURI().split("/");
                Feature feat = fh.find(featUri[featUri.length - 1]);
                if (feat.getMeta() != null) {
                    if (feat.getMeta().getTitles() != null) {
                        feat.getMeta().getTitles().forEach(t -> {
                            sb.append("Predicts ").append(t).append(" ");
                        });
                    }
                    if (feat.getMeta().getAudiences() != null) {
                        feat.getMeta().getAudiences().forEach(a -> {
                            sb.append(a).append(" ");
                        });
                    }
                    if (feat.getMeta().getComments() != null) {
                        feat.getMeta().getComments().forEach(c -> {
                            sb.append(c).append(" ");
                        });
                    }
                    if (feat.getMeta().getTags() != null) {
                        feat.getMeta().getTags().forEach(t -> {
                            sb.append(t).append(" ");
                        });
                    }
                    if (feat.getMeta().getDescriptions() != null) {
                        feat.getMeta().getDescriptions().forEach(d -> {
                            sb.append(d).append(" ");
                        });
                    }
                    if (feat.getMeta().getIdentifiers() != null) {
                        feat.getMeta().getIdentifiers().forEach(i -> {
                            sb.append(i).append(" ");
                        });
                    }
                    if (feat.getMeta().getMarkdown() != null) {
                        String markString = feat.getMeta().getMarkdown();
                        sb.append(markString).append(" ");
                    }
                }
            });
        }

        String forIndex = sb.toString()
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\\', ',')
                .replace('/', ' ')
                .replaceAll("/", " ")
                .replaceAll("\\\\", " ")
                .replace('"', ' ')
                .replaceAll("(\\d+)-(?=\\d)", "$1_")
                .replace("-", " ")
                .replace(".", " ")
                .replace(":", " ")
                .replace("[", " ")
                .replace("]", " ")
                .replace(";", " ")
                .replaceAll(":", " ")
                .replace("<", " ")
                .replace(">", " ")
                .replace("*", " ")
                .replaceAll("()", " ");

        String jaqpotIndex = elq.getModelIndex();

        String entity = String.format(jaqpotIndex, forIndex,
                "Model");
        HttpEntity httpEntity = new NStringEntity(entity, ContentType.APPLICATION_JSON);

        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_ENV).equals("dev")) {
            String path = null;
            if (indexType.equals("Index")) {
                path = String.format("jaqpotindexdev/meta_doc/%s/_create", dataset.getId());
            }
            if (indexType.equals("Update")) {
                path = String.format("jaqpotindexdev/meta_doc/%s/", dataset.getId());
            }
            Request req = new Request("PUT", path);
            req.setEntity(httpEntity);
            this.elc.getClient().performRequestAsync(req, indexListener(dataset.getId(), EntityType.DATASET, sb.toString()));
        } else {
            String path = String.format("jaqpotindexprod/_doc/{0}?op_type=create", dataset.getId());
            Request req = new Request("PUT", path);
            req.setEntity(httpEntity);
            this.elc.getClient().performRequestAsync(req, indexListener(dataset.getId(), EntityType.DATASET, sb.toString()));
        }

    }

    public void indexModel(ConsumerRecord<Long, String> consumerRecord) {
        String[] message = consumerRecord.value().toString().split("_");
        String modelId = message[message.length - 1];
        String indexType = message[0];
        Model model = mh.find(modelId);
        StringBuilder sb = new StringBuilder();

        try {
            if (model.getMeta() != null) {
                if (model.getMeta().getTitles() != null) {
                    model.getMeta().getTitles().forEach(t -> {
                        sb.append(t).append(" ");
                    });
                }
                if (model.getMeta().getAudiences() != null) {
                    model.getMeta().getAudiences().forEach(a -> {
                        sb.append(a).append(" ");
                    });
                }
                if (model.getMeta().getComments() != null) {
                    model.getMeta().getComments().forEach(c -> {
                        sb.append(c).append(" ");
                    });
                }
                if (model.getMeta().getTags() != null) {
                    model.getMeta().getTags().forEach(t -> {
                        sb.append(t).append(" ");
                    });
                }
                if (model.getMeta().getDescriptions() != null) {
                    model.getMeta().getDescriptions().forEach(d -> {
                        sb.append(d).append(" ");
                    });
                }
                if (model.getMeta().getIdentifiers() != null) {
                    model.getMeta().getIdentifiers().forEach(i -> {
                        sb.append(i).append(" ");
                    });
                }
                if (model.getMeta().getMarkdown() != null) {
                    String markString = model.getMeta().getMarkdown();
                    sb.append(markString).append(" ");
                }
                if (model.getMeta().getRead() != null) {
                    model.getMeta().getRead().forEach(i -> {
                        sb.append(i).append(" ");
                    });
                }
                if (model.getDependentFeatures() != null) {
                    model.getDependentFeatures().forEach(depf -> {
                        String[] featUri = depf.split("/");
                        Feature feat = fh.find(featUri[featUri.length - 1]);
                        if (feat.getMeta() != null) {
                            if (feat.getMeta().getTitles() != null) {
                                feat.getMeta().getTitles().forEach(t -> {
                                    sb.append("Predicts ").append(t).append(" ");
                                });
                            }
                            if (feat.getMeta().getAudiences() != null) {
                                feat.getMeta().getAudiences().forEach(a -> {
                                    sb.append(a).append(" ");
                                });
                            }
                            if (feat.getMeta().getComments() != null) {
                                feat.getMeta().getComments().forEach(c -> {
                                    sb.append(c).append(" ");
                                });
                            }
                            if (feat.getMeta().getTags() != null) {
                                feat.getMeta().getTags().forEach(t -> {
                                    sb.append(t).append(" ");
                                });
                            }
                            if (feat.getMeta().getDescriptions() != null) {
                                feat.getMeta().getDescriptions().forEach(d -> {
                                    sb.append(d).append(" ");
                                });
                            }
                            if (feat.getMeta().getIdentifiers() != null) {
                                feat.getMeta().getIdentifiers().forEach(i -> {
                                    sb.append(i).append(" ");
                                });
                            }
                            if (feat.getMeta().getMarkdown() != null) {
                                String markString = feat.getMeta().getMarkdown();
                                sb.append(markString).append(" ");
                            }
                        }
                    });

                }

                if (model.getIndependentFeatures() != null) {
                    model.getIndependentFeatures().forEach(indf -> {
                        String[] featUri = indf.split("/");
                        Feature feat = fh.find(featUri[featUri.length - 1]);
                        if (feat.getMeta() != null) {
                            if (feat.getMeta().getTitles() != null) {
                                feat.getMeta().getTitles().forEach(t -> {
                                    sb.append(t).append(" ");
                                });
                            }
                            if (feat.getMeta().getAudiences() != null) {
                                feat.getMeta().getAudiences().forEach(a -> {
                                    sb.append(a).append(" ");
                                });
                            }
                            if (feat.getMeta().getComments() != null) {
                                feat.getMeta().getComments().forEach(c -> {
                                    sb.append(c).append(" ");
                                });
                            }
                            if (feat.getMeta().getTags() != null) {
                                feat.getMeta().getTags().forEach(t -> {
                                    sb.append(t).append(" ");
                                });
                            }
                            if (feat.getMeta().getDescriptions() != null) {
                                feat.getMeta().getDescriptions().forEach(d -> {
                                    sb.append(d).append(" ");
                                });
                            }
                            if (feat.getMeta().getIdentifiers() != null) {
                                feat.getMeta().getIdentifiers().forEach(i -> {
                                    sb.append(i).append(" ");
                                });
                            }
                            if (feat.getMeta().getMarkdown() != null) {
                                String markString = feat.getMeta().getMarkdown();
                                sb.append(markString).append(" ");
                            }
                        }
                    });
                }

            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while gathering info about model" + modelId);
        }

        String forIndex = sb.toString()
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\\', ',')
                .replace('/', ' ')
                .replaceAll("/", " ")
                .replaceAll("\\\\", " ")
                .replace('"', ' ')
                .replaceAll("(\\d+)-(?=\\d)", "$1_")
                .replace("-", " ")
                .replace(".", " ")
                .replace(":", " ")
                .replace("[", " ")
                .replace("]", " ")
                .replace(";", " ")
                .replaceAll(":", " ")
                .replace("<", " ")
                .replace(">", " ")
                .replace("*", " ");

        String jaqpotIndex = elq.getModelIndex();

        String entity = String.format(jaqpotIndex, forIndex,
                "Model");
        HttpEntity httpEntity = new NStringEntity(entity, ContentType.APPLICATION_JSON);

        if (pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_ENV).equals("dev")) {
            String path = null;
            if (indexType.equals("Index")) {
                path = String.format("jaqpotindexdev/meta_doc/%s/_create", modelId);
            }
            if (indexType.equals("Update")) {
                path = String.format("jaqpotindexdev/meta_doc/%s/", modelId);
            }

            Request req = new Request("PUT", path);
            req.setEntity(httpEntity);
            try {
                this.elc.getClient().performRequestAsync(req, indexListener(modelId, EntityType.MODEL, forIndex));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not index model {0} ", modelId + " " + e.getMessage());
            }
        } else {
            String path = String.format("jaqpotindexprod/meta_doc/%s/_create", model.getId());
            Request req = new Request("PUT", path);
            req.setEntity(httpEntity);
            try {
                this.elc.getClient().performRequestAsync(req, indexListener(model.getId(), EntityType.MODEL, forIndex));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not index model {0} ", modelId + " " + e.getMessage());
            }
        }
    }

    public ResponseListener indexListener(String entityId, EntityType et, String sb) {
        ResponseListener responseListener = new ResponseListener() {

            @Override
            public void onFailure(Exception e) {
                logger.log(Level.SEVERE, "Model with id : {0} could not be indexed exception", entityId);
                logger.log(Level.SEVERE, "Entity string : {0}", sb);
                logger.log(Level.SEVERE, "Exception, {0}", e.getMessage());
            }

            @Override
            public void onSuccess(Response rspns) {
                switch (et) {
                    case MODEL:
                        mh.updateField(entityId, "indexed", true);
                        break;
                    case DATASET:
                        dh.updateField(entityId, "indexed", true);
                        break;
                }
//                
            }

        };
        return responseListener;
    }

    @PreDestroy
    public void destroy() {
        if(this.kafkaConsumer!= null){
            this.kafkaConsumer.close();
        }
    }

}
