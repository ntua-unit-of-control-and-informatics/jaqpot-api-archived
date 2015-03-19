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
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.JaqpotEntity;
import org.jaqpot.core.model.Task;

/**
 *
 * @author hampos
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JSONListBodyWriter implements MessageBodyWriter<List<JaqpotEntity>> {

    @Context
    UriInfo uriInfo;

    @Inject
    @Jackson
    JSONSerializer JSONSerializer;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(List<JaqpotEntity> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(List<JaqpotEntity> entityList, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        for (JaqpotEntity entity : entityList) {
            String uri = uriInfo.getBaseUri() + entity.getClass().getSimpleName().toLowerCase() + "/" + entity.getId();
            entity.setURI(uri);
            if (entity instanceof Task) {
                Task task = (Task) entity;
                if (task.getStatus().equals(Task.Status.COMPLETED) && task.getType() != null) {
                    switch (task.getType()) {
                        case PREDICTION:
                            task.setResultUri(uriInfo.getBaseUri() + "dataset" + "/" + task.getResult());
                            break;
                        case TRAINING:
                            task.setResultUri(uriInfo.getBaseUri() + "model" + "/" + task.getResult());
                            break;
                        case PREPARATION:
                            task.setResultUri(uriInfo.getBaseUri() + "dataset" + "/" + task.getResult());
                            break;
                    }
                }
            }
        }
        JSONSerializer.write(entityList, entityStream);
        entityStream.flush();
    }
}
