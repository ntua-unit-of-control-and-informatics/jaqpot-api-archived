/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.jaqpot.core.db.entitymanager;

import java.io.Closeable;
import java.io.InputStream;
import java.util.ArrayList;
import javax.ws.rs.InternalServerErrorException;
import org.jaqpot.core.model.JaqpotEntity;

/**
 *
 * @author pantelispanka
 */
public interface JaqpotGridFS extends Closeable{
    
    
    /**
     * Makes an entity instance persistent at grid fs for large models and objects.
     *
     * @param stream string stream of model
     * @param entity entity instance
     */
    public void persist(InputStream stream, JaqpotEntity j);
    
    
    public String getObject(String id) throws InternalServerErrorException;
    
}
