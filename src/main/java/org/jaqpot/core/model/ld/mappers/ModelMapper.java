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
package org.jaqpot.core.model.ld.mappers;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Model;
import org.jaqpot.core.model.ld.ModelLD;

/**
 *
 * @author Charalampos Chomenidis
 * @author Angelos Valsamis
 *
 */
@Dependent
public class ModelMapper extends BaseMapper implements IMapper<Model, ModelLD> {

    @Override
    public ModelLD map(Model input, String basePath) {
        String id = input.getId();
        String url = basePath + "/model/" + id;

        ModelLD model = new ModelLD(url);
        MetaInfo meta = input.getMeta();
        model = (ModelLD) mapMeta(meta, model);

        model.setDependentFeatures(input.getDependentFeatures());
        model.setIndependentFeatures(input.getIndependentFeatures());
        model.setPredictedFeatures(input.getPredictedFeatures());
        model.setTransformationModels(input.getTransformationModels());
        model.setLinkedModels(input.getLinkedModels());

        String algorithmId = input.getAlgorithm().getId();
        String algorithmUrl = basePath + "/algorithm/" + algorithmId;
        model.setAlgorithm(algorithmUrl);

        if (input.getParameters() != null) {
            Map<String, Object> parameters = new HashMap<>();
            for (Map.Entry<String, Object> entry : input.getParameters().entrySet()) {
                String parameterUrl = algorithmUrl + "/parameter/" + entry.getKey();
                Object parameterValue = entry.getValue();
                parameters.put(parameterUrl, parameterValue);
            }
            model.setParameters(parameters);
        }
        return model;
    }

}
