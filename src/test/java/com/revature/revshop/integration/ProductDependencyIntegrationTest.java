package com.revature.revshop.integration;

import com.revature.revshop.model.Cart;
import com.revature.revshop.model.Category;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Review;
import com.revature.revshop.model.Seller;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.ReviewRepository;

import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.repository.CategoryRepository;
import com.revature.revshop.model.Role;
import com.revature.revshop.service.CartItemService;
import com.revature.revshop.service.CartService;
import com.revature.revshop.service.ReviewService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductDependencyIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setupDatabase() {
        testUser = new User();
        testUser.setName("testuser_dep_integration");
        testUser.setEmail("test_dep@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.BUYER);
        testUser = userRepository.save(testUser);

        Seller testSeller = new Seller();
        testSeller.setBusinessName("Test Business 2");
        testSeller.setUser(testUser);
        testSeller.setBusinessDescription("456 Test Ave");
        testUser.setSellerProfile(testSeller);
        testUser = userRepository.save(testUser);
        testSeller = testUser.getSellerProfile();

        Category testCategory = new Category();
        testCategory.setName("Toys");
        testCategory = categoryRepository.save(testCategory);

        testProduct = new Product();
        testProduct.setName("Lego");
        testProduct.setMrp(new BigDecimal("100.00"));
        testProduct.setSellingPrice(new BigDecimal("80.00"));
        testProduct.setStockQuantity(20);
        testProduct.setThresholdQuantity(5);
        testProduct.setSeller(testSeller);
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testProductSoftDeletionLeavesReviewsIntact() {
        // 1. Create a Review for the Product
        Review review = new Review(testProduct, testUser, 5, "Awesome Lego set!");
        review = reviewService.addReview(review);
        assertNotNull(review.getReviewId());

        // 2. Soft delete the Product (set isActive = false)
        testProduct.setIsActive(false);
        productRepository.save(testProduct);

        // 3. Verify Review Still Exists and references the product
        List<Review> productReviews = reviewService.getReviewsByProduct(testProduct);
        assertEquals(1, productReviews.size());
        assertEquals(5, productReviews.get(0).getRating());
    }

    @Test
    void testProductHardDeletionThrowsConstraintExceptionIfNotHandled() {
        // 1. Create Cart and add item
        Cart cart = cartService.findOrCreateCart(testUser);
        cartItemService.addItemToCart(cart, testProduct, 1);

        // 2. Create Review
        Review review = new Review(testProduct, testUser, 5, "Awesome Lego set!");
        reviewService.addReview(review);

        // flush changes to DB
        productRepository.flush();

        // 3. Attempt to Hard Delete Product - should fail due to foreign keys in
        // CartItems and Reviews
        // Note: The assertion captures the exception thrown by Spring Data JPA
        // regarding constraint violations.
        entityManager.flush();
        entityManager.clear();
        final Long productId = testProduct.getProductId();

        assertThrows(Exception.class, () -> {
            Product p = productRepository.findById(productId).orElseThrow();
            productRepository.delete(p);
            productRepository.flush(); // Force the delete to the DB to trigger constraint
        });
    }

    @Test
    void testProductManualCleanupBeforeHardDeletion() {
        // 1. Create Cart and add item
        Cart cart = cartService.findOrCreateCart(testUser);
        cartItemService.addItemToCart(cart, testProduct, 1);

        // 2. Create Review
        Review review = new Review(testProduct, testUser, 5, "Awesome Lego set!");
        review = reviewService.addReview(review);

        // 5. Cleanup before deletion
        cartItemService.clearCart(cart);
        reviewRepository.delete(review);
        entityManager.flush();
        entityManager.clear(); // Clear to remove stale CartItem references from context

        // 6. Delete product
        assertDoesNotThrow(() -> {
            productRepository.delete(testProduct);
            productRepository.flush();
        });

        assertFalse(productRepository.findById(testProduct.getProductId()).isPresent());
    }
}
