package ch.cern.todo.service;

import ch.cern.todo.model.Category;
import ch.cern.todo.model.NewCategoryDto;
import ch.cern.todo.repository.CategoryRepository;
import jakarta.validation.Valid;
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

    public Category updateCategory(Long id, @Valid NewCategoryDto updatedCategory) {
        return categoryRepository.findById(id).map(
                existingCategory -> {
                    existingCategory.setName(updatedCategory.name());
                    existingCategory.setDescription(updatedCategory.description());
                    return categoryRepository.save(existingCategory);
                }
        ).orElseThrow();
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
