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
package org.jaqpot.core.service.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.jaqpot.core.model.JaqpotEntity;
import org.jaqpot.core.model.DataEntry;
import org.jaqpot.core.model.dto.dataset.Dataset;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Provider
@Produces("text/csv")
public class CSVWriter implements MessageBodyWriter<JaqpotEntity> {

    @Context
    UriInfo uriInfo;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(Dataset.class);
    }

    @Override
    public long getSize(JaqpotEntity t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(JaqpotEntity entity, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException, WebApplicationException {

        String uri = uriInfo.getBaseUri() + entity.getClass().getSimpleName().toLowerCase() + "/" + entity.getId();
        Dataset dataset = (Dataset) entity;

        Set<String> attributes = dataset.getDataEntry().get(0).getValues().keySet();
        String headers = "\"EntryId\"," + attributes.stream()
                .map(a -> "\"" + dataset.getFeatures().stream()
                        .filter(f -> f.getURI().equals(a))
                        .findFirst()
                        .get()
                        .getName() + "\"")
                .collect(Collectors.joining(","));
//        String headers = dataset.getDataEntry().get(0).getValues().keySet().stream().collect(Collectors.joining(","));
        entityStream.write(headers.getBytes());
        for (DataEntry de : dataset.getDataEntry()) {
            String row = "\n\"" + de.getEntryId().getName() + "\"," + de.getValues().values().stream()
                    .map(v -> v != null ? "\"" + v.toString() + "\"" : "\"null\"").collect(Collectors.joining(","));
            entityStream.write(row.getBytes());
        }
        entityStream.flush();
    }

}
