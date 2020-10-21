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
package org.jaqpot.core.service.filter;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.ejb.EJB;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.Task;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Provider
@Task
@Priority(Priorities.USER)
public class TaskRequestFilter implements ContainerRequestFilter {

    @EJB
    UserHandler userHandler;

    @EJB
    TaskHandler taskHandler;

    private static final Logger LOG = Logger.getLogger(TaskRequestFilter.class.getName());

    public TaskRequestFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String userName = requestContext.getSecurityContext().getUserPrincipal().getName();

//        Long queuedTasks = taskHandler.countByUserAndStatus(userName, org.jaqpot.core.model.Task.Status.QUEUED);
//        Long runningTasks = taskHandler.countByUserAndStatus(userName, org.jaqpot.core.model.Task.Status.RUNNING);
//        Long parallelTasks = queuedTasks + runningTasks;

//        User user = userHandler.find(userName);

//        Integer maxParallelTasks = user.getCapabilities().get("tasksParallel");

//        if (parallelTasks > maxParallelTasks) {
//            ErrorReport error = ErrorReportFactory.quotaExceeded("Dear " + userName + ", your quota has been exceeded; you already have " + parallelTasks + " tasks running in parallel. "
//                    + "No more than " + maxParallelTasks + " are allowed with your subscription.");
//            requestContext.abortWith(Response
//                    .status(Response.Status.PAYMENT_REQUIRED)
//                    .type(MediaType.APPLICATION_JSON)
//                    .entity(error)
//                    .build()
//            );
//        }

    }

}
