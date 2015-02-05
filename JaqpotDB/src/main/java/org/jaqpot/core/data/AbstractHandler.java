/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data;

import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;

/**
 *
 * @author hampos
 */
public abstract class AbstractHandler<T> {
    
    private final Class<T> entityClass;
    
    public AbstractHandler(Class<T> entityClass){
        this.entityClass = entityClass;
    }
    
    protected abstract JaqpotEntityManager getEntityManager();
    
    public void create(T entity){
        
    }
    
    public void edit(T entity){
        
    }
    
    public void remove(T entity){
        
    }
}
