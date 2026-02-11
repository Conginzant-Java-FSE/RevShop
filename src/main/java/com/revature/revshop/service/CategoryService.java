package com.revature.revshop.service;

import com.revature.revshop.model.Category;
import com.revature.revshop.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {

        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name is required");
        }

        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category already exists");
        }

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Integer categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
    }

    public void deleteCategory(Integer categoryId) {

        Category category = getCategoryById(categoryId);
        categoryRepository.delete(category);
    }
}
