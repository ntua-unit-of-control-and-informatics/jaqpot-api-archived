/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    public Feature findByTitleAndSource(String title, String source) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.titles", Arrays.asList(title));
        properties.put("meta.hasSources", Arrays.asList(source));
        List<Feature> features = this.find(properties);
        return features.stream().findFirst().orElse(null);
    }
    
}
