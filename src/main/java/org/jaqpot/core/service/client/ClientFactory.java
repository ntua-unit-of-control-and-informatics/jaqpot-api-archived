/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.net.ssl.SSLContext;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import org.apache.http.ssl.SSLContextBuilder;
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
        this.secureClient = setUpSecureClient();
    }

    private Client setUpSecureClient() {
        Client client = null;
        try {
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (certificate, authType) -> true).build();
            client = new ResteasyClientBuilder().sslContext(sslContext).hostnameVerification(ResteasyClientBuilder.HostnameVerificationPolicy.ANY)
                    .socketTimeout(30, TimeUnit.MINUTES)
                    .connectionPoolSize(10)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new InternalServerErrorException("Could not create client");
        } catch (KeyStoreException ex) {
            Logger.getLogger(ClientFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return client;
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
