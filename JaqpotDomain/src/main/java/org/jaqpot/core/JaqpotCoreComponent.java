/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author chung
 */
public abstract class JaqpotCoreComponent {
    
    private MetaInfo meta;
    private final Set<String> ontologicalClasses = new HashSet<>();
    
    
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
    
}
