/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import xyz.euclia.euclia.accounts.client.consumers.OrganizationConsumer;
import xyz.euclia.euclia.accounts.client.consumers.UserConsumer;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 *
 * @author pantelispanka
 */
public class EucliaAccountsImplementation implements EucliaAccounts {


    private final String basePath;
    private final AsyncHttpClient client;
    private final UserConsumer userConsumer;
    private final OrganizationConsumer orgConsumer;

    public EucliaAccountsImplementation(String basePath, AsyncHttpClient client, UserConsumer userConsumer, OrganizationConsumer orgConsumer){
        this.basePath = basePath;
        this.client = client;
        this.userConsumer = userConsumer;
        this.orgConsumer = orgConsumer;
    }

    @Override
    public Future<Response> getUser(String id, String apiKey) {
        return this.userConsumer.getWithIdParam(id, apiKey);
    }

    @Override
    public Future<Response> getOrganization(String id, String apiKey) {
        return this.orgConsumer.getWithIdUrl(id, apiKey);
    }

    @Override
    public void close() throws IOException {
        this.client.close();
    }

}
