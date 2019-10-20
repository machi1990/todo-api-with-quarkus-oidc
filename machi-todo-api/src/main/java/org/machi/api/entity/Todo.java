package org.machi.api.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

@Entity
public class Todo extends PanacheEntity {

    @NotBlank
    @Column(unique = true)
    public String title;

    public TodoItemStatus status = TodoItemStatus.pending;

    @Column(name = "ordering")
    public int order;

    @Column(nullable = false)
    public String createdBy;

    @Column(length = 500)
    public String description;
}
