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
package org.jaqpot.core.model.factory;

import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.TaskBuilder;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalamπos Chomenidis
 */
public class TaskFactory {

    /**
     * Creates the default queued task. The default task has the following
     * characteristics:
     * <ul>
     * <li>HTTP Status: 201</li>
     * <li>Task status: {@link Task.Status.QUEUED QUEUED}</li>
     * <li>Date: current date</li>
     * <li>Comment: task created</li>
     * <li>title: user defined</li>
     * <li>description: user defined</li>
     * </ul>
     *
     * @param title 
     *      Title of the task, e.g., <code>SVM Task 34</code>. 
     *      Can potentially be set to <code>null</code>, although it is not
     *      advisable.
     * @param description 
     *      Short description of the task, e.g., <code>this task
     *      was created to train a SVM model</code>. The description can be set
     *      to <code>null</code>, but it is not advisable to help discoverability
     * @param creator
     *      of tasks.
     * @return
     *      The default queued task.
     */
    public static Task queuedTask(String title, String description, String creator) {
        Task queuedTask =  TaskBuilder.builderRandomUuid().
                addProgressComments("Task created").
                addDescription(description).
                addTitles(title).
                setCreatedBy(creator).
                setCurrentDate().
                setHttpStatus(202).
                setStatus(Task.Status.QUEUED).
                build();
        return queuedTask;
    }

}
