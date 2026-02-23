package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.dto.CategoryDTO;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }



    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO) {

        CategoryDTO savedCategory = categoryService.createCategory(categoryDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Category created successfully",
                        savedCategory
                ));
    }



    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {

        List<CategoryDTO> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Categories fetched successfully",
                        categories
                )
        );
    }



    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(
            @PathVariable Long id) {

        CategoryDTO category = categoryService.getCategoryById(id);

        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Category fetched successfully",
                        category
                )
        );
    }
}