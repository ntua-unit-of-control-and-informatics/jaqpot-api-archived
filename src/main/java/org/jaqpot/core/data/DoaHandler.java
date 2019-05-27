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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.Doa;

/**
 *
 * @author pantelispanka
 */
@Stateless
public class DoaHandler extends AbstractHandler<Doa> {
    
    @Inject
    @MongoDB
    JaqpotEntityManager em;
    
    
    public DoaHandler(){
        super(Doa.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }
    
    
    public Doa findBySources(String hasSources){
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.hasSources", Arrays.asList(hasSources));
        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("meta");
        fields.add("aValue");
        return em.find(Doa.class, properties, fields, 0, 1).stream().findFirst().orElse(null);
        
    }
    
    public Doa findBySourcesWithDoaMatrix(String hasSources){
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.hasSources", Arrays.asList(hasSources));
        List<String> fields = new ArrayList<>();
        fields.add("_id");
        fields.add("meta");
        fields.add("aValue");
        fields.add("doaMatrix");
        return em.find(Doa.class, properties, fields, 0, 1).stream().findFirst().orElse(null);
        
    }
    
}
