package com.revature.revshop.integration;

import com.revature.revshop.model.Cart;
import com.revature.revshop.model.CartItem;
import com.revature.revshop.model.Category;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.Seller;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.CartItemRepository;
import com.revature.revshop.repository.CartRepository;
import com.revature.revshop.repository.CategoryRepository;
import com.revature.revshop.repository.ProductRepository;

import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.model.Role;
import com.revature.revshop.service.CartItemService;
import com.revature.revshop.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Use a test profile to potentially use H2 or isolate DB
@Transactional
class CartIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setupDatabase() {
        testUser = new User();
        testUser.setName("testuser_cart_integration");
        testUser.setEmail("test_cart@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.BUYER);

        Seller testSeller = new Seller();
        testSeller.setBusinessName("Test Business");
        testSeller.setUser(testUser);
        testSeller.setBusinessDescription("123 Test St");
        testUser.setSellerProfile(testSeller);

        testUser = userRepository.save(testUser);
        testSeller = testUser.getSellerProfile();

        Category testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory = categoryRepository.save(testCategory);

        testProduct1 = new Product();
        testProduct1.setName("Laptop");
        testProduct1.setMrp(new BigDecimal("1200.00"));
        testProduct1.setSellingPrice(new BigDecimal("1000.00"));
        testProduct1.setStockQuantity(10);
        testProduct1.setThresholdQuantity(2);
        testProduct1.setSeller(testSeller);
        testProduct1.setCategory(testCategory);
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product();
        testProduct2.setName("Mouse");
        testProduct2.setMrp(new BigDecimal("50.00"));
        testProduct2.setSellingPrice(new BigDecimal("40.00"));
        testProduct2.setStockQuantity(50);
        testProduct2.setThresholdQuantity(5);
        testProduct2.setSeller(testSeller);
        testProduct2.setCategory(testCategory);
        testProduct2 = productRepository.save(testProduct2);
    }

    @Test
    void testEndToEndCartLifecycle() {
        // 1. Create/Find Cart
        Cart cart = cartService.findOrCreateCart(testUser);
        assertNotNull(cart);
        assertNotNull(cart.getCartId());

        // 2. Add items to cart
        CartItem item1 = cartItemService.addItemToCart(cart, testProduct1, 1);
        CartItem item2 = cartItemService.addItemToCart(cart, testProduct2, 2);

        // Assert items saved
        assertNotNull(item1.getCartItemId());
        assertNotNull(item2.getCartItemId());

        // Use findById explicitly to avoid lazily loading issues in test tx
        cart = cartRepository.findById(cart.getCartId()).orElseThrow();

        // 3. Verify Cart Items
        List<CartItem> itemsInCart = cartItemService.getCartItemsByCart(cart);
        assertEquals(2, itemsInCart.size());

        // 4. Update quantity
        CartItem updatedItem2 = cartItemService.updateItemQuantity(item2.getCartItemId(), 3);
        assertEquals(3, updatedItem2.getQuantity());

        // Set cart items manually for total calculation if lazy loading isn't triggered
        cart.setCartItems(cartItemService.getCartItemsByCart(cart));

        // 5. Calculate Total (1 x 1000) + (3 x 40) = 1120
        BigDecimal total = cartService.calculateCartTotal(cart);
        assertEquals(new BigDecimal("1120.00"), total);

        // 6. Remove one item
        cartItemService.removeItemFromCart(cart, testProduct1);
        cartItemRepository.flush();
        entityManager.clear();
        cart = cartRepository.findById(cart.getCartId()).orElseThrow();
        itemsInCart = cartItemService.getCartItemsByCart(cart);
        assertEquals(1, itemsInCart.size());

        // 7. Calculate new total (3 x 40) = 120
        cart.getCartItems().clear();
        cart.getCartItems().addAll(itemsInCart);
        total = cartService.calculateCartTotal(cart);
        assertEquals(0, new BigDecimal("120.00").compareTo(total));

        // 8. Clear cart
        cartItemService.clearCart(cart);
        cartItemRepository.flush();
        itemsInCart = cartItemService.getCartItemsByCart(cart);
        assertTrue(itemsInCart.isEmpty());
    }
}
