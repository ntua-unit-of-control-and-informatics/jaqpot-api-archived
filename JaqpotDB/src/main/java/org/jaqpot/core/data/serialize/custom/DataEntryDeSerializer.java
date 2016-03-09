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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.jaqpot.core.model.dto.dataset.DataEntry;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
public class DataEntryDeSerializer extends StdDeserializer<DataEntry> implements ResolvableDeserializer {

    JsonDeserializer parent;

    public DataEntryDeSerializer(JsonDeserializer parent) {
        super(DataEntry.class);
        this.parent = parent;
    }

    @Override
    public DataEntry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        DataEntry dataEntry = (DataEntry) parent.deserialize(p, ctxt);
        TreeMap<String, Object> valuesMap = new TreeMap<>();
        
        for (Map.Entry<String, Object> entry : dataEntry.getValues().entrySet()) {
            valuesMap.put(entry.getKey().replaceAll("\\(DOT\\)", "\\."), entry.getValue());
        }              
        dataEntry.setValues(valuesMap);
        
        return dataEntry;
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        ((ResolvableDeserializer) parent).resolve(ctxt);
    }

}
