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
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.jaqpot.core.annotations.ElasticSearch;
import org.jaqpot.core.model.JaqpotEntity;
import org.jaqpot.core.properties.PropertyManager;

/**
 *
 * @author Pantelis Karatzas
 *
 */
@ElasticSearch
@Singleton
@Startup
@DependsOn("OnAppInit")
public class JaqpotElasticSearch implements JaqpotSearchEngine {

    private static final Logger LOG = Logger.getLogger(JaqpotElasticSearch.class.getName());

    @Inject
    PropertyManager propertyManager;

    private String elasticHost;
    private Integer elasticPort;
    private RestClient elCl;
    private String elasticExistence;

    @PostConstruct
    void init() {

        elasticExistence = propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.ELASTIC_EXISTS);

        if ("true".equals(elasticExistence)) {
            this.elasticHost = propertyManager
                    .getProperty(PropertyManager.PropertyType.ELASTIC_HOST);
            this.elasticPort = Integer.valueOf(propertyManager
                    .getProperty(PropertyManager.PropertyType.ELASTIC_PORT));
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
        Response resp = null;
        String responseBody = null;
        try {
            resp = this.elCl.performRequest("GET", "_cat/indices");
            responseBody = EntityUtils.toString(resp.getEntity());
            Boolean indExists = responseBody.toLowerCase().contains("model");
            System.out.println(responseBody);
//            if (indExists == false) {
//                this.createIndices();
//            }
        } catch (IOException e) {
            throw new InternalServerErrorException("Something went wrong with elastic client", e);
        }
    }
    
    

    @Override
    public void index(JaqpotEntity entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
