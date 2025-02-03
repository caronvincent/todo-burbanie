package ch.cern.todo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record NewTaskDto (
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description,
    @NotNull LocalDateTime deadline,
    @NotNull Long categoryId
) {
    public NewTaskDto(String name, String description, String deadline, Long categoryId) {
        this(name, description, LocalDateTime.parse(deadline), categoryId);
    }
}
