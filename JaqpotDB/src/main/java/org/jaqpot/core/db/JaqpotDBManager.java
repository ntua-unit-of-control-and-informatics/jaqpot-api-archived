/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.db;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;

/**
 *
 * @author hampos
 */
@Singleton
public class JaqpotDBManager {
    
    private JaqpotEntityManager em;
    
    
    @PostConstruct
    private void init(){
        //Get Server name from property file
        this.em = new JaqpotEntityManager("");
    }
    
    @Produces
    public JaqpotEntityManager getEntityManager(){
        return em;
    }
    
}
