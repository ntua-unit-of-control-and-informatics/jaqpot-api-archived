/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client;

import org.asynchttpclient.Response;

import java.io.Closeable;
import java.util.concurrent.Future;

/**
 *
 * @author pantelispanka
 */
public interface EucliaAccounts extends Closeable {

    public Future<Response>  getUser(String id, String apiKey);
    public  Future<Response>  getOrganization(String id, String apiKey);

}
