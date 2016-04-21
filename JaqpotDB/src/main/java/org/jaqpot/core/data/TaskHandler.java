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
package org.jaqpot.core.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jaqpot.core.annotations.MongoDB;
import org.jaqpot.core.db.entitymanager.JaqpotEntityManager;
import org.jaqpot.core.model.Task;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Stateless
public class TaskHandler extends AbstractHandler<Task> {

    private static final Map<Object, Task> taskCache = new HashMap<>();
    private static final Map<Object, CountDownLatch> taskLatches = new ConcurrentHashMap<>();

    @Inject
    @MongoDB
    JaqpotEntityManager em;

    public TaskHandler() {
        super(Task.class);
    }

    @Override
    protected JaqpotEntityManager getEntityManager() {
        return em;
    }

    @Override
    public Task find(Object id) {
        Task result = taskCache.get(id);
        return result != null ? result : super.find(id);
    }

    @Override
    public void edit(Task entity) {
        CountDownLatch latch = taskLatches.get(entity.getId());
        if (latch != null) {
            latch.countDown();
            taskLatches.put(entity.getId(), new CountDownLatch(1));
        }
        super.edit(entity);
    }

    public void cache(Object id) {
        Task result = super.find(id);
        if (result != null) {
            taskCache.put(id, result);
            taskLatches.put(id, new CountDownLatch(1));
        }
    }

    public void clear(Object id) {
        taskCache.remove(id);
        taskLatches.remove(id);
    }

    public Task longPoll(Object id) throws InterruptedException {
        CountDownLatch latch = taskLatches.get(id);
        Task result = this.find(id);
        if (result.getStatus().equals(Task.Status.QUEUED) || result.getStatus().equals(Task.Status.RUNNING)) {
            latch.await();
            return result;
        } else {
            return result;
        }
    }

    public List<Task> findByUser(String userName, Integer start, Integer max) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(userName));
        properties.put("visible", true);

        return em.find(Task.class, properties, start, max);
    }

    public Long countByUser(String userName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(userName));
        properties.put("visible", true);

        return em.count(Task.class, properties);
    }

    public List<Task> findByStatus(Task.Status status, Integer start, Integer max) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("status", status.name());
        properties.put("visible", true);

        return em.find(Task.class, properties, start, max);
    }

    public Long countByStatus(Task.Status status) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("status", status.name());
        properties.put("visible", true);

        return em.count(Task.class, properties);
    }

    public List<Task> findByUserAndStatus(String userName, Task.Status status, Integer start, Integer max) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(userName));
        properties.put("status", status.name());
        properties.put("visible", true);

        return em.find(Task.class, properties, start, max);
    }

    public Long countByUserAndStatus(String userName, Task.Status status) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("meta.creators", Arrays.asList(userName));
        properties.put("status", status.name());
        properties.put("visible", true);

        return em.count(Task.class, properties);
    }

}
