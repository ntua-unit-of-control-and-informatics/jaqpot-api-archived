/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.client.jpdi;

import java.util.ResourceBundle;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.client.HttpAsyncClient;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;

/**
 *
 * @author hampos
 */
@ApplicationScoped
public class JPDIClientFactory {

    private final JPDIClient client;

    @Inject
    public JPDIClientFactory(@Jackson JSONSerializer serializer, FeatureHandler featureHandler) {
        HttpAsyncClient asyncClient = HttpAsyncClientBuilder.create()
                .build();
        this.client = new JPDIClientImpl(asyncClient, serializer, featureHandler, ResourceBundle.getBundle("config").getString("ServerBasePath"));
    }

    @Produces
    public JPDIClient getClient() {
        return client;
    }

}
