/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.jaqpot.core.db.entitymanager;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import org.jaqpot.core.model.JaqpotEntity;

/**
 *
 * Interface used to interact with the database context. A JaqpotEntityManager
 * instance is associated with a specific database vendor mechanism and a set of
 * entities that is managed by the JaqpotEntityManager. The JaqpotEntityManager
 * API is used to create and remove persistent entity instances, to find
 * entities by their primary key, and to query over entities.
 *
 * The set of entities that can be managed by a given EntityManager instance is
 * defined by extending {@link org.jaqpot.core.model.JaqpotEntity} class.
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
public interface JaqpotEntityManager extends Closeable {

    /**
     * Makes an entity instance persistent.
     *
     * @param entity entity instance
     */
    public void persist(JaqpotEntity entity);

    /**
     * Merges the state of the entity instance to the database context. If the
     * entity already existed in the database context, the old state is
     * returned.
     *
     * @param <T>
     * @param entity entity instance
     * @return old state of entity instance
     */
    public <T extends JaqpotEntity> T merge(T entity);

    /**
     * Removes the entity instance from the database context.
     *
     * @param entity entity instance
     */
    public void remove(JaqpotEntity entity);

    /**
     * Find by primary key. Searches for an entity of the specified class and
     * primary key.
     *
     * @param <T>
     * @param entityClass entity class
     * @param primaryKey primary key
     * @return the found entity instance or null if the entity does not exist
     */
    public <T extends JaqpotEntity> T find(Class<T> entityClass, Object primaryKey);

    /**
     * Find by properties. Searches for entities of the specified class that
     * match the given properties.
     *
     * This method has paging capability.
     *
     * @param <T>
     * @param entityClass entity class
     * @param properties a properties map matching field names with values
     * @param start the position of the first result to retrieve
     * @param max the maximum number of results to retrieve
     * @return a list of entity instances that match the given properties
     */
    public <T extends JaqpotEntity> List<T> find(Class<T> entityClass, Map<String, Object> properties, Integer start, Integer max);

    /**
     * Count by properties. Counts the entities of the specified class that
     * match the given properties.
     *
     *
     * @param <T>
     * @param entityClass entity class
     * @param properties a properties map matching field names with values
     * @return the total of entity instances that match the given properties
     */
    public <T extends JaqpotEntity> Long count(Class<T> entityClass, Map<String, Object> properties);

    /**
     * Find by primary keys. Searches for entities of the specified class that
     * match the given primary keys. The returned entity instances will only
     * contain fields that are present in the fields list.
     *
     *
     * @param <T>
     * @param entityClass entity class
     * @param keys list of primary keys
     * @param fields list of fields to be returned
     * @return a list of entity instances that match the given primary keys
     */
    public <T extends JaqpotEntity> List<T> find(Class<T> entityClass, List<String> keys, List<String> fields);

    /**
     * Find by properties, return specific fields. Searches for entities of the
     * specified class that match the given properties but returns only fields
     * that are present in the fields parameter.
     *
     * This method has paging capability.
     *
     * @param <T>
     * @param entityClass entity class
     * @param properties a properties map matching field names with values
     * @param fields a list of fields to be returned
     * @param start the position of the first result to retrieve
     * @param max the maximum number of results to retrieve
     * @return the total of entity instances that match the given properties
     */
    public <T extends JaqpotEntity> List<T> find(Class<T> entityClass, Map<String, Object> properties, List<String> fields, Integer start, Integer max);
   
    /**
     * Find all entities. Searches for all entities of the specified class with keys to search.
     *
     * This method has paging capability.
     *
     * @param <T>
     * @param entityClass entity class
     * @param start the position of the first result to retrieve
     * @param max the maximum number of results to retrieve
     * @param properties the properties upon to search
     * @return a list with all entity instances of given class
     */
    public <T extends JaqpotEntity> List<T> findAllWithReqexp(Class<T> entityClass, Map<String, Object> properties, List<String> fields, Integer start, Integer max);
    
    
    /**
     * Find all entities. Searches for all entities of the specified class.
     *
     * This method has paging capability.
     *
     * @param <T>
     * @param entityClass entity class
     * @param start the position of the first result to retrieve
     * @param max the maximum number of results to retrieve
     * @return a list with all entity instances of given class
     */
    public <T extends JaqpotEntity> List<T> findAll(Class<T> entityClass, Integer start, Integer max);

    /**
     * Find all entities, return specific fields. Searches for all entities of
     * the specified class but returns only fields that are present in the
     * fields parameter.
     *
     * This method has paging capability.
     *
     * @param <T>
     * @param entityClass entity class
     * @param fields a list of fields to be returned
     * @param start the position of the first result to retrieve
     * @param max the maximum number of results to retrieve
     * @return a list with all entity instances of given class, each instance
     * containing only specified fields
     */
    public <T extends JaqpotEntity> List<T> findAll(Class<T> entityClass, List<String> fields, Integer start, Integer max);

    /**
     * Count all entities. Counts all persisted entities of the specified class.
     *
     *
     * @param <T>
     * @param entityClass entity class
     * @return the total of entity instances that match the given properties
     */
    public <T extends JaqpotEntity> Long countAll(Class<T> entityClass);

    /**
     * Return specific fields of entity found by primaryKey.
     *
     * @param <T>
     * @param entityClass entity class
     * @param fields a list of fields to be returned
     * @param primaryKey primary key
     * @return only the specified fields of the entity with given primary key
     */
    public <T extends JaqpotEntity> T find(Class<T> entityClass, Object primaryKey, List<String> fields);

    public <T extends JaqpotEntity> List<T> findSorted(Class<T> entityClass, Map<String, Object> properties, List<String> fields, Integer start, Integer max, List<String> ascendingFields, List<String> descendingFields);
    
    public <T extends JaqpotEntity> List<T> findSortedAsc(Class<T> entityClass, Map<String, Object> properties, List<String> fields, Integer start, Integer max, List<String> ascendingFields);
    
    public <T extends JaqpotEntity> List<T> findSortedDesc(Class<T> entityClass, Map<String, Object> properties, List<String> fields, Integer start, Integer max, List<String> descendingFields);

    public <T extends JaqpotEntity> List<T> findSorted(Class<T> entityClass, Map<String, Object> properties, Integer start, Integer max, List<String> ascendingFields, List<String> descendingFields);
    
    public <T extends JaqpotEntity> List<T> findSortedAsc(Class<T> entityClass, Map<String, Object> properties, Integer start, Integer max, List<String> ascendingFields);
    
    public <T extends JaqpotEntity> List<T> findSortedDesc(Class<T> entityClass, Map<String, Object> properties, Integer start, Integer max, List<String> descendingFields);
}
