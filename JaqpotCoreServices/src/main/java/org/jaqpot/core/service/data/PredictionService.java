/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.data;

import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.factory.TaskFactory;

/**
 *
 * @author hampos
 */
@Stateless
public class PredictionService {

    @Resource(lookup = "java:jboss/exported/jms/topic/prediction")
    private Topic predictionQueue;

    @Inject
    private JMSContext jmsContext;

    @EJB
    TaskHandler taskHandler;

    public Task initiatePrediction(Map<String, Object> options) {

        Task task = TaskFactory.queuedTask("Prediction by model " + options.get("modelId"),
                "A prediction procedure will return a new Dataset if completed successfully.",
                (String) options.get("createdBy"));
        task.setType(Task.Type.PREDICTION);
        options.put("taskId", task.getId());
        if ((Boolean) options.get("visible")) {
            task.setVisible(Boolean.TRUE);
        } else {
            task.setVisible(Boolean.FALSE);
        }
        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(predictionQueue, options);
        return task;
    }
}
