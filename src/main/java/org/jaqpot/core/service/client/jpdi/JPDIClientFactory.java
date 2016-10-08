/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.client.jpdi;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.properties.PropertyManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author hampos
 */
@Named
@ApplicationScoped
public class JPDIClientFactory {

    private static final Logger LOG = Logger.getLogger(JPDIClientFactory.class.getName());

    private JPDIClient client;

    @Inject
    @Jackson
    JSONSerializer serializer;

    @EJB
    FeatureHandler featureHandler;

    @Inject
    PropertyManager propertyManager;

    @PostConstruct
    public void init() {
        CloseableHttpAsyncClient asyncClient = HttpAsyncClientBuilder.create()
                .build();
        this.client = new JPDIClientImpl(asyncClient, serializer, featureHandler, propertyManager.getProperty(PropertyManager.PropertyType.JAQPOT_BASE_SERVICE));
    }

    @PreDestroy
    public void destroy() {
        try {
            this.client.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Could not successfully close JPDIClient", ex);
        }
    }

    @Produces
    public JPDIClient getClient() {
        return client;
    }
    
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        CloseableHttpAsyncClient asyncClient = HttpAsyncClientBuilder.create()
                .build();
        asyncClient.start();
        HttpGet request = new HttpGet("http://www.google.com");
        request.addHeader("Accept", "text/html");
        Future f = asyncClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse t) {
                System.out.println("completed");
                                
                try {
                    String result = new BufferedReader(new InputStreamReader(t.getEntity().getContent()))
                            .lines().collect(Collectors.joining("\n"));
                    System.out.println(result);
                } catch (IOException ex) {
                    Logger.getLogger(JPDIClientFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedOperationException ex) {
                    Logger.getLogger(JPDIClientFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void failed(Exception excptn) {
                System.out.println("failed");
            }

            @Override
            public void cancelled() {
                System.out.println("cancelled");
            }
        });
        f.get();
        asyncClient.close();
    }

}
