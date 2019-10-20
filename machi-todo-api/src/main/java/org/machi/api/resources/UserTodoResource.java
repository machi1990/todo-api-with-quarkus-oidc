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
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.util.List;

@Counted
@Path("/todos")
@RolesAllowed("user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserTodoResource {
    @Inject
    Principal principal;

    @Inject
    TodoRepository todoRepository;

    @GET
    @Metric
    @Operation(summary = "list all todo items. The endpoint also allows filtering by status")
    public List<Todo> getAll(@QueryParam("status") TodoItemStatus status) {
        return todoRepository.findAll(principal.getName(), status);
    }

    @GET
    @Path("/{id}")
    @Metric
    @Operation(summary = "retrieve a todo item, given its id.")
    public Todo getOne(@PathParam("id") Long id) {
        Todo entity = todoRepository.findByIdAndCreator(id, principal.getName());
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", Response.Status.NOT_FOUND);
        }

        return entity;
    }

    @POST
    @Transactional
    @Metric
    @Operation(summary = "create a new todo item. The will set the completion status to false")
    public Response create(@Valid Todo item) {
        item.createdBy = principal.getName();
        item.status = TodoItemStatus.pending;
        todoRepository.persist(item);
        return Response.status(Response.Status.CREATED).entity(item.id).build();
    }

    @PUT
    @Metric
    @Path("/{id}")
    @Transactional
    @Operation(summary = "update a todo item given by id")
    public Response update(@Valid Todo todo, @PathParam("id") Long id) {
        Todo entity = getOne(id);

        entity.id = id;
        entity.status = todo.status;
        entity.order = todo.order;
        entity.title = todo.title;
        entity.description = todo.description;

        return Response.ok(entity).build();
    }

    @DELETE
    @Transactional
    @Metric
    @Operation(summary = "delete all completed todo items")
    public Response deleteCompleted() {
        todoRepository.deleteCompleted(principal.getName());
        return Response.noContent().build();
    }


    @DELETE
    @Metric
    @Transactional
    @Path("/{id}")
    @Operation(summary = "delete a given todo item")
    public Response deleteOne(@PathParam("id") Long id) {
        Todo entity = getOne(id);
        todoRepository.delete(entity);
        return Response.noContent().build();
    }
}
