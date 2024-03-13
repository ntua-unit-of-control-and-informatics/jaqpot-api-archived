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
package org.jaqpot.core.service.httphandlers;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.core.JsonFactory;

import com.fasterxml.jackson.core.JsonParser;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;

import com.fasterxml.jackson.databind.node.ArrayNode;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.Iterator;

import javax.ejb.Stateless;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import javax.json.Json;

import javax.json.JsonObject;

import javax.ws.rs.InternalServerErrorException;

import javax.ws.rs.core.Context;

import javax.ws.rs.core.UriInfo;

import org.jaqpot.core.properties.PropertyManager;

/**
 *
 *
 *
 * @author pantelispanka
 *
 */
@Stateless

public class SwaggerLdHandler {

    private final ObjectMapper objectMapper;

    private String base;

    @Context

    UriInfo uriInfo;

    @Inject

    PropertyManager pm;

    public SwaggerLdHandler() {

        objectMapper = new ObjectMapper();

        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    }

    public JsonNode test(String swagger) {

        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);

        JsonFactory factory = objectMapper.getFactory();

        JsonParser parser = null;

        JsonNode swaggerObj = null;

        try {

            parser = factory.createParser(swagger);

            swaggerObj = objectMapper.readTree(parser);

        } catch (IOException e) {

            throw new InternalServerErrorException("Json could not be formed");

        }

        ObjectNode on = (ObjectNode) swaggerObj;

        on = createContext(on);

        on = setId(on);

        on = addIdOnPaths(on);

//        on = paramLd(on);
        swaggerObj = (JsonNode) on;

        return swaggerObj;

    }

    public ObjectNode setId(ObjectNode on) {

        String jaqBase = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE);

        String port = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_PORT);

        String host = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_HOST);

        String swagUri = "http://" + host + ":" + port + jaqBase + "/openapi.json";

        on.put("x-orn-@id", swagUri);

        return on;

    }

    public ObjectNode addIdOnPaths(ObjectNode on) {

        ObjectNode paths = (ObjectNode) on.get("paths");

        Iterator<String> services = paths.fieldNames();

        String jaqBase = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_BASE);

        String port = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_PORT);

        String host = pm.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_HOST);

        String pathId = "http://" + host + ":" + port + jaqBase;

        while (services.hasNext()) {

            String path_taken = services.next();

            JsonNode path = paths.get(path_taken);

//            path = addPathId(path, pathId, path_taken);
            for (JsonNode methods : path) {

                ObjectNode pa = (ObjectNode) methods;

                pa.put("x-orn-@id", pathId + path_taken);

                pa = paramLd(pa);

//                ObjectNode paaa = (ObjectNode) path;
//                paaa.put("x-orn-@id", pathId + path_taken);
                on.replace(path_taken, pa);

            }

        }

        return on;

    }

    public JsonNode addPathId(JsonNode jn, String pathId, String path_taken) {

        ObjectNode on = (ObjectNode) jn;

        on.put("x-orn-@id", pathId + path_taken);

        jn = (JsonNode) on;

        return jn;

    }

    public ObjectNode paramLd(ObjectNode on) {

        JsonNode params = on.findPath("parameters");

        for (JsonNode param : params) {

            JsonNode name = param.get("name");

            String nameValue = name.asText();

            ObjectNode paramLd = (ObjectNode) param;

            paramLd.put("x-orn-@type", "x-orn:" + nameValue.substring(0, 1).toUpperCase() + nameValue.substring(1));

        }

        return on;

    }

    public ObjectNode createContext(ObjectNode on) {

        ObjectNode contxt = objectMapper.createObjectNode();

        contxt.put("x-orn", "http://openrisknet.org/schema/");

        contxt.put("x-orn-@id", "@id");

        contxt.put("x-orn-@type", "@type");

        contxt.put("title", "x-orn:title");

        contxt.put("info", "x-orn:info");

        contxt.put("description", "x-orn:description");

        ObjectNode paths = objectMapper.createObjectNode();

        paths.put("@id", "x-orn:paths");

        paths.put("@container", "@index");

        ObjectNode resp = objectMapper.createObjectNode();

        resp.put("@id", "x-orn:OperationResponses");

        resp.put("@container", "@index");

        ObjectNode props = objectMapper.createObjectNode();

        props.put("@id", "x-orn:Properties");

        props.put("@container", "@index");

        ObjectNode cont = objectMapper.createObjectNode();

        cont.put("@id", "x-orn:Content");

        cont.put("@container", "@index");

        contxt.put("paths", paths);

        contxt.put("responses", resp);

        contxt.put("properties", props);

        contxt.put("content", cont);

        contxt.put("parameters", "x-orn:OperationParameters");

        contxt.put("schema", "x-orn:Schema");

        contxt.put("get", "x-orn:Get");

        contxt.put("post", "x-orn:Post");

        contxt.put("delete", "x-orn:Delete");

        contxt.put("put", "x-orn:Put");

        contxt.put("patch", "x-orn:Patch");
        on.put("x-orn-@context", contxt);
        return on;

    }

}
