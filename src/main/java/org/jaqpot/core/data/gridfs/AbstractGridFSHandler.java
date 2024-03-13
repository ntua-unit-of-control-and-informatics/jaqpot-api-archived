/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jaqpot.core.data.gridfs;


import java.io.InputStream;
import javax.ws.rs.InternalServerErrorException;
import org.jaqpot.core.db.entitymanager.JaqpotGridFS;
import org.jaqpot.core.model.JaqpotEntity;

/**
 *
 * @author pantelispanka
 */
public abstract class AbstractGridFSHandler <T extends JaqpotEntity> {
    
    final Class<T> entityClass;

    public AbstractGridFSHandler(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract JaqpotGridFS getGridFSManager();
    
    
    public void persist(InputStream stream, T j){
        getGridFSManager().persist(stream, j);
    }
    
    
    public String getObject(String id) throws InternalServerErrorException{
        return getGridFSManager().getObject(id);
    }
    
}
