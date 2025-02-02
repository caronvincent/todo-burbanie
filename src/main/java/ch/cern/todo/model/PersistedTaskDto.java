package ch.cern.todo.model;

public record PersistedTaskDto(
    Long id,
    String name,
    String description,
    String deadline,
    Long categoryId,
    String author
) {
    public PersistedTaskDto (Task task) {
        this(
            task.getId(),
            task.getName(),
            task.getDescription(),
            task.getDeadline().toString(),
            task.getCategory().getId(),
            task.getAuthor()
        );
    }
}
