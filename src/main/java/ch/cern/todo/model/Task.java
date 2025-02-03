package ch.cern.todo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "task_id")
    private Long id;

    @Column(
        name = "task_name",
        length = 100,
        nullable = false
    )
    private String name;

    @Column(
        name = "task_description",
        length = 500
    )
    private String description;

    @Column(
        name = "deadline",
        nullable = false
    )
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime deadline;

    @JoinColumn(
        name = "category_id",
        nullable = false
    )
    @ManyToOne
    private Category category;

    @Column(nullable = false)
    private String author;

    public Task(NewTaskDto newTaskDto, Category category, String username) {
        this.name = newTaskDto.name();
        this.description = newTaskDto.description();
        this.deadline = newTaskDto.deadline();
        this.category = category;
        this.author = username;
    }

    public Task() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
