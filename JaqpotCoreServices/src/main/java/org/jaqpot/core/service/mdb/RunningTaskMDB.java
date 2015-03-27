/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.mdb;

import javax.inject.Inject;
import javax.jms.MessageListener;

/**
 *
 * @author chung
 */
public abstract class RunningTaskMDB implements MessageListener {

    @Inject
    ThreadReference threadMap;

    public void init(String taskId) {
        // Task started - updated the task reference map
        if (taskId != null) {
            threadMap.getThreadReferenceMap().put(taskId, Thread.currentThread());
        }
    }

    public void terminate(String taskId) {
        // remove the task from the thread reference map
        if (taskId != null) {
            threadMap.getThreadReferenceMap().remove(taskId);
        }
    }

    public RunningTaskMDB() {
    }
   

}
