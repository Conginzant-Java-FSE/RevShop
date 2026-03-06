package com.revature.revshop.service;

import com.revature.revshop.dto.CategoryDTO;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Category;
import com.revature.revshop.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    private static final String CATEGORY_NOT_FOUND = "Category not found";
    private static final String PARENT_CATEGORY_NOT_FOUND = "Parent category not found";

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDTO createCategory(CategoryDTO dto) {
        log.info("Creating category name={}", dto.getName());

        if (categoryRepository.existsByName(dto.getName())) {
            throw new InvalidInputException("Category already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        if (dto.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(dto.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(PARENT_CATEGORY_NOT_FOUND));
            category.setParentCategory(parent);
        }

        Category saved = categoryRepository.save(category);

        return convertToDTO(saved);
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<CategoryDTO> getSubCategories(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException(PARENT_CATEGORY_NOT_FOUND);
        }
        return categoryRepository.findByParentCategoryCategoryId(parentId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public CategoryDTO getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));

        return convertToDTO(category);
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        log.info("Updating category id={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));

        if (!category.getName().equals(dto.getName()) &&
                categoryRepository.existsByName(dto.getName())) {
            throw new InvalidInputException("Category name already exists");
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        Category updated = categoryRepository.save(category);

        return convertToDTO(updated);
    }

    public void deleteCategory(Long id) {
        log.info("Deleting category id={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }

    private CategoryDTO convertToDTO(Category category) {

        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        if (category.getParentCategory() != null) {
            dto.setParentCategoryId(category.getParentCategory().getCategoryId());
        }

        return dto;
    }
}
