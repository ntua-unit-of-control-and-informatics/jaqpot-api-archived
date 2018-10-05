package org.jaqpot.core.service.data;

import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.factory.TaskFactory;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import java.util.Map;
import java.util.Set;

/**
 * Created by Angelos Valsamis on 29/3/2017.
 */

@Stateless
public class CalculationService {

    @EJB
    TaskHandler taskHandler;

    @Resource(lookup = "java:jboss/exported/jms/topic/descriptorspreparation")
    private Topic calculationQueue;

    @Inject
    private JMSContext jmsContext;

    private Set<FeatureInfo> featureMap;

    private Set<Dataset.DescriptorCategory> usedDescriptors;

    public Task initiatePreparation(Map<String, Object> options, String userName) throws JaqpotDocumentSizeExceededException {
        Task task = TaskFactory.queuedTask("Preparation on file: " + options.get("filename"),
                "A preparation procedure will return a Dataset if completed successfully."
                        + "It may also initiate other procedures if desired.",
                userName);
        task.setType(Task.Type.PREPARATION);
        options.put("taskId", task.getId());
        task.setVisible(Boolean.TRUE);
        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(calculationQueue, options);
        return task;
    }
}
