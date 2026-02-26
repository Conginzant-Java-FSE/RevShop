package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.dto.ProductDTO;
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


    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {

        List<ProductDTO> products = productService.getAllProducts();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products fetched successfully",
                        products
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getById(
            @PathVariable Long id) {

        ProductDTO product = productService.getProductById(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product fetched successfully",
                        product
                )
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO dto) {

        ProductDTO updated = productService.updateProduct(id, dto);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product updated successfully",
                        updated
                )
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product deleted successfully",
                        null
                )
        );
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