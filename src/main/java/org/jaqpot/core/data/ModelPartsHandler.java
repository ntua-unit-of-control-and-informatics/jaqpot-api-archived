/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jaqpot.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.ModelParts;
import org.jaqpot.core.model.dto.dataset.Dataset;

/**
 *
 * @author pantelispanka
 */
@Stateless
public class ModelPartsHandler extends AbstractHandler<ModelParts> {

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public ModelPartsHandler() {
        super(ModelParts.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }

    public long countModelPartsUploaded(String creator, String modelId) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", Arrays.asList(creator));
        properties.put("modelId", modelId);
        return getEntityManager().count(entityClass, properties);
    }

    public List<ModelParts> getParts(String creator, String modelId) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", Arrays.asList(creator));
        properties.put("modelId", modelId);
        List<String> asc = new ArrayList();
        asc.add("partNumber");
        return getEntityManager().findSortedAsc(entityClass, properties, 0, 1000, asc);
    }

    public List<ModelParts> getPartsOfModel(String modelId) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("modelId", modelId);
        List<String> asc = new ArrayList();
        asc.add("partNumber");
        
        return getEntityManager().findSortedAsc(entityClass, properties, 0, 1000, asc);
    }

}
