package ch.cern.todo.repository;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskSpecification {
    public static Specification<Task> nameLike(String pattern) {
        return (root, query, cb)
            -> cb.like(root.get("name"), "%" + pattern + "%");
    }

    public static Specification<Task> descriptionLike(String pattern) {
        return (root, query, cb)
            -> cb.like(root.get("description"), "%" + pattern + "%");
    }

    public static Specification<Task> deadlineEqual(LocalDateTime deadline) {
        return (root, query, cb)
            -> cb.equal(root.get("deadline"), deadline);
    }

    public static Specification<Task> categoryEqual(Category category) {
        return (root, query, cb)
            -> cb.equal(root.get("category"), category);
    }

    public static Specification<Task> authorEqual(String author) {
        return (root, query, cb)
            -> cb.equal(root.get("author"), author);
    }
}
