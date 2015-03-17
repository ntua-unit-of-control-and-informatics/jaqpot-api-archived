/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.security;

import java.security.Principal;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author hampos
 */
public class SecurityContextImpl implements SecurityContext {

    Principal userPrincipal;

    public SecurityContextImpl(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "subjectid";
    }

}
