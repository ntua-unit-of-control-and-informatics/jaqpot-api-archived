/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.jaqpot.core.service.annotations.Secure;
import org.jaqpot.core.service.annotations.UnSecure;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Dependent
public class ClientFactory {

    private static final Logger LOG = Logger.getLogger(ClientFactory.class.getName());

    @Produces
    @UnSecure
    public Client getUnsecureRestClient() {
        try {
            SSLContext context = SSLContext.getInstance("TLSv1");
            TrustManager[] trustManagerArray = {
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };
            context.init(null, trustManagerArray, null);
            return ClientBuilder.newBuilder()
                    .hostnameVerifier((String string, SSLSession ssls) -> true)
                    .sslContext(context)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
            return null;
        }
    }

    @Produces
    @Secure
    public Client getRestClient() {
        return ClientBuilder.newClient();
    }

}
