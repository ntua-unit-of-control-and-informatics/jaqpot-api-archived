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
package org.jaqpot.core.service.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import javax.ejb.EJB;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.model.Task;
import org.jaqpot.core.model.builder.ErrorReportBuilder;
import org.jaqpot.core.model.factory.ErrorReportFactory;

/**
 *
 * @author hampos
 */
@Path("task")
@Api(value = "/task", description = "Tasks API")
public class TaskResource {

    @Context UriInfo uriInfo;
    
    @EJB
    TaskHandler taskHandler;

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
    public Response getTasks(
            @ApiParam(value = "Creator of the task (username)") @QueryParam("creator") String creator,
            @ApiParam(value = "Status of the task") @QueryParam("status") String status
    ) {
        return Response.ok(taskHandler.findAll()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(value = "Finds Task by Id",
            notes = "Finds specified Task",
            response = Task.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Task is found"),
        @ApiResponse(code = 404, message = "This task was not found."),
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response getTask(
            @ApiParam(value = "ID of the task to be retrieved") @PathParam("id") String id) {
        Task task = taskHandler.find(id);
        System.out.println("task:" + task);
        if (task == null) {
            return Response
                    .ok(ErrorReportFactory.notFoundError(uriInfo.getPath()))
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }
        return Response.ok(task).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ApiOperation(value = "Deletes a Task of given ID",
            notes = "Deletes a Task given its ID in the URI. When the DELETE method is applied, the task "
            + "is interrupted and tagged as CANCELLED."
    )
    @ApiResponses(value = {          
        @ApiResponse(code = 200, message = "Task deleted successfully"),
        @ApiResponse(code = 401, message = "Wrong, missing or insufficient credentials. Error report is produced."),
        @ApiResponse(code = 403, message = "This is a forbidden operation (do not attempt to repeat it)."),
        @ApiResponse(code = 404, message = "There is no such task; no deletion took place!"),        
        @ApiResponse(code = 500, message = "Internal server error - this request cannot be served.")
    })
    public Response deleteTask(
            @ApiParam(value = "ID of the task which is to be deleted.", required = true) @PathParam("id") String id,
            @HeaderParam("subjectid") String subjectId) {
        try {
            taskHandler.remove(new Task(id));
        } catch (Exception e) {
            ErrorReport error = new ErrorReport("1234");
            error.setCode("msg::" + e.getMessage());
            error.setDetails("sth went wrong!");
            return Response
                    .ok(error)
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
        return Response.ok("{\"status\":\"fine\"}").build();
    }
}
