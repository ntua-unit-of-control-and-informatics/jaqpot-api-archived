/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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
package org.jaqpot.core.service.mdb;

import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.ws.rs.NotFoundException;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.factory.ErrorReportFactory;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 *
 */
public abstract class AbstractJaqpotProcedure implements MessageListener {

    private static final Logger LOG = Logger.getLogger(AbstractJaqpotProcedure.class.getName());

    private final TaskHandler taskHandler;
    private Task task;

    public AbstractJaqpotProcedure(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    protected void init(String taskId) {
        taskHandler.cache(taskId);
        task = taskHandler.find(taskId);
        if (task == null) {
            LOG.log(Level.SEVERE, "Task with id:{0} could not be found in the database.", taskId);
            throw new NotFoundException("Task with id:" + taskId + " could not be found in the database.");
        }
    }

    protected void checkCancelled() {
        if (task.getStatus().equals(Task.Status.CANCELLED)) {
            throw new CancellationException("Task with id:" + task.getId() + " was cancelled");
        }
    }

    protected void start(Task.Type type) {
        task.setHttpStatus(202);
        task.setStatus(Task.Status.RUNNING);
        task.setType(type);
        progress(5f, type.name() + " Task is now running.");
    }

    protected void addProgress(Float percentage, String... messages) {
        task.getMeta().getComments().addAll(Arrays.asList(messages));
        task.setPercentageCompleted(task.getPercentageCompleted() + percentage);
        taskHandler.edit(task);
    }

    protected void progress(Float percentage, String... messages) {
        task.getMeta().getComments().addAll(Arrays.asList(messages));
        task.setPercentageCompleted(percentage);
        taskHandler.edit(task);
    }

    protected void progress(Float percentage) {
        task.setPercentageCompleted(percentage);
        taskHandler.edit(task);
    }

    protected void progress(String... messages) {
        task.getMeta().getComments().addAll(Arrays.asList(messages));
        taskHandler.edit(task);
    }

    protected void cancel() {
        task.setStatus(Task.Status.CANCELLED);
        task.getMeta().getComments().add("Task was cancelled by the user.");
        taskHandler.edit(task);
        taskHandler.clear(task.getId());
    }

    protected void complete(String result) {
        task.setResult(result);
        task.setHttpStatus(201);
        task.setPercentageCompleted(100.f);
        task.setDuration(System.currentTimeMillis() - task.getMeta().getDate().getTime()); // in ms
        task.setStatus(Task.Status.COMPLETED);
        task.getMeta().getComments().add("Task Completed Successfully.");
        taskHandler.edit(task);
        taskHandler.clear(task.getId());
    }

    protected void errNotFound(Throwable t) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(404);
        task.setErrorReport(ErrorReportFactory.notFoundError(t, null));
        taskHandler.edit(task);
        taskHandler.clear(task.getId());
    }

    protected void errNotFound(Throwable t, String details) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(404);
        task.setErrorReport(ErrorReportFactory.notFoundError(t, details));
        taskHandler.edit(task);
        taskHandler.clear(task.getId());
    }

    protected void errNotFound(String message) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(404);
        task.setErrorReport(ErrorReportFactory.notFoundError(message));
        taskHandler.edit(task);
        taskHandler.clear(task.getId());
    }

    protected void errInternalServerError(String message) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(500);
        task.setErrorReport(ErrorReportFactory.internalServerError(message, null));
        taskHandler.edit(task);
        taskHandler.clear(task.getId());
    }

    protected void errInternalServerError(Throwable t, String details) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(500);
        task.setErrorReport(ErrorReportFactory.internalServerError(t, details));
        taskHandler.edit(task);
        taskHandler.clear(task.getId());
    }

    protected void errBadRequest(String message) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(400);
        task.setErrorReport(ErrorReportFactory.badRequest(message, null));
        taskHandler.clear(task.getId());
    }

    protected void errBadRequest(Throwable t, String details) {
        task.setStatus(Task.Status.ERROR);
        task.setHttpStatus(400);
        task.setErrorReport(ErrorReportFactory.badRequest(t, details));
        taskHandler.clear(task.getId());
    }

}
