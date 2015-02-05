/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data.serialize;

import org.jaqpot.core.annotations.Jackson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Default;
import javax.net.ssl.SSLContext;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author hampos
 */
@Default
@Jackson
public class JacksonJSONSerializer implements EntityJSONSerializer {

    private static final Logger LOG = Logger.getLogger(JacksonJSONSerializer.class.getName());

    ObjectMapper mapper;

    public JacksonJSONSerializer() {
        this.mapper = new ObjectMapper();        
    }

    @Override
    public void write(Object entity, OutputStream out) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(Object entity, Writer writer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String write(Object entity) {
        try {
            return mapper.writeValueAsString(entity);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            return "";
        }
    }

    @Override
    public <T> T parse(String content, Class<T> valueType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T parse(InputStream src, Class<T> valueType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
