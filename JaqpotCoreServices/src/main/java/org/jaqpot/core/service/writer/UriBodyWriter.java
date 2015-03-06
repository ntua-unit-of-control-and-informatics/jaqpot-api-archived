/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.jaqpot.core.model.JaqpotEntity;

/**
 *
 * @author hampos
 */
@Provider
@Produces("text/uri-list")
public class UriBodyWriter implements MessageBodyWriter<JaqpotEntity> {

    @Context
    UriInfo uriInfo;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
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
        entityStream.write(uri.getBytes());
        entityStream.flush();
    }

}
