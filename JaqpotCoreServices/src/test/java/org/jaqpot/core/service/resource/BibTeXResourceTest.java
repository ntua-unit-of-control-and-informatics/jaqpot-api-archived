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
package org.jaqpot.core.service.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import org.junit.Test;

/**
 *
 * @author chung
 */
public class BibTeXResourceTest {

    public BibTeXResourceTest() {
    }

    @Test
    public void testPATCH() throws Exception {
        
        String patch = "[{ \"op\": \"add\", \"path\": \"/key\", \"value\": \"foo\" }]";
        // 494d5837-1970-4cbb-8fde-a3c56218f240
        // [{ "op": "add", "path": "/key", "value": "foo" }]
        String origin = "{\n"
                + "  \"bibType\":\"Article\",\n"
                + "  \"title\":\"title goes here\",\n"
                + "  \"author\":\"A.N.Onymous\",\n"
                + "  \"journal\":\"Int. J. Biochem.\",\n"
                + "  \"year\":2010,\n"
                + "  \"meta\":{\"comments\":[\"default bibtex\"]}\n"
                + "}";
        
        final ObjectMapper mapper = new ObjectMapper();        
        JsonPatch p = mapper.readValue(patch, JsonPatch.class);
        JsonNode result = p.apply(mapper.readTree(origin));
        System.out.println(result);
        
    }

}
