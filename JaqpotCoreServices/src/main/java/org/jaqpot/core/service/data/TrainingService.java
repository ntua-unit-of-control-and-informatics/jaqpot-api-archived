/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licenced by GPL v3 as specified hereafter. Additional components may ship
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
 * All source files of JAQPOT Quattro that are stored on github are licenced
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
import javax.ws.rs.NotFoundException;
import org.jaqpot.core.data.AlgorithmHandler;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Algorithm;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;
import org.jaqpot.core.model.factory.TaskFactory;
import org.jaqpot.core.model.util.ROG;

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

        //TODO Improve TaskFactory to create queued tasks (add more methods)
        //TODO Create MetaInfoBuilder form existing meta
        Task task = new Task(new ROG(true).nextString(12));
        task.setMeta(
                MetaInfoBuilder.builder()
                .setCurrentDate()
                .addTitles("Training on algorithm: " + algorithm.getId())
                .addSources("algorithm" + options.get("algorithmId").toString())
                .addComments("Training task created")
                .addDescriptions("Training task using algorithm " + algorithmId)
                .build());
        task.setType(Task.Type.TRAINING);
        options.put("taskId", task.getId());
        taskHandler.create(task);
        jmsContext.createProducer().setDeliveryDelay(1000).send(trainingQueue, options);
        return task;
    }

}
