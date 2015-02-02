/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model;

import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author chung
 */
@XmlRootElement
public abstract class JaqpotEntity {

    private String id;

    private MetaInfo meta;
    private Set<String> ontologicalClasses;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MetaInfo getMeta() {
        return meta;
    }

    public void setMeta(MetaInfo meta) {
        this.meta = meta;
    }

    public Set<String> getOntologicalClasses() {
        return ontologicalClasses;
    }

    public void setOntologicalClasses(Set<String> ontologicalClasses) {
        this.ontologicalClasses = ontologicalClasses;
    }

}
