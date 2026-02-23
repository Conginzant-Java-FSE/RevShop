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

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Category fetched successfully",
                        category
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {

        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Category updated successfully",
                        updatedCategory
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(
            @PathVariable Long id) {

        categoryService.deleteCategory(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Category deleted successfully",
                        null
                )
        );
    }
}