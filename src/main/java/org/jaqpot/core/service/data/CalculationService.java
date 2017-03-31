package org.jaqpot.core.service.data;

import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.FeatureHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.dto.dataset.Dataset;
import org.jaqpot.core.model.dto.dataset.FeatureInfo;
import org.jaqpot.core.model.factory.TaskFactory;
import org.jaqpot.core.service.client.ambit.Ambit;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Angelos Valsamis on 29/3/2017.
 */

@Stateless
public class CalculationService {

    @Inject
    @Jackson
    JSONSerializer serializer;


    @EJB
    TaskHandler taskHandler;

    @EJB
    FeatureHandler featureHandler;

    //@Resource(lookup = "java:jboss/exported/jms/topic/descriptorspreparation")
    private Topic preparationQueue;


    @Inject
    private JMSContext jmsContext;

    @Inject
    Ambit ambitClient;

    private Set<FeatureInfo> featureMap;

    private Set<Dataset.DescriptorCategory> usedDescriptors;

    public Task initiatePreparation(Map<String, Object> options, String userName) {
        Task task = TaskFactory.queuedTask("Preparation on file: " + options.get("bundle_uri"),
                "A preparation procedure will return a Dataset if completed successfully."
                        + "It may also initiate other procedures if desired.",
                userName);
        task.setType(Task.Type.PREPARATION);
        options.put("taskId", task.getId());
        task.setVisible(Boolean.TRUE);
        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(preparationQueue, options);
        return task;
    }

    public Dataset prepareDataset(Byte[] bytes){

        return null;
    }
}
