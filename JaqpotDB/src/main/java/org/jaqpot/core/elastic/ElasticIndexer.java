/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licenced
 * with the aforementioned licence. 
 */
package org.jaqpot.core.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import org.elasticsearch.action.index.IndexResponse;
import org.jaqpot.core.model.*;

/**
 *
 *
 * Used to index any Jaqpot Entity. Features the method #index which is used to
 * index
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 */
public class ElasticIndexer {

    private final JaqpotEntity entity;

    static final Map<Class<? extends JaqpotEntity>, Class<? extends AbstractMetaStripper>> STRIP_CLUB
            = new HashMap<Class<? extends JaqpotEntity>, Class<? extends AbstractMetaStripper>>() {
                {
                    //TODO: Do this with reflections!
                    put(BibTeX.class, BibTeXMetaStripper.class);
                    put(Conformer.class, ConformerMetaStripper.class);
                    put(Model.class, ModelMetaStripper.class);
                }
            };

    public ElasticIndexer(JaqpotEntity entity) {
        this.entity = entity;
    }

    public String index(ObjectMapper mapper) {
        try {
            JaqpotEntity strippedEntity = entity;
            Class<? extends JaqpotEntity> entityClass = entity.getClass();
            String entityName = entityClass.getAnnotation(XmlRootElement.class).name();
            if ("##default".equals(entityName)) {
                entityName = entityClass.getSimpleName().toLowerCase();
            }
            Class<? extends AbstractMetaStripper> stripperClass = STRIP_CLUB.get(entityClass);
            if (stripperClass != null) {
                try {
                    AbstractMetaStripper stripper = stripperClass.getConstructor(entityClass).newInstance(entity);
                    strippedEntity = stripper.strip();
                } catch (NoSuchMethodException | SecurityException | InstantiationException | 
                        IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(ElasticIndexer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("Impossible! A MetaStripper doesn't have a necessary constructor!");
                }
            }
            String jsonString = mapper.writeValueAsString(strippedEntity);
            IndexResponse response = ElasticClient.getClient().prepareIndex("jaqpot", entityName, entity.getId())
                    .setSource(jsonString)
                    .execute()
                    .actionGet();
            return response.getId();
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ElasticIndexer.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Incredible! JAKSON Couldn't serialize an entity!");
        }
    }

}
