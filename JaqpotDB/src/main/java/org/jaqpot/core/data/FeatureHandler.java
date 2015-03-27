/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data;

import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.Feature;

/**
 *
 * @author chung
 */
@Stateless
public class FeatureHandler extends AbstractHandler<Feature> {

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public FeatureHandler() {
        super(Feature.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }

}
