package com.revature.revshop.service;

import com.revature.revshop.dto.CategoryDTO;
import com.revature.revshop.model.Category;
import com.revature.revshop.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDTO createCategory(CategoryDTO dto) {

        if (categoryRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        Category saved = categoryRepository.save(category);

        return convertToDTO(saved);
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return convertToDTO(category);
    }

    private CategoryDTO convertToDTO(Category category) {

        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        return dto;
    }
}
