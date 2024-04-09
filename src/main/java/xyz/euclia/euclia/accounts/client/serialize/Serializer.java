/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.euclia.euclia.accounts.client.serialize;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 *
 * @author pantelispanka
 */
public interface Serializer {

    public void write(Object entity, OutputStream out);

    public void write(Object entity, Writer writer);

    public String write(Object entity);

    public <T> T parse(String content, Class<T> valueType);

    public <T> T parse(InputStream src, Class<T> valueType);

}
