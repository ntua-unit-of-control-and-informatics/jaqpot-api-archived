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
package org.jaqpot.core.model.builder;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;

/**
 *
 * @author chung
 */
public class TaskBuilder implements EntityBuilder<Task> {

    private final Task task;

    @Override
    public Task build() {
        return this.task;
    }

    /**
     * Created a Task builder which will instantiate a new Task object with
     * given ID. You cannot generate a task without specifying an ID.
     * @param id
     * @return 
     * @see #builderRandomUuid() 
     */
    public static TaskBuilder builder(String id) {
        return new TaskBuilder(id);
    }
    
    /**
     * Created a Task builder which will instantiate a new Task object with
     * a randomly generated ID in the form of a UUID.
     * @return 
     * @see #builder(java.lang.String) 
     */
    public static TaskBuilder builderRandomUuid() {
        return new TaskBuilder(UUID.randomUUID().toString());
    }

    private TaskBuilder(String id) {
        task = new Task(id);
    }

    public TaskBuilder setCreatedBy(String createdBy) {
        task.setCreatedBy(createdBy);
        return this;
    }

    public TaskBuilder setDuration(long duration) {
        task.setDuration(duration);
        return this;
    }

    public TaskBuilder setErrorReport(ErrorReport errorReport) {
        task.setErrorReport(errorReport);
        return this;
    }

    public TaskBuilder setHttpStatus(int httpStatus) {
        task.setHttpStatus(httpStatus);
        return this;
    }

    public TaskBuilder setPercentageCompleted(float percentageCompleted) {
        task.setPercentageCompleted(percentageCompleted);
        return this;
    }

    public TaskBuilder setResultUri(String resultUri) {
        task.setResultUri(resultUri);
        return this;
    }

    public TaskBuilder setStatus(Task.Status status) {
        task.setStatus(status);
        return this;
    }        

    /**
     * Add comments on the progress of the task. While a task is running,
     * comments are added so that the client knows exactly what is happening
     * behind the scenes. Comments may be like "Loading dataset", "Processing
     * user input" etc...
     *
     * @param progressComments Array of comments to be added
     * @return The current modifiable instance of TaskBuilder.
     */
    public TaskBuilder addProgressComments(String... progressComments) {
        initMeta();
        if (task.getMeta().getComments() == null) {
            task.getMeta().setComments(new HashSet<>(progressComments.length));
        }
        task.getMeta().getComments().addAll(Arrays.asList(progressComments));
        return this;
    }

    public TaskBuilder setDate(Date date) {
        initMeta();
        task.getMeta().setDate(date);
        return this;
    }

    public TaskBuilder setCurrentDate() {
        initMeta();
        task.getMeta().setDate(new Date());
        return this;
    }
    
    public TaskBuilder addTitles(String... titles) {
        initMeta();
        if (task.getMeta().getTitles()== null) {
            task.getMeta().setTitles(new HashSet<>(titles.length));
        }
        task.getMeta().getTitles().addAll(Arrays.asList(titles));
        return this;
    }

    private void initMeta() {
        if (task.getMeta() == null) {
            task.setMeta(new MetaInfo());
        }
    }

}
