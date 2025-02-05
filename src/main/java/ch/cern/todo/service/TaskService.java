package ch.cern.todo.service;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.NewTaskDto;
import ch.cern.todo.model.PersistedTaskDto;
import ch.cern.todo.model.Task;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static ch.cern.todo.repository.TaskSpecification.*;
import static org.springframework.data.jpa.domain.Specification.where;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public Task saveTask(@Valid NewTaskDto newTaskDto, String username) {
        try {
            Category category = categoryRepository.findById(newTaskDto.categoryId()).orElseThrow();
            return taskRepository.save(new Task(newTaskDto, category, username));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Category " + newTaskDto.categoryId() + " not found");
        }
    }

    public Task getTask(Long id) {
        try {
            return taskRepository.findById(id).orElseThrow();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Task " + id + " not found");
        }
    }

    public Task getTask(Long id,  UserDetails userDetails) {
        Task found = getTask(id);
        checkTaskRights(found, userDetails);
        return found;
    }

    public PersistedTaskDto updateTask(Task taskToUpdate, @Valid NewTaskDto newTaskDto) {
        try {
            taskToUpdate.setCategory(categoryRepository.findById(newTaskDto.categoryId()).orElseThrow());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Category " + newTaskDto.categoryId() + " not found");
        }

        taskToUpdate.setName(newTaskDto.name());
        taskToUpdate.setDescription(newTaskDto.description());
        taskToUpdate.setDeadline(newTaskDto.deadline());

        return new PersistedTaskDto(taskRepository.save(taskToUpdate));
    }

    public PersistedTaskDto updateTask(Long id, @Valid NewTaskDto newTaskDto, UserDetails userDetails) {
        Task found = getTask(id);
        checkTaskRights(found, userDetails);

        return updateTask(found, newTaskDto);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public void deleteTask(Long id, UserDetails userDetails) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) return;
        checkTaskRights(optionalTask.orElseThrow(), userDetails);

        deleteTask(id);
    }

    public List<PersistedTaskDto> search(
        String author,
        String name,
        String description,
        LocalDateTime deadline,
        Category category
    ) {
        Specification<Task> spec = where(null);
        if (author != null) {
            spec = spec.and(authorEqual(author));
        }
        if (name != null) {
            spec = spec.and(nameLike(name));
        }
        if (description != null) {
            spec = spec.and(descriptionLike(description));
        }
        if (deadline != null) {
            spec = spec.and(deadlineEqual(deadline));
        }
        if (category != null) {
            spec = spec.and(categoryEqual(category));
        }

        List<PersistedTaskDto> output = new ArrayList<>();
        taskRepository.findAll(spec).forEach(task -> output.add(new PersistedTaskDto(task)));
        return output;
    }

    public List<PersistedTaskDto> search(
        String author,
        String name,
        String description,
        LocalDateTime deadline,
        Category category,
        UserDetails userDetails
    ) {
        if (userIsAdmin(userDetails)) {
            return search(author, name, description, deadline, category);
        }

        if (author != null) {
            throw new ResponseStatusException(FORBIDDEN, "Only administrators may search by author");
        }

        return search(userDetails.getUsername(), name, description, deadline, category);
    }

    private void checkTaskRights(Task task, UserDetails userDetails) {
        if (!userIsAdmin(userDetails) && !task.getAuthor().equals(userDetails.getUsername())) {
            throw new ResponseStatusException(FORBIDDEN, "You are not authorized to interact with task " + task.getId());
        }
    }

    private boolean userIsAdmin(UserDetails userDetails) {
        return userDetails
            .getAuthorities()
            .stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
