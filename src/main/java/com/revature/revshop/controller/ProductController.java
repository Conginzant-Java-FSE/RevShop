package com.revature.revshop.controller;

import com.revature.revshop.dto.ProductDTO;
import com.revature.revshop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ProductDTO create(@Valid @RequestBody ProductDTO dto) {
        return productService.createProduct(dto);
    }

    @GetMapping("/search")
    public List<ProductDTO> search(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }
}
