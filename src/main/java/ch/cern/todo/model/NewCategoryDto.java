package ch.cern.todo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewCategoryDto(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 500) String description
) {}
