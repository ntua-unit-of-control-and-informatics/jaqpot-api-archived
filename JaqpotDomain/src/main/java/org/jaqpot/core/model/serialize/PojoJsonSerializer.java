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
package org.jaqpot.core.model.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Deprecated
public class PojoJsonSerializer {

    private final Object pojo;
    private ObjectMapper mapper;

    public PojoJsonSerializer(Object pojo) {
        this.pojo = pojo;
    }

    public PojoJsonSerializer(Object pojo, ObjectMapper mapper) {
        this.pojo = pojo;
        this.mapper = mapper;
    }        

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }        

    public String toJsonString() throws IOException, JsonGenerationException, JsonMappingException {
        if (mapper == null) {
            setMapper(new ObjectMapper());
        }
        return mapper.writeValueAsString(pojo);
    }

    public void writeValue(OutputStream out) throws IOException, JsonGenerationException, JsonMappingException {
        mapper.writeValue(out, pojo);
    }

    public void writeValue(Writer w) throws IOException, JsonGenerationException, JsonMappingException {
        mapper.writeValue(w, pojo);
    }

}
