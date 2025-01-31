package ch.cern.todo.model;

import jakarta.persistence.*;

@Entity(name = "task_categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "category_id")
    private Long id;

    @Column(
        name = "category_name",
        length = 100,
        nullable = false
    )
    private String name;

    @Column(
        name = "category_description",
        length = 500,
        nullable = false
    )
    private String description;

    public Category() {}

    public Category(NewCategoryDto input) {
        this.name = input.name();
        this.description = input.description();
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
}
