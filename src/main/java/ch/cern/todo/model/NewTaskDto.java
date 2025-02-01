package ch.cern.todo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewTaskDto (
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description,
    @NotNull String deadline,
    @NotNull Long categoryId
) {}
