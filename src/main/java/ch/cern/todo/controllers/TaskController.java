package ch.cern.todo.controllers;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.NewTaskDto;
import ch.cern.todo.model.PersistedTaskDto;
import ch.cern.todo.model.Task;
import ch.cern.todo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersistedTaskDto createTask(@Valid @RequestBody NewTaskDto newTaskDto, @AuthenticationPrincipal UserDetails userDetails) {
        return new PersistedTaskDto(taskService.saveTask(newTaskDto, userDetails.getUsername()));
    }

    @GetMapping("{id}")
    public PersistedTaskDto getTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return new PersistedTaskDto(taskService.getTask(id, userDetails));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(NOT_FOUND, "No task with ID " + id, e);
        }
    }

    @PutMapping("{id}")
    public PersistedTaskDto updateTask(
        @PathVariable Long id,
        @Valid @RequestBody NewTaskDto newTaskDto,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            return taskService.updateTask(id, newTaskDto, userDetails);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(NOT_FOUND, "No task with ID " + id, e);
        }
    }

    @DeleteMapping("{id}")
    public void deleteTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(id, userDetails);
    }

    @GetMapping("search")
    public List<PersistedTaskDto> search(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) LocalDateTime deadline,
            @RequestParam(required = false) Category category,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        boolean userIsAdmin = userDetails
            .getAuthorities()
            .stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (userIsAdmin) {
            return taskService.search(author, name, description, deadline, category);
        } else {
            if (author != null) {
                throw new ResponseStatusException(FORBIDDEN, "Only administrators may search by author");
            }
            return taskService.search(userDetails.getUsername(), name, description, deadline, category);
        }
    }
}
