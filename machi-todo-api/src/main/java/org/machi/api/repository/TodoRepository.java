package org.machi.api.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import org.machi.api.entity.TodoItemStatus;
import org.machi.api.entity.Todo;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class TodoRepository implements PanacheRepository<Todo> {
    public TodoRepository() {
    }

    public Todo findByIdAndCreator(Long id, String createdBy) {
        return Todo.find("id = ?1 and createdBy = ?2", id, createdBy).firstResult();
    }

    public List<Todo> findAll(String createdBy, TodoItemStatus status) {
        return buildAndExecuteQuery(createdBy, status);
    }

    private List<Todo> buildAndExecuteQuery(String createdBy, TodoItemStatus status) {
        StringBuilder query = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        if (createdBy != null) {
            query.append("createdBy = :createdBy");
            params.put("createdBy", createdBy);
        }

        if (status != null) {
            if (!params.isEmpty()) {
                query.append(" and ");
            }

            query.append(" status = :status");
            params.put("status", status);
        }

        return Todo.list(query.toString(), Sort.by("order"), params);
    }

    public long deleteCompleted(String createdBy) {
        return Todo.delete("status = ?1 and createdBy = ?2", TodoItemStatus.completed, createdBy);
    }
}
