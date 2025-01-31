package ch.cern.todo.service;

import ch.cern.todo.model.Category;
import ch.cern.todo.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }
}
