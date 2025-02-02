package ch.cern.todo.service;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.NewTaskDto;
import ch.cern.todo.model.PersistedTaskDto;
import ch.cern.todo.model.Task;
import ch.cern.todo.repository.CategoryRepository;
import ch.cern.todo.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ch.cern.todo.repository.TaskSpecification.*;
import static org.springframework.data.jpa.domain.Specification.where;

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

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<PersistedTaskDto> search(String author, String name, String description, LocalDateTime deadline, Category category) {
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
}
