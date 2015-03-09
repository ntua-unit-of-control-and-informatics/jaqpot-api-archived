/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.data;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.ws.rs.NotFoundException;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.factory.TaskFactory;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Singleton
public class TrainingService {

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    TaskHandler taskHandler;

    @Resource(lookup = "java:jboss/exported/jms/topic/training")
    private Topic trainingQueue;

    @Inject
    private JMSContext jmsContext;

    public Task initiateTraining(Map<String, Object> options) {

        String algorithmId = (String) options.get("algorithmId");

        Algorithm algorithm = algorithmHandler.find(algorithmId);
        if (algorithm == null) {
            throw new NotFoundException("Could not find algorithm with id:" + algorithmId);
        }

        Task task = TaskFactory.queuedTask("This is a new task",
                "this is a description for my task",
                "chung");

        options.put("taskId", task.getId());
        taskHandler.create(task);
        options.entrySet().stream().forEach(e -> {
            System.out.println(e.getKey() + ":" + e.getValue());
        });
        jmsContext.createProducer().setDeliveryDelay(1000).send(trainingQueue, options);

        return task;
    }

}
