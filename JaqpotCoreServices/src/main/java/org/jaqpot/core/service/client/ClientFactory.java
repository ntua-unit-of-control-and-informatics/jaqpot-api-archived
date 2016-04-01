/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.client;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.ws.rs.client.Client;
import org.jaqpot.core.service.annotations.Secure;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@ApplicationScoped
public class ClientFactory {

    private static final Logger LOG = Logger.getLogger(ClientFactory.class.getName());

    private final Client unSecureClient;
    private final Client secureClient;

    public ClientFactory() {
        this.unSecureClient = new ResteasyClientBuilder()
                .disableTrustManager()
                .socketTimeout(30, TimeUnit.MINUTES)
                .connectionPoolSize(20)
                .build();
        this.secureClient = new ResteasyClientBuilder()
                .socketTimeout(30, TimeUnit.MINUTES)
                .connectionPoolSize(10)
                .build();
    }

    @Produces
    @UnSecure
    public Client getUnsecureRestClient() {
        return unSecureClient;
    }

    @Produces
    @Secure
    public Client getRestClient() {
        return secureClient;
    }

}
