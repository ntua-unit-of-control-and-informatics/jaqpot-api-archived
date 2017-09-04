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
package org.jaqpot.core.service.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.service.annotations.Authorize;
import org.jaqpot.core.service.client.jpdi.JPDIClient;
import org.jaqpot.core.service.exceptions.JaqpotForbiddenException;
import org.jaqpot.core.service.mdb.ThreadReference;

/**
 *
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenidis
 *
 */
@Path("task")
@Api(value = "/task", description = "Tasks API")
@Authorize
public class TaskResource {

    @Context
    UriInfo uriInfo;

    @EJB
    TaskHandler taskHandler;

    @Inject
    JPDIClient jpdiClient;

    @Context
    SecurityContext securityContext;

    @Resource
    private ManagedExecutorService executor;

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @ApiOperation(value = "Finds all Tasks",
            notes = "Finds all Tasks from Jaqpot Dataset. One may specify various "
            + "search criteria such as the task creator of the task status.",
            response = Task.class,
            responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success; the list of tasks is found in the response"),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response listTasks(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "Status of the task", allowableValues = "RUNNING,QUEUED,COMPLETED,ERROR,CANCELLED,REJECTED") @QueryParam("status") String status,
            @ApiParam(value = "start", defaultValue = "0") @QueryParam("start") Integer start,
            @ApiParam(value = "max - the server imposes an upper limit of 500 on this "
                    + "parameter.", defaultValue = "10") @QueryParam("max") Integer max
    ) {
        start = start != null ? start : 0;
        if (max == null || max > 500) {
            max = 500;
        }
        List<Task> foundTasks;
        Long totalTasks;
        String creator = securityContext.getUserPrincipal().getName();
        if (status == null) {
            foundTasks = taskHandler.findByUser(creator, start, max);
            totalTasks = taskHandler.countAllOfCreator(creator);
        } else {
            foundTasks = taskHandler.findByUserAndStatus(creator, Task.Status.valueOf(status), start, max);
            totalTasks = taskHandler.countByUserAndStatus(creator, Task.Status.valueOf(status));
        }
        foundTasks.stream().forEach(task -> {
            if (task.getResult() != null) {
                task.setResultUri(uriInfo.getBaseUri() + task.getResult());
            }
        });
        return Response.ok(foundTasks)
                .header("total", totalTasks)
                .build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Path("/{id}")
    @ApiOperation(value = "Finds Task by Id",
            notes = "Finds specified Task",
            response = Task.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Task is found"),
        @ApiResponse(code = 201, message = "Task is created (see content - redirects to other task)"),
        @ApiResponse(code = 202, message = "Task is accepted (still running)"),
        @ApiResponse(code = 404, message = "This task was not found."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getTask(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @ApiParam(value = "ID of the task to be retrieved") @PathParam("id") String id) {
        Task task = taskHandler.find(id);
        if (task == null) {
            throw new NotFoundException("Task " + uriInfo.getPath() + "not found");
        }
        if (task.getResult() != null) {
            task.setResultUri(uriInfo.getBaseUri() + task.getResult());
        }

        Response.ResponseBuilder builder = Response
                .ok(task)
                .status(task.getHttpStatus());
        if (Task.Status.COMPLETED == task.getStatus()) {
            builder.header("Location", task.getResultUri());
        }
        return builder.build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(value = "Deletes a Task of given ID",
            notes = "Deletes a Task given its ID in the URI. When the DELETE method is applied, the task "
            + "is interrupted and tagged as CANCELLED. Note that this method does not return a response "
            + "on success. If the task does not exist, an error report will be returned to the client "
            + "accompanied by an HTTP status code 404. Note also that authentication and authorization "
            + "restrictions apply, so clients need to be authenticated with a valid token and have "
            + "appropriate rights to be able to successfully apply this method."
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Task deleted successfully"),
        @ApiResponse(code = 200, message = "Task not found"),
        @ApiResponse(code = 401, message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 403, message = "This is a forbidden operation (do not attempt to repeat it)."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response deleteTask(
            @ApiParam(value = "ID of the task which is to be cancelled.", required = true) @PathParam("id") String id,
            @HeaderParam("subjectid") String subjectId) throws JaqpotForbiddenException {

        Task task = taskHandler.find(id);
        if (task == null) {
            throw new NotFoundException("Task with ID:" + id + " was not found on the server.");
        }

        MetaInfo metaInfo = task.getMeta();
        if (metaInfo.getLocked())
            throw new JaqpotForbiddenException("You cannot delete a Task that is locked.");

        String userName = securityContext.getUserPrincipal().getName();
        if (!task.getMeta().getCreators().contains(userName)) {
            throw new ForbiddenException("You cannot cancel a Task not created by you.");
        }

        if (task.getStatus().equals(Task.Status.QUEUED)) {
            task.setStatus(Task.Status.CANCELLED);
            task.getMeta().getComments().add("Task was cancelled by the user.");
            taskHandler.edit(task);
        }

        if (task.getStatus().equals(Task.Status.RUNNING)) {
            boolean cancelled = jpdiClient.cancel(id);
            if (!cancelled) {
                task.setStatus(Task.Status.CANCELLED);
                task.getMeta().getComments().add("Task was cancelled by the user.");
                taskHandler.edit(task);
            }
        }

        return Response.ok().build();
    }

    @GET
    @Path("/{id}/poll")
    @ApiOperation(value = "Poll Task by Id",
            notes = "Implements long polling",
            response = Task.class)
    public void poll(
            @ApiParam(value = "Authorization token") @HeaderParam("subjectid") String subjectId,
            @Suspended final AsyncResponse asyncResponse,
            @PathParam("id") String id) {

        executor.submit(() -> {
            asyncResponse.setTimeout(3, TimeUnit.MINUTES);
            try {
                Task task = taskHandler.longPoll(id);
                asyncResponse.resume(task);
            } catch (InterruptedException ex) {
                asyncResponse.resume(ex);
            }
        });
    }
}
