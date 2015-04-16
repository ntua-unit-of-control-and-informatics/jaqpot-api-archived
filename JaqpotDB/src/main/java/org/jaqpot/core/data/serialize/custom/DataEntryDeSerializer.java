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
package org.jaqpot.core.data.serialize.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.jaqpot.core.model.dto.dataset.DataEntry;
import org.jaqpot.core.model.dto.dataset.Substance;

/**
 *
 * @author hampos
 */
public class DataEntryDeSerializer extends JsonDeserializer<DataEntry> {

    @Override
    public DataEntry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        TreeNode tree = p.readValueAsTree();

        Substance compound = new Substance();
        compound.setURI(tree.get("compound").get("URI").traverse().nextTextValue());

        TreeMap<String, Object> valuesMap = new TreeMap<>();

        TreeNode values = tree.get("values");
        Iterator<String> it = values.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            Double value = values.get(key).traverse().getDoubleValue();
            valuesMap.put(key.replaceAll("\\(DOT\\)", "\\."), value);
        }

        DataEntry dataEntry = new DataEntry();
        dataEntry.setCompound(compound);
        dataEntry.setValues(valuesMap);
        return dataEntry;
    }

}
