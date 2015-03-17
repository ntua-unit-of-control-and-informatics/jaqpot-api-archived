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
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
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
@Stateless
public class TrainingService {

    @EJB
    AlgorithmHandler algorithmHandler;

    @EJB
    TaskHandler taskHandler;

    @Resource(lookup = "java:jboss/exported/jms/topic/training")
    private Topic trainingQueue;

    @Inject
    private JMSContext jmsContext;

    public Task initiateTraining(Map<String, Object> options, String userName) {

        String algorithmId = (String) options.get("algorithmId");
        Algorithm algorithm = algorithmHandler.find(algorithmId);
        if (algorithm == null) {
            throw new NotFoundException("Could not find algorithm with id:" + algorithmId);
        }

        Task task = TaskFactory.queuedTask("Training on algorithm: " + algorithm.getId(),
                "A training procedure will return a Model if completed successfully.",
                userName);
        task.setType(Task.Type.TRAINING);
        options.put("taskId", task.getId());
        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(trainingQueue, options);
        return task;
    }

}
