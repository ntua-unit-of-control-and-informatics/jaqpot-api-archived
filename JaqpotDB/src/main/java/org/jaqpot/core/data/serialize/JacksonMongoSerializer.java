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
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void write(Object entity, Writer writer) {
        try {
            mapper.writeValue(writer, entity);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String write(Object entity) {
        try {
            String result = mapper.writeValueAsString(entity);
            //for(int i=0; i<20 ; i++){
            while (true) {
                String temp = result.replaceAll("(\"[^\"]*)(\\.)([^\"]*\":)", "$1\\(DOT\\)$3");
                if (temp.equals(result)) {
                    result = temp;
                    break;
                }
                result = temp;
            }
            //}
            return result;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public <T> T parse(String content, Class<T> valueType) {
        try {
            return mapper.readValue(content.replaceAll("\\(DOT\\)", "\\."), valueType);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public <T> T parse(InputStream src, Class<T> valueType) {
        try {
            return mapper.readValue(src, valueType);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
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
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static void main(String[] args) {
        JacksonMongoSerializer s = new JacksonMongoSerializer();
        Object o = s.parse("{\n"
                + "		\"design\": [\n"
                + "			{\n"
                + "				\"Comp.1\": -101.3422,\n"
                + "				\"Comp.2\": -9.9767\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": -83.4677,\n"
                + "				\"Comp.2\": 50.4427\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": 456.1221,\n"
                + "				\"Comp.2\": -24.0812\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": 311.1677,\n"
                + "				\"Comp.2\": 38.0699\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": 341.8513,\n"
                + "				\"Comp.2\": 48.2868\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": 494.8896,\n"
                + "				\"Comp.2\": -39.8123\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": 455.0816,\n"
                + "				\"Comp.2\": 26.5578\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": 489.5801,\n"
                + "				\"Comp.2\": -23.6909\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": 399.3027,\n"
                + "				\"Comp.2\": 13.9503\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": 447.113,\n"
                + "				\"Comp.2\": -27.4991\n"
                + "			},\n"
                + "			{\n"
                + "				\"Comp.1\": 391.9597,\n"
                + "				\"Comp.2\": -12.6911\n"
                + "			}\n"
                + "		],\n"
                + "		\"selected.rows\": [\n"
                + "			1,\n"
                + "			2,\n"
                + "			3,\n"
                + "			4,\n"
                + "			5,\n"
                + "			6,\n"
                + "			7,\n"
                + "			8,\n"
                + "			9,\n"
                + "			10,\n"
                + "			11\n"
                + "		],\n"
                + "		\"norm.var\": [\n"
                + "			0.404\n"
                + "		],\n"
                + "		\"confounding.effect\": [\n"
                + "			0.992\n"
                + "		],\n"
                + "		\"r.squared\": [\n"
                + "			null\n"
                + "		],\n"
                + "		\"adj.r.squared\": [\n"
                + "			null\n"
                + "		],\n"
                + "		\"verbal.notes\": [\n"
                + "			\"Ge value is:0.404. Ge for optimal design is 1.\",\n"
                + "			\"Diagonality value is:0.992. Diagonality for minimal confounding is 1.\"\n"
                + "		],\n"
                + "		\"predictedFeatures\": [\n"
                + "			\"suggestedTrials\"\n"
                + "		]\n"
                + "	}", Object.class);
        System.out.println(s.write(o));
    }
}
