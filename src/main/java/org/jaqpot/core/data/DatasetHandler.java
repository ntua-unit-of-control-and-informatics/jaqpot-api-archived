/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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

import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@Stateless
public class DatasetHandler extends AbstractHandler<Dataset> {
    
    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public DatasetHandler() {
        super(Dataset.class);
    }
    
    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }
    
    @Override
    public void create(Dataset dataset) throws IllegalArgumentException, JaqpotDocumentSizeExceededException {

        if (dataset.toString().length() > 14000000)
            throw new JaqpotDocumentSizeExceededException("Resulting Dataset exceeds limit of 14mb on Dataset Resources");
        super.create(dataset);
    }
    
    @Override
    public void edit(Dataset dataset) throws IllegalArgumentException {
        HashSet<String> features = dataset.getFeatures().stream().map(FeatureInfo::getURI).collect(Collectors.toCollection(HashSet::new));
        for (DataEntry dataEntry : dataset.getDataEntry()) {
            HashSet<String> entryFeatures = new HashSet<>(dataEntry.getValues().keySet());
            if (!entryFeatures.equals(features)) {
                throw new IllegalArgumentException("Invalid Dataset - DataEntry URIs do not match with Feature URIs. "
                        + " Problem was found when parsing " + dataEntry.getEntryId() + "On dataset" + dataset.getId());
            }
        }
        getEntityManager().merge(dataset);
    }
    
    public Dataset find(Object id) {
        Dataset dataset = em.find(Dataset.class, id);
        if (dataset == null) {
            return null;
        }
        return dataset;
    }
    
    public List<Dataset> listDatasetCreatorsExistence(String creator, Dataset.DatasetExistence existence, Integer start, Integer max){
        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("meta");
        fields.add("ontologicalClasses");
        fields.add("organizations");
        fields.add("totalRows");
        fields.add("totalColumns");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(creator));
        properties.put("existence", existence.getName().toUpperCase());
        return em.find(Dataset.class, properties, fields, start, max);
    }
    
    public List<Dataset> listDatasetOrgsExistence(String organization, Dataset.DatasetExistence existence, Integer start, Integer max){
        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("meta");
        fields.add("ontologicalClasses");
        fields.add("organizations");
        fields.add("totalRows");
        fields.add("totalColumns");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("organizations", Arrays.asList(organization));
        properties.put("existence", existence.getName().toUpperCase());
        return em.find(Dataset.class, properties, fields, start, max);
    }
    
    public Number countCreatorsExistenseDatasets(String creator, Dataset.DatasetExistence existence){
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(creator));
        properties.put("existence", existence.getName().toUpperCase());
        properties.put("visible", true);
        return getEntityManager().count(entityClass, properties);
    }
    
    
}
