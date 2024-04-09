/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client.consumers;

import org.asynchttpclient.AsyncHttpClient;
import xyz.euclia.euclia.accounts.client.models.User;

/**
 *
 * @author pantelispanka
 */
public class UserConsumer extends BaseConsumer<User> {
    
    private static final String usersPath = "/users";
    
    public UserConsumer(AsyncHttpClient client, String baseUrl){
        super(client, baseUrl, usersPath);
    }
}
