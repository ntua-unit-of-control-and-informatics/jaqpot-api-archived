/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data.serialize;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 *
 * @author hampos
 */
public interface EntityJSONSerializer {
    
    public void write(OutputStream out);
    
    public void write(Writer writer);
    
    public String write();
    
    public <T> T parse(String content, Class<T> valueType);
    
    public <T> T parse(InputStream src, Class<T> valueType);
    
}
