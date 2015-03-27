/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.mdb;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author chung
 */
@ApplicationScoped
public class ThreadReference {

    private final Map<String, Thread> THREAD_MAP;

    public ThreadReference() {
        this.THREAD_MAP = new HashMap<>();
    }

    public Map<String, Thread> getThreadReferenceMap() {
        return THREAD_MAP;
    }

}
