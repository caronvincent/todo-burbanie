package ch.cern.todo.controllers;

import ch.cern.todo.model.NewTaskDto;
import ch.cern.todo.model.PersistedTaskDto;
import ch.cern.todo.model.Task;
import ch.cern.todo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableMethodSecurity
@RequestMapping("tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersistedTaskDto createTask(@Valid @RequestBody NewTaskDto newTaskDto, @AuthenticationPrincipal UserDetails userDetails) {
        Task newTask = taskService.saveTask(newTaskDto, userDetails.getUsername());
        return new PersistedTaskDto(
            newTask.getId(),
            newTask.getName(),
            newTask.getDescription(),
            newTask.getDeadline().toString(),
            newTask.getCategory().getId(),
            newTask.getAuthor()
        );
    }
}
