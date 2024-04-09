/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client.consumers;

import org.asynchttpclient.AsyncHttpClient;
import xyz.euclia.euclia.accounts.client.models.Organization;

/**
 *
 * @author pantelispanka
 */
public class OrganizationConsumer extends BaseConsumer<Organization> {
    
    private static final String orgsPath = "/organizations";
    
    public OrganizationConsumer(AsyncHttpClient client, String baseUrl){
        super(client, baseUrl, orgsPath);
    }
    
    
}
