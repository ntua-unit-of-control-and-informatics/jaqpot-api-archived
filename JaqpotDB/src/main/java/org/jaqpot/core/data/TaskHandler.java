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
package org.jaqpot.core.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.MetaInfoBuilder;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Stateless
public class TaskHandler extends AbstractHandler<Task> {

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public TaskHandler() {
        super(Task.class);
    }

    @PostConstruct
    public void init() {

        MetaInfoBuilder metaBuilder = MetaInfoBuilder.builder();
        MetaInfo meta = metaBuilder.
                addComments("task started", "this task does training", "dataset downloaded").
                addDescriptions("this is a very nice task", "oh, and it's very useful too").
                addSources("http://jaqpot.org/algorithm/wonk").build();

        Task taskPojo;
        taskPojo = new Task("115a0da8-92cc-4ec4-845f-df643ad607ee");
        taskPojo.setCreatedBy("random-user@jaqpot.org");
        taskPojo.setPercentageCompleted(0.95f);
        taskPojo.setDuration(1534l);
        taskPojo.setMeta(meta);
        taskPojo.setHttpStatus(202);
        taskPojo.setStatus(Task.Status.RUNNING);
        em.merge(taskPojo);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }

    public List<Task> findByUser(String userName, Integer start, Integer max) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("createdBy", userName);

        return em.find(Task.class, properties, start, max);
    }

    public Long countByUser(String userName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("createdBy", userName);

        return em.count(Task.class, properties);
    }

    public List<Task> findByStatus(Task.Status status, Integer start, Integer max) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hasStatus", status);

        return em.find(Task.class, properties, start, max);
    }

    public Long countByStatus(Task.Status status) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hasStatus", status);

        return em.count(Task.class, properties);
    }

}
