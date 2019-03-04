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
package org.jaqpot.core.search.engine;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.jaqpot.core.properties.PropertyManager;

/**
 *
 * @author Pantelis Karatzas
 *
 */
//@ElasticSearch
@Singleton
@Startup
@DependsOn("ElasticQueries")
public class JaqpotElasticSearch {

    private static final Logger LOG = Logger.getLogger(JaqpotElasticSearch.class.getName());

    @Inject
    PropertyManager propertyManager;

    @Inject
    ElasticQueries eq;

    private String elasticHost;
    private Integer elasticPort;
    private RestClient elCl;
    private String elasticExistence;

    @PostConstruct
    void init() {

        elasticExistence = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.ELASTIC_EXISTS);

        if ("true".equals(elasticExistence)) {
            this.elasticHost = propertyManager
                    .getPropertyOrDefault(PropertyManager.PropertyType.ELASTIC_HOST);
            this.elasticPort = Integer.valueOf(propertyManager
                    .getPropertyOrDefault(PropertyManager.PropertyType.ELASTIC_PORT));
            LOG.log(Level.INFO, "ElasticSearch initialization");
            LOG.log(Level.INFO, "ElasticSearch host : {0}", this.elasticHost);
            LOG.log(Level.INFO, "ElasticSearch port : {0}", this.elasticPort);

            RestClientBuilder builder = RestClient.builder(
                    new HttpHost(this.elasticHost, this.elasticPort)
            );
            builder.setMaxRetryTimeoutMillis(10000);
            this.elCl = builder.build();

            this.checkForIndices();
        }

    }

    public void checkForIndices() {
        this.createModelIndice();
    }

    private void createModelIndice() {
        String responseBody = null;
        try {
            Request check = new Request("GET", "_cat/indices");
            Response resp1 = this.elCl.performRequest(check);
            responseBody = EntityUtils.toString(resp1.getEntity());
            Boolean indDevExists = responseBody.toLowerCase().contains("jaqpotindexdev");
            if (indDevExists == false) {
                String settings = eq.getModelIndice();
                try {
                    HttpEntity entity = new NStringEntity(settings, ContentType.APPLICATION_JSON);
                    Request req = new Request("PUT", "/jaqpotindexdev");
                    req.setEntity(entity);
                    req.addParameter("pretty", "true");
                    this.elCl.performRequest(req);
                } catch (IOException e) {
                    throw new InternalServerErrorException("Could not create index for models", e);
                }
            }
            Boolean indProdExists = responseBody.toLowerCase().contains("jaqpotindexprod");
            if (indProdExists == false) {
                String settings = eq.getModelIndice();
                try {
                    HttpEntity entity = new NStringEntity(settings, ContentType.APPLICATION_JSON);
                    Request req = new Request("PUT", "/jaqpotindexprod");
                    req.setEntity(entity);
                    req.addParameter("pretty", "true");
                    this.elCl.performRequest(req);
//                    responseBody = EntityUtils.toString(resp3.getEntity());
                } catch (IOException e) {
                    throw new InternalServerErrorException("Could not create index for models", e);
                }
            }
        } catch (IOException e) {
            throw new InternalServerErrorException("Something went wrong with elastic client", e);
        }
    }

    
    public RestClient getClient(){
        return this.elCl;
    }
            
          
//    @Override
//    public void index(JaqpotEntity entity) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

}
