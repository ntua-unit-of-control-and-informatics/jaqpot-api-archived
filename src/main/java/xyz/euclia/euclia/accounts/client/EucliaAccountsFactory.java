/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import xyz.euclia.euclia.accounts.client.consumers.OrganizationConsumer;
import xyz.euclia.euclia.accounts.client.consumers.UserConsumer;

/**
 *
 * @author pantelispanka
 */
public class EucliaAccountsFactory {

    public static EucliaAccounts createNewClient(String basePath){
        AsyncHttpClient httpClient = ClientFactory.INSTANCE.getClient();
        UserConsumer userConsumer = new UserConsumer(httpClient, basePath);
        OrganizationConsumer orgConsumer = new OrganizationConsumer(httpClient, basePath);
        EucliaAccounts client = new EucliaAccountsImplementation(basePath, httpClient, userConsumer,orgConsumer);
        return client;
    }
    
    
    private enum ClientFactory {

        INSTANCE;

        private DefaultAsyncHttpClient s;

        ClientFactory() {
            AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
                    .setPooledConnectionIdleTimeout(500)
                    .setMaxConnections(20000)
                    //                    .setAcceptAnyCertificate(true)
                    .setMaxConnectionsPerHost(5000).build();

            s = new DefaultAsyncHttpClient(config);

        }

        public AsyncHttpClient getClient() {
            return s;
        }
    }

    public AsyncHttpClient getClient() {
        return ClientFactory.INSTANCE.getClient();
    }

}
