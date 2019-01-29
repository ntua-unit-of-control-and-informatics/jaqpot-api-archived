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
package org.jaqpot.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.JaqpotEntity;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;


/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 * @param <T> Entity Type to be handled by the Handler.
 *
 */
public abstract class AbstractHandler<T extends JaqpotEntity>  {

    final Class<T> entityClass;

    public AbstractHandler(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract JaqpotEntityManager getEntityManager();

    public void create(T entity) throws JaqpotDocumentSizeExceededException {
        if (entity.getMeta() != null) {
            entity.getMeta().setDate(new Date());
        }
        getEntityManager().persist(entity);
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    public void updateField(Object id, String key, Object value) {
        getEntityManager().updateField(entityClass,id,key,value);
    }
    
    public void updateMeta(Object id, MetaInfo meta){
        getEntityManager().updateMeta(entityClass, id, meta);
    }

    public void remove(T entity) {
        getEntityManager().remove(entity);
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public T find(Object id, List<String> fields) {
        return getEntityManager().find(entityClass, id, fields);
    }

    public T findMeta(Object id)
    {
        List<String> fields = new ArrayList<>();
        fields.add("totalColumns");
        fields.add("totalRows");
        fields.add("features");
        return getEntityManager().find(entityClass, id, fields);
    }
    
//    public List<T> findInArray(Map<String, Object> properties, List<String> fields, Integer start, Integer max){
//        return getEntityManager().findInArray(entityClass, properties, fields, start, max);
//    }

    public List<T> find(Map<String, Object> properties) {
        return getEntityManager().find(entityClass, properties, 0, Integer.MAX_VALUE);
    }
    
    public List<T> find(Map<String, Object> properties, List<String> fields, Integer start, Integer max) {
        return getEntityManager().find(entityClass, properties, fields, 0, Integer.MAX_VALUE);
    }
    
    public List<T> findAll() {
        return getEntityManager().findAll(entityClass, 0, Integer.MAX_VALUE);
    }

    public List<T> findAll(Integer start, Integer max) {
        return getEntityManager().findAll(entityClass, start, max);
    }
    
    public List<T> findAllAndNe( Map<String, Object> properties,Map<String, Object> neProperties, List<String> fields, Integer start, Integer max){
        return getEntityManager().findAndNe(entityClass, properties, neProperties, fields, start, max);
    }

    public List<T> findFeatured(Integer start, Integer max) {
        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("meta");
        fields.add("ontologicalClasses");

        Map<String, Object> properties = new HashMap<>();
        properties.put("featured", true);

        return getEntityManager().find(entityClass, properties, fields, start, max);
    }

    public Long countFeatured() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("featured", true);

        return getEntityManager().count(entityClass, properties);
    }

    public List<T> listMeta(Integer start, Integer max) {
        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("meta");
        fields.add("ontologicalClasses");
        return getEntityManager().findAll(entityClass, fields, start, max);
    }

    public List<T> listMetaOfCreator(String createdBy, Integer start, Integer max) {
        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("meta");
        fields.add("ontologicalClasses");

        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(createdBy));
        properties.put("visible", true);
        Map<String, Object> notProperties = new HashMap<>();
        notProperties.put("onTrash", true);
        
        return getEntityManager().findSortedDescAndNe(entityClass, properties, notProperties , fields, start, max, Arrays.asList("meta.date"));
    }

    public Long countAll() {
        return getEntityManager().countAll(entityClass);
    }

    public Long countAllOfCreator(String createdBy) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(createdBy));
        properties.put("visible", true);
        Map<String, Object> notProperties = new HashMap<>();
        notProperties.put("onTrash", true);
        return getEntityManager().countAndNe(entityClass, properties, notProperties);
    }
    
    public Long countAllOfOrg(String createdBy, String organization) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(createdBy));
        properties.put("visible", true);
//        properties.put("meta.read", organization);
        Map<String, Object> notProperties = new HashMap<>();
        notProperties.put("onTrash", true);
        return getEntityManager().countAndNe(entityClass, properties, notProperties);
    }
    
    public Long countCreatorsInTrash(String createdBy){
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(createdBy));
        properties.put("onTrash", true);
        return getEntityManager().count(entityClass, properties);
    }

}
