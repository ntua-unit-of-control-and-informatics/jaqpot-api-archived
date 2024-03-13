/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import org.jaqpot.core.model.factory.TaskFactory;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

/**
 *
 * @author pantelispanka
 */

@Stateless
public class JsonPredictionService {
    
    @Resource(lookup = "java:jboss/exported/jms/topic/jsonprediction")
    private Topic predictionQueue;

    @Inject
    private JMSContext jmsContext;

    @EJB
    TaskHandler taskHandler;

    public Task initiatePrediction(Map<String, Object> options) throws JaqpotDocumentSizeExceededException {

        Task task = TaskFactory.queuedTask("Prediction by model " + options.get("modelId"),
                "A prediction procedure will return a new Dataset if completed successfully.",
                (String) options.get("creator"));
        task.setType(Task.Type.PREDICTION);
        options.put("taskId", task.getId());
        task.setVisible(Boolean.TRUE);

        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).setTimeToLive(3000).send(predictionQueue, options);
        return task;
    }
}
