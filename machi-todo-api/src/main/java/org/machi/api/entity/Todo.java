package org.machi.api.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.UniqueConstraint;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotBlank;

@Entity
@Table(
        name="todo",
        uniqueConstraints=
        @UniqueConstraint(columnNames={"title", "createdBy"})
)
public class Todo extends PanacheEntityBase {
    @Id
    @SequenceGenerator(name= "TODO_SEQUENCE", sequenceName = "TODO_SEQUENCE_ID", allocationSize = 1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="TODO_SEQUENCE")
    public Long id;

    @NotBlank
    public String title;

    public TodoItemStatus status = TodoItemStatus.pending;

    @Column(name = "ordering")
    public int order;

    @Column(nullable = false)
    public String createdBy;

    @Column(length = 500)
    public String description;
}
