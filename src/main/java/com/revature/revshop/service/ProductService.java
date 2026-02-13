package com.revature.revshop.service;

import com.revature.revshop.dto.ProductDTO;
import com.revature.revshop.model.Category;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Seller;
import com.revature.revshop.repository.CategoryRepository;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SellerRepository sellerRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sellerRepository = sellerRepository;
    }

    public ProductDTO createProduct(ProductDTO dto) {

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Seller seller = sellerRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setMrp(dto.getMrp());
        product.setSellingPrice(dto.getSellingPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setThresholdQuantity(dto.getThresholdQuantity());
        product.setIsActive(dto.getIsActive());
        product.setCategory(category);
        product.setSeller(seller);

        Product saved = productRepository.save(product);

        return convertToDTO(saved);
    }

    public List<ProductDTO> searchProducts(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new RuntimeException("Search keyword is required");
        }

        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductDTO convertToDTO(Product product) {

        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setMrp(product.getMrp());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setThresholdQuantity(product.getThresholdQuantity());
        dto.setIsActive(product.getIsActive());
        dto.setCategoryId(product.getCategory().getCategoryId());
        dto.setSellerId(product.getSeller().getUserId());

        return dto;
    }
}
