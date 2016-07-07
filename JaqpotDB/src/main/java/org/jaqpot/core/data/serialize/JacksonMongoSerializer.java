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
package org.jaqpot.core.data.serialize;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.data.serialize.custom.DataEntryDeSerializeModifier;
import org.jaqpot.core.data.serialize.custom.DataEntryDeSerializer;
import org.jaqpot.core.data.serialize.custom.DataEntrySerializer;
import org.jaqpot.core.model.dto.dataset.DataEntry;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 */
@MongoDB
@Dependent
public class JacksonMongoSerializer implements JSONSerializer {

    private static final Logger LOG = Logger.getLogger(JacksonJSONSerializer.class.getName());

    ObjectMapper mapper;

    public JacksonMongoSerializer() {
        this.mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataEntry.class, new DataEntrySerializer());
        mapper.registerModule(module.setDeserializerModifier(new DataEntryDeSerializeModifier()));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void write(Object entity, OutputStream out) {
        try {
            mapper.writeValue(out, entity);
        } catch (IOException ex) {
            throw new JaqpotSerializationException(ex);
        }
    }

    @Override
    public void write(Object entity, Writer writer) {
        try {
            mapper.writeValue(writer, entity);
        } catch (IOException ex) {
            throw new JaqpotSerializationException(ex);
        }
    }

    @Override
    public String write(Object entity) {
        try {
            String result = mapper.writeValueAsString(entity);
            //for(int i=0; i<20 ; i++){
         //   while (true) {
                String temp = result.replaceAll("([A-Za-z*])(\\.)([A-Za-z0-9*])", "$1\\(DOT\\)$3");
//                if (temp.equals(result)) {
//                    result = temp;
//                    break;
//                }
                result = temp;
           // }
            //}
            return result;
        } catch (IOException ex) {
            throw new JaqpotSerializationException(ex);
        }
    }

    @Override
    public <T> T parse(String content, Class<T> valueType) {
        try {
            return mapper.readValue(content.replaceAll("\\(DOT\\)", "\\."), valueType);
        } catch (IOException ex) {
            throw new JaqpotSerializationException(ex);
        }
    }

    @Override
    public <T> T parse(InputStream src, Class<T> valueType) {
        try {
            return mapper.readValue(src, valueType);
        } catch (IOException ex) {
            throw new JaqpotSerializationException(ex);
        }
    }

    @Override
    public <T> T patch(Object entity, String patchJson, Class<T> valueType) {
        try {
            JsonNode patchAsNode = mapper.readTree(patchJson);
            JsonPatch patchTool = JsonPatch.fromJson(patchAsNode);
            JsonNode entityAsNode = mapper.valueToTree(entity);
            JsonNode modifiedAsNode = patchTool.apply(entityAsNode);
            return mapper.treeToValue(modifiedAsNode, valueType);
        } catch (IOException | JsonPatchException ex) {
            throw new JaqpotSerializationException(ex);
        }
    }
}
