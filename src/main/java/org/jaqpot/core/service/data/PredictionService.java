/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
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
import org.jaqpot.core.service.exceptions.JaqpotDocumentSizeExceededException;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@Stateless
public class PredictionService {

    @Resource(lookup = "java:jboss/exported/jms/topic/prediction")
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
        jmsContext.createProducer().setDeliveryDelay(1000).send(predictionQueue, options);
        return task;
    }
}
