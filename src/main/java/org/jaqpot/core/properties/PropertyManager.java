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
package org.jaqpot.core.properties;

import javax.inject.Inject;
import java.util.ResourceBundle;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * @author Angelos Valsamis
 */
@Startup
@Singleton
//@ApplicationScoped
public class PropertyManager {

    public enum PropertyType {
        JAQPOT_ENV("jaqpot.env", "config", "dev"),
        //        JAQPOT_ENV("jaqpot.env", "config", "prod"),
        JAQPOT_LOCAL_IP("jaqpot.local.ip", "config", "147.102.86.129"),
        JAQPOT_ADMINISTRATORS("jaqpot.administrators", "config", "admin"),
        JAQPOT_AA("jaqpot.aa", "config", "true"),
        JAQPOT_CORS_ALLOWORIGIN("jaqpot.cors.alloworigin", "config", "*"),
        JAQPOT_SCHEMA("jaqpot.schema", "config", "http"),
        JAQPOT_HOST("jaqpot.host", "config", "localhost"),
        JAQPOT_PORT("jaqpot.port", "config", "8080"),
        JAQPOT_BASE("jaqpot.base", "config", "/jaqpot/services"),
        JAQPOT_BASE_SERVICE("jaqpot.base.service", "config", "http://localhost:8080/jaqpot/services/"),
        JAQPOT_BASE_IMAGE("jaqpot.base.image", "config", "http://localhost:8880/imageAnalysis/service/"),
        JAQPOT_BASE_VALIDATION("jaqpot.base.validation", "config", "http://localhost:5000/pws/validation"),
        JAQPOT_BASE_INTERLAB("jaqpot.base.interlab", "config", "http://jaqpot.org:8091/pws/interlabtest/"),
        JAQPOT_RABBITMQ_HOST("jaqpot.rabbitmq.host", "config", ""),
        JAQPOT_RABBITMQ_USERNAME("jaqpot.rabbitmq.username", "config", ""),
        JAQPOT_RABBITMQ_PASSWORD("jaqpot.rabbitmq.password", "config", ""),
        DEFAULT_DOA("default.doa", "config", ""),
        DEFAULT_SCALING("default.scaling", "config", ""),
        DEFAULT_STANDARIZATION("default.standarization", "config", ""),
        JAQPOT_DB_NAME("jaqpot.db.name", "db", "test"),
        JAQPOT_DB_HOST("jaqpot.db.host", "db", "localhost"),
        JAQPOT_DB_PORT("jaqpot.db.port", "db", "27017"),
        JAQPOT_DB_CONNECTION_STRING("jaqpot.db.connection.string", "db", "mongodb://localhost:27017"),
        JAQPOT_MAIL_SEND("jaqpot.mail.dosend", "mail", "false"),
        JAQPOT_MAIL_MANDRILL_API_KEY("jaqpot.mail.mandrillApiKey", "mail", ""),
        JAQPOT_MAIL_FROM_MAIL("jaqpot.mail.fromMail", "mail", ""),
        JAQPOT_MAIL_FROM_NAME("jaqpot.mail.fromName", "mail", ""),
        JAQPOT_MAIL_RECIPIENTS("jaqpot.mail.recipients", "mail", ""),
        JAQPOT_JANITOR_TARGET("janitor.target", "janitor", ""),
        JAQPOT_JANITOR_USERNAME("janitor.username", "janitor", ""),
        JAQPOT_JANITOR_PASSWORD("janitor.password", "janitor", ""),
        JAQPOT_AMBIT("jaqpot.base.ambit", "config", "https://data.enanomapper.net/"),
        PYTHON_ALGORITHMS_HOST("python.algorithms.host", "config", "http://jaqpot.org:5000/"),
        JAQPOT_EXPERIMENTAL_DESIGNS_HOST("exp.design.host", "config", "http://localhost:8080/"),
        JAQPOT_BASE_ALGORITHMS("jaqpot.base.algorithms", "config", "http://localhost:8080/algorithms/service/"),
        JAQPOT_READACROSS("jaqpot.readacross", "config", "http://147.102.82.32:8095/"),
        JAQPOT_QPRF("jaqpot.qprf", "config", "http://147.102.82.32:8094/pws/qprf"),
        PKSIM_BASE("pksim.base", "config", "http://147.102.86.129:9999/"),
        OCPU_LM_BASE("ocpulm.base", "config", "http://test.jaqpot.org:8004/"),
        HTTK_BASE("httk.base", "config", "http://jaqpot.org:8011/"),
        
        OIDC_ISSUER("oidc.issuer", "config", "https://login.jaqpot.org/auth/realms/jaqpot/"),
        OIDC_CLIENT_ID("oidc.client.id", "config", "jaqpot-api"),
        OIDC_CLIENT_PASS("oidc.client.pass", "config", "9dccac19-23c6-49be-83be-8f07859d263f"),

//        OIDC_ISSUER("oidc.issuer", "config", "https://login.eosc.jaqpot.org/auth/realms/master/"),
//        OIDC_CLIENT_ID("oidc.client.id", "config", "jaqpot-api"),
//        OIDC_CLIENT_PASS("oidc.client.pass", "config", "b478b3f8-42d7-43bf-afa1-5370516bb97f"),
        
        OIDC_PROVIDER_CONF("oidc.provider.conf", "config", ".well-known/openid-configuration"),
        ELASTIC_HOST("elastic.host", "config", "192.168.10.100"),
        ELASTIC_PORT("elastic.port", "config", "31643"),
        ELASTIC_EXISTS("elastic.exists", "config", "false"),
        ELASTIC_AUTH("elastic.auth", "config", "false"),
        ELASTIC_USER("elastic.user", "config", "elastic"),
        ELASTIC_PASS("elastic.pass", "config", "52Uj482Q00djvkJ5VYfP0n6L"),
        KAFKA_BOOTSTRAP("kafka.bootstrap", "config", "192.168.10.84:32400,192.168.10.84:32401,192.168.10.84:32402"),
        KAFKA_EXISTS("kafka.exists", "config", "false"),
        KAFKA_REPLICATION("kafka.replication", "config", "3"),
        REDIS_EXISTS("redis.exists", "config", "false"),
        REDIS_DB("redis.db", "config", "localhost"),
        REDIS_ON_CLUSTER("redis.on.cluster", "config", "false"),
        REDIS_CLUSTER("redis.cluster", "config", "redis-cluster.redis:6379"),
        QUOTS_EXIST("quots.exist", "config", "false"),
        QUOTS_URL("quots.url", "config", "http://localhost:8000"),
        QUOTS_APP("quots.app", "config", "jaqpot"),
        QUOTS_APP_SECRET("quots.app.secret", "config", "VHChwwYKgsvmKQRF"),
        EUCLIA_ACCOUNTS_URL("euclia.accounts.url", "config", "https://accountsapi.jaqpot.org");

        private final String name;
        private final String bundle;
        private final String defaultValue;

        PropertyType(String value, String bundle, String defaultValue) {
            this.name = value;
            this.bundle = bundle;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return this.name;
        }

        public String getBundle() {
            return this.bundle;
        }

        public String getDefaultValue() {
            return this.defaultValue;
        }
    }

    @Inject
    public PropertyManager() {
    }

    public String getProperty(PropertyType propertyType) {
        String property = System.getenv(propertyType.getName().toUpperCase().replaceAll("\\.", "_"));
        if (property == null || property.isEmpty()) {
            property = ResourceBundle
                    .getBundle(propertyType.getBundle())
                    .getString(propertyType.getName());
        }
        return property;
    }

    public String getPropertyOrDefault(PropertyType propertyType, String defaultValue) {
        String property = getProperty(propertyType);
        if (property == null || property.isEmpty()) {
            property = defaultValue;
        }
        return property;
    }

    public String getPropertyOrDefault(PropertyType propertyType) {
        String property = getProperty(propertyType);
        if (property == null || property.isEmpty()) {
            property = propertyType.getDefaultValue();
        }
        return property;
    }
}
