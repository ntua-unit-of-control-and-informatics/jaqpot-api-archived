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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.Model;

/**
 *
 * @author hampos
 */
@Stateless
public class ModelHandler extends AbstractHandler<Model> {

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public ModelHandler() {
        super(Model.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }

    public Model findModelPmml(String id) {
        List<String> keys = new ArrayList<>();
        keys.add(id);

        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("pmmlModel");
        return em.find(Model.class, keys, fields).stream().findFirst().orElse(null);
    }

    public Model findModelMeta(String id) {
        List<String> keys = new ArrayList<>();
        keys.add(id);

        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("createdBy");
        fields.add("dependentFeatures");
        fields.add("independentFeatures");
        fields.add("predictedFeatures");
        fields.add("algorithm");
        fields.add("bibtex");
        fields.add("datasetUri");
        fields.add("parameters");
        fields.add("doaModel");
        fields.add("transformationModels");
        fields.add("linkedModels");

        return em.find(Model.class, keys, fields).stream().findFirst().orElse(null);
    }

    public Model findModelPredictedFeatures(String id) {
        List<String> keys = new ArrayList<>();
        keys.add(id);

        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("predictedFeatures");
        return em.find(Model.class, keys, fields).stream().findFirst().orElse(null);
    }

    public Model findModelIndependentFeatures(String id) {
        List<String> keys = new ArrayList<>();
        keys.add(id);

        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("independentFeatures");
        return em.find(Model.class, keys, fields).stream().findFirst().orElse(null);
    }

    public Model findModelDependentFeatures(String id) {
        List<String> keys = new ArrayList<>();
        keys.add(id);

        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("dependentFeatures");
        return em.find(Model.class, keys, fields).stream().findFirst().orElse(null);
    }

    public List<Model> findAllMeta() {
        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("createdBy");
        fields.add("dependentFeatures");
        fields.add("independentFeatures");
        fields.add("predictedFeatures");
        fields.add("algorithm");
        fields.add("bibtex");
        fields.add("datasetUri");
        fields.add("parameters");

        return em.findAll(Model.class, fields, 0, Integer.MAX_VALUE);
    }

    public Model findModel(String id) {
        List<String> keys = new ArrayList<>();
        keys.add(id);

        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("meta");
        fields.add("createdBy");
        fields.add("dependentFeatures");
        fields.add("independentFeatures");
        fields.add("predictedFeatures");
        fields.add("algorithm");
        fields.add("bibtex");
        fields.add("datasetUri");
        fields.add("parameters");
        fields.add("doaModel");
        fields.add("transformationModels");
        fields.add("linkedModels");

        return em.find(Model.class, keys, fields).stream().findFirst().orElse(null);
    }

    public Long countByUser(String userName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("createdBy", userName);
        return em.count(Model.class, properties);
    }

}
