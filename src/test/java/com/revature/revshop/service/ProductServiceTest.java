package com.revature.revshop.service;

import com.revature.revshop.dto.ProductDTO;
import com.revature.revshop.exception.InvalidInputException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Category;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Seller;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.CategoryRepository;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentMatchers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Seller seller;
    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        // Category
        category = new Category();
        category.setCategoryId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic items");

        // User → Seller
        User user = new User();
        user.setUserId(10L);
        user.setName("Test Seller");

        seller = new Seller(user);

        // Product entity
        product = new Product();
        product.setProductId(100L);
        product.setName("Laptop");
        product.setDescription("A powerful laptop");
        product.setMrp(new BigDecimal("80000"));
        product.setSellingPrice(new BigDecimal("70000"));
        product.setStockQuantity(50);
        product.setThresholdQuantity(5);
        product.setIsActive(true);
        product.setCategory(category);
        product.setSeller(seller);

        // Matching DTO
        productDTO = new ProductDTO();
        productDTO.setName("Laptop");
        productDTO.setDescription("A powerful laptop");
        productDTO.setMrp(new BigDecimal("80000"));
        productDTO.setSellingPrice(new BigDecimal("70000"));
        productDTO.setStockQuantity(50);
        productDTO.setThresholdQuantity(5);
        productDTO.setIsActive(true);
        productDTO.setCategoryId(1L);
        productDTO.setSellerId(10L);
    }

    @Test
    void createProduct_shouldSaveAndReturnDTO_whenValidInput() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(sellerRepository.findById(10L)).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(productDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getSellerId()).isEqualTo(10L);
        assertThat(result.getSellingPrice()).isEqualByComparingTo(new BigDecimal("70000"));

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowResourceNotFoundException_whenCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(productDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_shouldThrowUserNotFoundException_whenSellerNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(sellerRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(productDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Seller not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getAllProducts_shouldReturnPageOfDTOs_whenProductsExist() {
        Product product2 = new Product();
        product2.setProductId(101L);
        product2.setName("Phone");
        product2.setDescription("A smartphone");
        product2.setMrp(new BigDecimal("30000"));
        product2.setSellingPrice(new BigDecimal("25000"));
        product2.setStockQuantity(100);
        product2.setThresholdQuantity(10);
        product2.setIsActive(true);
        product2.setCategory(category);
        product2.setSeller(seller);

        Page<Product> pagedResponse = new PageImpl<>(Arrays.asList(product, product2));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(pagedResponse);

        Page<ProductDTO> results = productService.getAllProducts(PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting(ProductDTO::getName)
                .containsExactlyInAnyOrder("Laptop", "Phone");
    }

    @Test
    void getAllProducts_shouldReturnEmptyPage_whenNoProductsExist() {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<ProductDTO> results = productService.getAllProducts(PageRequest.of(0, 10));

        assertThat(results.getContent()).isEmpty();
    }

    @Test
    void getProductById_shouldReturnDTO_whenProductFound() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(100L);

        assertThat(result.getProductId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("Laptop");
    }

    @Test
    void getProductById_shouldThrowResourceNotFoundException_whenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void updateProduct_shouldUpdateFieldsAndReturnDTO_whenProductFound() {
        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setName("Gaming Laptop");
        updateDTO.setDescription("High performance gaming laptop");
        updateDTO.setMrp(new BigDecimal("120000"));
        updateDTO.setSellingPrice(new BigDecimal("100000"));
        updateDTO.setStockQuantity(20);
        updateDTO.setThresholdQuantity(3);
        updateDTO.setIsActive(true);
        updateDTO.setCategoryId(1L);
        updateDTO.setSellerId(10L);

        Product updatedProduct = new Product();
        updatedProduct.setProductId(100L);
        updatedProduct.setName("Gaming Laptop");
        updatedProduct.setDescription("High performance gaming laptop");
        updatedProduct.setMrp(new BigDecimal("120000"));
        updatedProduct.setSellingPrice(new BigDecimal("100000"));
        updatedProduct.setStockQuantity(20);
        updatedProduct.setThresholdQuantity(3);
        updatedProduct.setIsActive(true);
        updatedProduct.setCategory(category);
        updatedProduct.setSeller(seller);

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductDTO result = productService.updateProduct(100L, updateDTO);

        assertThat(result.getName()).isEqualTo("Gaming Laptop");
        assertThat(result.getSellingPrice()).isEqualByComparingTo(new BigDecimal("100000"));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowResourceNotFoundException_whenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(999L, productDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_shouldDeleteProduct_whenProductFound() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete((Product) (Object) product);

        assertThatCode(() -> productService.deleteProduct(100L))
                .doesNotThrowAnyException();

        verify(productRepository, times(1)).delete((Product) (Object) product);
    }

    @Test
    void deleteProduct_shouldThrowResourceNotFoundException_whenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void searchProducts_shouldReturnMatchingProducts_whenKeywordIsValid() {
        Page<Product> pagedResponse = new PageImpl<>(List.of(product));
        when(productRepository.findByNameContainingIgnoreCase(eq("laptop"), any(Pageable.class)))
                .thenReturn(pagedResponse);

        Page<ProductDTO> results = productService.searchProducts("laptop", PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void searchProducts_shouldReturnEmptyPage_whenNoMatch() {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
        when(productRepository.findByNameContainingIgnoreCase(eq("xyz"), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<ProductDTO> results = productService.searchProducts("xyz", PageRequest.of(0, 10));

        assertThat(results.getContent()).isEmpty();
    }

    @Test
    void searchProducts_shouldThrowInvalidInputException_whenKeywordIsBlank() {
        assertThatThrownBy(() -> productService.searchProducts("   ", PageRequest.of(0, 10)))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Search keyword is required");
    }

    @Test
    void searchProducts_shouldThrowInvalidInputException_whenKeywordIsNull() {
        assertThatThrownBy(() -> productService.searchProducts(null, PageRequest.of(0, 10)))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("Search keyword is required");
    }

    @Test
    void filterProducts_shouldReturnMatchingProducts_whenFiltersAreApplied() {
        Page<Product> pagedResponse = new PageImpl<>(List.of(product));
        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
                .thenReturn(pagedResponse);

        Page<ProductDTO> results = productService.filterProducts(100.0, 500000.0, 1L, PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void createProduct_shouldMapAllFieldsCorrectly_fromDTO() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(sellerRepository.findById(10L)).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(productDTO);

        assertThat(result.getMrp()).isEqualByComparingTo(new BigDecimal("80000"));
        assertThat(result.getStockQuantity()).isEqualTo(50);
        assertThat(result.getThresholdQuantity()).isEqualTo(5);
        assertThat(result.getIsActive()).isTrue();
    }
}
