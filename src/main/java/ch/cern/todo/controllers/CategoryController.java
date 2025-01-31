package ch.cern.todo.controllers;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.NewCategoryDto;
import ch.cern.todo.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@EnableMethodSecurity
@RequestMapping("categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // A more sophisticated system could have been built where each
    // category is bound to a user, giving the same access control as tasks.
    // Instead, only admins can create/update/delete categories.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category addCategory(@RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.saveCategory(new Category(newCategoryDto));
    }

    @GetMapping("{id}")
    public Category getCategory(@PathVariable Long id) {
        try {
            return categoryService.getCategory(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(NOT_FOUND, "No category with ID " + id, e);
        }
    }
}
