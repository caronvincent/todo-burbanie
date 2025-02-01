package ch.cern.todo.model;

public record PersistedTaskDto(
    Long id,
    String name,
    String description,
    String deadline,
    Long categoryId,
    String author
) {}
