package ch.cern.todo.controllers;

import ch.cern.todo.model.NewTaskDto;
import ch.cern.todo.model.PersistedTaskDto;
import ch.cern.todo.model.Task;
import ch.cern.todo.repository.TaskRepository;
import ch.cern.todo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@EnableMethodSecurity
@RequestMapping("tasks")
public class TaskController {
    private final TaskService taskService;
    private final TaskRepository taskRepository;

    public TaskController(TaskService taskService, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersistedTaskDto createTask(@Valid @RequestBody NewTaskDto newTaskDto, @AuthenticationPrincipal UserDetails userDetails) {
        return new PersistedTaskDto(taskService.saveTask(newTaskDto, userDetails.getUsername()));
    }

    private void checkTaskRights(Task task, UserDetails userDetails) {
        if (userDetails
            .getAuthorities()
            .stream()
            .noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
            && !task.getAuthor().equals(userDetails.getUsername())
        ) {
            throw new ResponseStatusException(FORBIDDEN, "You are not authorized to view task " + task.getId());
        }
    }

    @GetMapping("{id}")
    public PersistedTaskDto getTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Task found = taskService.getTask(id);
            checkTaskRights(found, userDetails);
            return new PersistedTaskDto(found);
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
            Task found = taskService.getTask(id);
            checkTaskRights(found, userDetails);
            return taskService.updateTask(found, newTaskDto);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(NOT_FOUND, "No task with ID " + id, e);
        }
    }

    @DeleteMapping("{id}")
    public void deleteCategory(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) return;

        checkTaskRights(optionalTask.orElseThrow(), userDetails);
        taskService.deleteTask(id);
    }
}
