package org.machi.api.resources;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.machi.api.entity.TodoItemStatus;
import org.machi.api.entity.Todo;
import org.machi.api.repository.TodoRepository;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Counted
@Path("/todos/admin")
@RolesAllowed("admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminTodoResources {
    @Inject
    TodoRepository todoRepository;

    @GET
    @Metric
    @Operation(summary = "list all todo items. The endpoint also allows filtering by status or item creator")
    public List<Todo> getAll(@QueryParam("createdBy") String createdBy, @QueryParam("status") TodoItemStatus status) {
        return todoRepository.findAll(createdBy, status);
    }

    @DELETE
    @Transactional
    @Operation(summary = "delete all users todo items")
    public Response deleteAll() {
        todoRepository.deleteAll();
        return Response.noContent().build();
    }
}
