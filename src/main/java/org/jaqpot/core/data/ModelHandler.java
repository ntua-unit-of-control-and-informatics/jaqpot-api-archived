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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import org.bson.BsonMaximumSizeExceededException;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.data.gridfs.ModelGridFSHandler;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.ModelParts;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@Stateless
public class ModelHandler extends AbstractHandler<Model> {

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    @EJB
    ModelGridFSHandler modelGridFSHandler;

    @EJB
    ModelPartsHandler modelPartsHandler;
    
    
    private static volatile Instrumentation globalInstrumentation;

    public ModelHandler() {
        super(Model.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }

    public void updateActualModel(String id, String actualModel) throws IOException, BsonMaximumSizeExceededException , JaqpotDocumentSizeExceededException , EJBTransactionRolledbackException {
        try {
            em.updateField(Model.class, id, "actualModel", actualModel);
        } catch (JaqpotDocumentSizeExceededException e) {
            byte[] bytes = actualModel.toString().getBytes(StandardCharsets.UTF_8);

            InputStream in = new ByteArrayInputStream(bytes);
            Model m = em.find(Model.class, id);
            modelGridFSHandler.persist(in, m);
            in.close();
        }

    }

    public Model findActualModel(String id) {
        List<String> keys = new ArrayList<>();
        keys.add(id);

        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("﻿actualModel");
        return em.find(Model.class, keys, fields).stream().findFirst().orElse(null);
    }

    public Model findModelPmml(String id) {
        List<String> keys = new ArrayList<>();
        keys.add(id);

        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("pmmlModel");
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
        fields.add("dependentFeatures");
        fields.add("independentFeatures");
        fields.add("predictedFeatures");
        fields.add("algorithm");
        fields.add("bibtex");
        fields.add("datasetUri");
        fields.add("parameters");
        fields.add("organizations");
        fields.add("meta");
        return em.findAll(Model.class, fields, 0, Integer.MAX_VALUE);
    }

    public Model findModel(String id) {
        List<String> keys = new ArrayList<>();
        keys.add(id);

        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("meta");
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
        fields.add("additionalInfo");
        fields.add("meta");
        fields.add("libraries");
        fields.add("libraryVersions");
        fields.add("type");
        return em.find(Model.class, keys, fields).stream().findFirst().orElse(null);
    }

    public Long countAllOfAlgos(String user, String algo) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(user));
        properties.put("algorithm._id", algo);
        Map<String, Object> notProperties = new HashMap<>();
        notProperties.put("onTrash", true);
        return getEntityManager().countAndNe(Model.class, properties, notProperties);
    }

    @Override
    public Model find(Object id) {
        Model model = getEntityManager().find(Model.class, id);

        
        if (model.getActualModel() == null) {
            
            StringJoiner s = new StringJoiner("");
            String oid = (String) id;
            List<ModelParts> partsStored = modelPartsHandler.getPartsOfModel(oid);
            for (ModelParts mp : partsStored) {
                s.add(mp.getPart());
            }
            Set<String> actualModel = new HashSet();
            actualModel.add(s.toString());
            
            
            model.setActualModel(actualModel);
//            String oid = (String) id;
//            String actualModelString = new String();
//            ArrayList<String> actualModelS = new ArrayList();
//            actualModelString = this.modelGridFSHandler.getObject(oid);
//            Object actualModel = actualModelString;
//            actualModelS.add(actualModelString);
//            model.setActualModel(actualModelString);
        }

        return model;
    }

}
