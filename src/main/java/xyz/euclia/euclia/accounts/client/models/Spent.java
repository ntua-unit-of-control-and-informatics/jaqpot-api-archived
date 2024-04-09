/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client.models;

import java.util.HashMap;

/**
 *
 * @author pantelispanka
 */
public class Spent {
    
    private String appid;
    private HashMap<String, Object> usage;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public HashMap<String, Object> getUsage() {
        return usage;
    }

    public void setUsage(HashMap<String, Object> usage) {
        this.usage = usage;
    }
    
    
    
    
}
