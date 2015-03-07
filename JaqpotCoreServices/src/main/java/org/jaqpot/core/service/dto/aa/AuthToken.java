/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.dto.aa;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author chung
 */
@XmlRootElement
public class AuthToken {

    private String userName;
    private String authToken;

    public AuthToken() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

}
