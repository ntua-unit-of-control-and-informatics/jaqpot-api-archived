/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jaqpot.core.data.gridfs;

import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotGridFS;
import org.jaqpot.core.model.Model;

/**
 *
 * @author pantelispanka
 */
@Stateless
public class ModelGridFSHandler extends AbstractGridFSHandler<Model>{

    @Inject
    @MongoDB
    JaqpotGridFS em;

    public ModelGridFSHandler() {
        super(Model.class);
    }


    @Override
    protected JaqpotGridFS getGridFSManager() {
        return em;
    }

}
