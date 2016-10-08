/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.data.serialize;

/**
 *
 * @author hampos
 */
public class JaqpotSerializationException extends RuntimeException {

    /**
     * Creates a new instance of <code>JaqpotSerializationException</code>
     * without detail message.
     */
    public JaqpotSerializationException() {
    }

    /**
     * Constructs an instance of <code>JaqpotSerializationException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public JaqpotSerializationException(String msg) {
        super(msg);
    }

    public JaqpotSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JaqpotSerializationException(Throwable cause) {
        super(cause);
    }

}
