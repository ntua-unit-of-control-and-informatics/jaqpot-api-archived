/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.dto.aa;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jaqpot.core.model.JaqpotEntity;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
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


    @Override
    public String toString() {
        return authToken;
    }

}
