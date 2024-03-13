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
package org.jaqpot.core.model.factory;

import java.util.HashSet;
import java.util.Set;
import org.jaqpot.core.model.Feature;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.util.ROG;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 */
public class FeatureFactory {

    
    public static Feature predictionFeature(Feature feature) {
        Feature newFeature = new Feature();
        
        MetaInfo mf = MetaInfoBuilder.builder(feature.getMeta()).build();
        mf.getHasSources().clear();
        Set<String> sources = new HashSet();
        sources.add("feature/" + feature.getId());
        mf.setHasSources(sources);
        newFeature.setMeta(mf);
        if(feature.getUnits() != null){
            newFeature.setUnits(feature.getUnits());
        }   
        newFeature.setCategory(Dataset.DescriptorCategory.PREDICTED);
//        newFeature = featHandler.create(newFeature);
        ROG randomStringGenerator = new ROG(true);
        newFeature.setId(randomStringGenerator.nextString(20));
        return newFeature;
    }

}
