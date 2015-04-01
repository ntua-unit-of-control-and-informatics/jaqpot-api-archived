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
package org.jaqpot.core.model.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.util.ROG;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
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
     *
     * @param id
     * @return
     * @see #builderRandomId()
     */
    public static TaskBuilder builder(String id) {
        return new TaskBuilder(id);
    }

    public static TaskBuilder builder(Task task) {
        return new TaskBuilder(task);
    }

    /**
     * Created a Task builder which will instantiate a new Task object with a
     * randomly generated ID in the form of a UUID.
     *
     * @return
     * @see #builder(java.lang.String)
     */
    public static TaskBuilder builderRandomId() {
        ROG rog = new ROG(true);
        return new TaskBuilder("TSK" + rog.nextString(12));
    }

    private TaskBuilder(String id) {
        task = new Task(id);
    }

    private TaskBuilder(Task other) {
        this.task = other;
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
        if (progressComments==null){
            return this;
        }
        initMeta();
        if (task.getMeta().getComments() == null) {
            task.getMeta().setComments(new ArrayList<>(progressComments.length));
        }
        for (String entry : progressComments){
            if (entry != null){
                task.getMeta().getComments().add(entry);
            }
        }
        return this;
    }

    public TaskBuilder addDescription(String... descriptions) {
        if (descriptions == null) {
            return this;
        }
        initMeta();
        if (task.getMeta().getDescriptions() == null) {
            task.getMeta().setDescriptions(new HashSet<>(descriptions.length));
        }
        task.getMeta().getDescriptions().addAll(Arrays.asList(descriptions));
        return this;
    }

    public TaskBuilder addSources(String... sources) {
        if (sources == null) {
            return this;
        }
        initMeta();
        if (task.getMeta().getHasSources() == null) {
            task.getMeta().setHasSources(new HashSet<>(sources.length));
        }
        task.getMeta().getHasSources().addAll(Arrays.asList(sources));
        return this;
    }

    public TaskBuilder setDate(Date date) {
        if (date==null){
            return null;
        }
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
        if (task.getMeta().getTitles() == null) {
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
