package com.revature.revshop.service;

import com.revature.revshop.model.Category;
import com.revature.revshop.model.Product;
import com.revature.revshop.repository.CategoryRepository;
import com.revature.revshop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product createProduct(Product product) {

        if (product == null) {
            throw new RuntimeException("Product cannot be null");
        }

        if (product.getCategory() == null ||
                product.getCategory().getCategoryId() == null) {
            throw new RuntimeException("Valid category is required");
        }

        Category category = categoryRepository.findById(
                        product.getCategory().getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setCategory(category);

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Integer productId) {

        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }

    public List<Product> getActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public List<Product> getProductsByCategory(Integer categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new RuntimeException("Search keyword is required");
        }

        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public void deleteProduct(Integer productId) {

        Product product = getProductById(productId);
        productRepository.delete(product);
    }
}
