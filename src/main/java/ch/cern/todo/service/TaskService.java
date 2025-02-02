package ch.cern.todo.service;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.NewTaskDto;
import ch.cern.todo.model.PersistedTaskDto;
import ch.cern.todo.model.Task;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public Task saveTask(@Valid NewTaskDto newTaskDto, String username) {
        Category category = categoryRepository.findById(newTaskDto.categoryId()).orElseThrow();
        return taskRepository.save(new Task(newTaskDto, category, username));
    }

    public Task getTask(Long id) {
        return taskRepository.findById(id).orElseThrow();
    }

    public PersistedTaskDto updateTask(Task taskToUpdate, @Valid NewTaskDto newTaskDto) {
        taskToUpdate.setName(newTaskDto.name());
        taskToUpdate.setDescription(newTaskDto.description());
        taskToUpdate.setCategory(categoryRepository.findById(newTaskDto.categoryId()).orElseThrow());
        taskToUpdate.setDeadline(LocalDateTime.parse(newTaskDto.deadline()));

        return new PersistedTaskDto(taskRepository.save(taskToUpdate));
    }
}
