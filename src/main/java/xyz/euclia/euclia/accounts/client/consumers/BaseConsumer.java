/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client.consumers;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Param;
import org.asynchttpclient.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 * @author pantelispanka
 */
public abstract class BaseConsumer <T> {

    private final AsyncHttpClient client;
//    private final Serializer serializer;
    private final String accountsBaseUrl;
    private final String path;

    public BaseConsumer(AsyncHttpClient client, String baseUrl, String path){
        this.client = client;
        this.accountsBaseUrl = baseUrl;
        this.path = this.accountsBaseUrl + path;
    }


    public  Future<Response>  getWithIdParam(String id, String apiKey){
        List<Param> params = new ArrayList();
        Param a = new Param("min", null);
        Param b = new Param("max", null);
        Param c = new Param("email", null);
        Param d = new Param("id", id);
        params.add(a);
        params.add(b);
        params.add(c);
        params.add(d);
        Future<Response> f = this.client.prepareGet(path)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type","application/json")
                .addQueryParams(params)
                .execute();
        return f;
    }

        public  Future<Response>  getWithIdUrl(String id, String apiKey){
        String pathWithId = this.path + "/" + id;
        Future<Response> f = this.client.prepareGet(pathWithId)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type","application/json")
                .execute();
        return f;
    }

}
