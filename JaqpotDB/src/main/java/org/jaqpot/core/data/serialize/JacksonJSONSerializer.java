/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data.serialize;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import javax.enterprise.inject.Default;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author hampos
 */
@Default
public class JacksonJSONSerializer implements EntityJSONSerializer {
    
    ObjectMapper mapper;

    public JacksonJSONSerializer() {
        this.mapper = new ObjectMapper();
    }        

    @Override
    public void write(OutputStream out) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(Writer writer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String write() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
