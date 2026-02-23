package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.dto.ProductDTO;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> create(
            @Valid @RequestBody ProductDTO dto) {

        ProductDTO saved = productService.createProduct(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Product created successfully",
                        saved
                ));
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> search(
            @RequestParam String keyword) {

        List<ProductDTO> products =
                productService.searchProducts(keyword);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products fetched successfully",
                        products
                )
        );
    }
}