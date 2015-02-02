/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

/**
 *
 * @author chung
 */
public class Parameter extends JaqpotEntity {

    /**
     * The scope of a parameter can either be {@link #MANDATORY Mandatory} or
     * {@link #OPTIONAL Optional}.
     */
    public enum Scope {

        /**
         * If a parameter is tagged as 'Optional' then the client does not need
         * to provide its value explicitly but instead a default value will be
         * used.
         */
        OPTIONAL,
        /**
         * A parameter is mandatory when the user has to provide it's value and
         * no default values can be assigned to it.
         */
        MANDATORY;
    };

    private String name;
    private Object value;
    private Scope scope;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

}
