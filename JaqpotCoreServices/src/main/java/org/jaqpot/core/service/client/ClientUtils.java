/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.client;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public class ClientUtils {

    public static Client buildUnsecureRestClient() throws GeneralSecurityException {
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
    }

}
