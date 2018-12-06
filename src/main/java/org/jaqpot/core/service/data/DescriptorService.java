package org.jaqpot.core.service.data;

import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.factory.TaskFactory;
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import java.util.Map;
import javax.jms.DeliveryMode;

@Stateless
public class DescriptorService {

    @EJB
    TaskHandler taskHandler;

    @Resource(lookup = "java:jboss/exported/jms/topic/descriptor")
    private Topic descriptorQueue;

    @Inject
    private JMSContext jmsContext;

    public Task initiateDescriptor(Map<String, Object> options, String userName) throws JaqpotDocumentSizeExceededException {
        Task task = TaskFactory.queuedTask("Preparation on file: " + options.get("filename"),
                "A preparation procedure will return a Dataset if completed successfully."
                        + "It may also initiate other procedures if desired.",
                userName);
        task.setType(Task.Type.PREPARATION);
        options.put("taskId", task.getId());
        task.setVisible(Boolean.TRUE);
        taskHandler.create(task);
        jmsContext.createProducer()
                .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                .setTimeToLive(3000000)
                .setDeliveryDelay(10)
                .send(descriptorQueue, options);
        return task;
    }
}
