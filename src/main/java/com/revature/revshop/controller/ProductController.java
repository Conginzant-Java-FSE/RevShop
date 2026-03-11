package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.dto.ProductDTO;
import com.revature.revshop.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

        private static final Logger log = LoggerFactory.getLogger(ProductController.class);
        private static final String FETCHED_SUCCESSFULLY = "Products fetched successfully";

        private final ProductService productService;

        public ProductController(ProductService productService) {
                this.productService = productService;
        }

        @PostMapping
        public ResponseEntity<ApiResponse<ProductDTO>> create(
                        @Valid @RequestBody ProductDTO dto) {

                log.info("POST /api/products");
                ProductDTO saved = productService.createProduct(dto);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>(
                                                "Product created successfully",
                                                saved));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "productId") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction) {

                Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                log.info("GET /api/products - page={} size={}", page, size);

                Page<ProductDTO> products = productService.getAllProducts(pageable);

                return ResponseEntity.ok(
                                new ApiResponse<>(
                                                FETCHED_SUCCESSFULLY,
                                                products));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<ProductDTO>> getById(
                        @PathVariable Long id) {

                log.info("GET /api/products/{}", id);
                ProductDTO product = productService.getProductById(id);

                return ResponseEntity.ok(
                                new ApiResponse<>(
                                                "Product fetched successfully",
                                                product));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<ProductDTO>> update(
                        @PathVariable Long id,
                        @Valid @RequestBody ProductDTO dto) {

                log.info("PUT /api/products/{}", id);
                ProductDTO updated = productService.updateProduct(id, dto);

                return ResponseEntity.ok(
                                new ApiResponse<>(
                                                "Product updated successfully",
                                                updated));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<String>> delete(
                        @PathVariable Long id) {

                log.info("DELETE /api/products/{}", id);
                productService.deleteProduct(id);

                return ResponseEntity.ok(
                                new ApiResponse<>(
                                                "Product deleted successfully",
                                                null));
        }

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<Page<ProductDTO>>> search(
                        @RequestParam String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "productId") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction) {

                Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                log.info("GET /api/products/search - keyword={}", keyword);
                Page<ProductDTO> products = productService.searchProducts(keyword, pageable);

                return ResponseEntity.ok(
                                new ApiResponse<>(
                                                FETCHED_SUCCESSFULLY,
                                                products));
        }

        @GetMapping("/filter")
        public ResponseEntity<ApiResponse<Page<ProductDTO>>> filter(
                        @RequestParam java.util.Map<String, String> allParams,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "productId") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction) {

                Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                String keyword = allParams.get("keyword");
                Double minPrice = allParams.containsKey("minPrice") ? Double.valueOf(allParams.get("minPrice")) : null;
                Double maxPrice = allParams.containsKey("maxPrice") ? Double.valueOf(allParams.get("maxPrice")) : null;
                Long categoryId = allParams.containsKey("categoryId") ? Long.valueOf(allParams.get("categoryId"))
                                : null;
                Integer minRating = allParams.containsKey("minRating") ? Integer.valueOf(allParams.get("minRating"))
                                : null;
                Integer minDiscount = allParams.containsKey("minDiscount")
                                ? Integer.valueOf(allParams.get("minDiscount"))
                                : null;

                // Extract dynamic filters (exclude known standard query params)
                java.util.Map<String, String> dynamicFilters = new java.util.HashMap<>(allParams);
                dynamicFilters.keySet().removeAll(java.util.Arrays.asList(
                                "keyword", "minPrice", "maxPrice", "categoryId", "minRating", "minDiscount",
                                "page", "size", "sortBy", "direction"));

                log.info("GET /api/products/filter - keyword={} categoryId={} dynamicFilters={}", keyword, categoryId,
                                dynamicFilters);
                Page<ProductDTO> products = productService.filterProducts(keyword, minPrice, maxPrice, categoryId,
                                minRating, minDiscount, dynamicFilters, pageable);

                return ResponseEntity.ok(
                                new ApiResponse<>(
                                                "Filtered products fetched successfully",
                                                products));
        }

        @GetMapping("/seller/{sellerId}")
        public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProductsBySeller(
                        @PathVariable Long sellerId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "productId") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction) {

                Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                log.info("GET /api/products/seller/{} - page={} size={}", sellerId, page, size);
                Page<ProductDTO> products = productService.getProductsBySeller(sellerId, pageable);
                return ResponseEntity.ok(new ApiResponse<>(FETCHED_SUCCESSFULLY, products));
        }

        @PatchMapping("/{id}/toggle-active")
        public ResponseEntity<ApiResponse<ProductDTO>> toggleActive(@PathVariable Long id) {

                log.info("PATCH /api/products/{}/toggle-active", id);
                ProductDTO updated = productService.toggleActive(id);
                return ResponseEntity.ok(new ApiResponse<>("Product status toggled", updated));
        }
}