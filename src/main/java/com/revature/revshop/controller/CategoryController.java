package com.revature.revshop.controller;

import com.revature.revshop.dto.CategoryDTO;
import com.revature.revshop.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public CategoryDTO create(@Valid @RequestBody CategoryDTO dto) {
        return categoryService.createCategory(dto);
    }

    @GetMapping
    public List<CategoryDTO> getAll() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public CategoryDTO getById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }
}
