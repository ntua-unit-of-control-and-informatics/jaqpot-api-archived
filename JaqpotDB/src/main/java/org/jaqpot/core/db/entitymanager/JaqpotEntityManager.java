/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.db.entitymanager;

import java.util.Map;

/**
 *
 * @author hampos
 */
public interface JaqpotEntityManager {

    public void persist(Object entity);

    public <T> T merge(T entity);

    public void remove(Object entity);

    public <T> T find(Class<T> entityClass, Object primaryKey);

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties);

}
