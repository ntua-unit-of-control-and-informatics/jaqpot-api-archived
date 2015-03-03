/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.service.resource;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jaqpot.core.data.TaskHandler;
import org.jaqpot.core.model.Task;

/**
 *
 * @author hampos
 */
@Path("task")
public class TaskResource {

    @EJB
    TaskHandler taskHandler;

    @GET
    @Produces("text/uri-list")
    public Response getTasks() {
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response getTask(@PathParam("id") String id) {
        Task task = taskHandler.find(id);
        if (task == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(task).build();
    }

}
