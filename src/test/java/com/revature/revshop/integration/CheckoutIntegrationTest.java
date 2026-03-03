package com.revature.revshop.integration;

import com.revature.revshop.dto.*;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import com.revature.revshop.service.OrdersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CheckoutIntegrationTest {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private OrderItemsRepository orderItemsRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void fullCheckoutFlow_ShouldMapDataCorrectlyAndReduceStock() {
        // Setup Data
        Category cat = new Category();
        cat.setName("Test Category");
        Category savedCat = categoryRepository.save(cat);

        User sellerUser = new User();
        sellerUser.setName("Seller User");
        sellerUser.setEmail("seller-" + System.currentTimeMillis() + "@test.com");
        sellerUser.setPassword("pass");
        sellerUser.setRole(com.revature.revshop.model.Role.SELLER);

        Seller seller = new Seller();
        seller.setBusinessName("Test Business");
        seller.setUser(sellerUser);
        sellerUser.setSellerProfile(seller);

        User savedSellerUser = userRepository.save(sellerUser);
        Seller savedSeller = savedSellerUser.getSellerProfile();

        User user = new User();
        user.setName("Integration User");
        user.setEmail("int-" + System.currentTimeMillis() + "@test.com");
        user.setPassword("pass");
        user.setRole(com.revature.revshop.model.Role.BUYER);
        User savedUser = userRepository.save(user);

        Address addr = new Address();
        addr.setUser(savedUser);
        addr.setAddressLine("123 Test St");
        addr.setCity("Test City");
        addr.setState("Test State");
        addr.setCountry("Test Country");
        addr.setZipCode("12345");
        addr.setAddressType("SHIPPING");
        Address savedAddr = addressRepository.save(addr);

        Product prod = new Product();
        prod.setName("Int Product");
        prod.setCategory(savedCat);
        prod.setSeller(savedSeller);
        prod.setMrp(new BigDecimal("60.00"));
        prod.setSellingPrice(new BigDecimal("50.00"));
        prod.setStockQuantity(20);
        prod.setThresholdQuantity(5);
        Product savedProd = productRepository.save(prod);

        // Prepare Request
        OrderItemRequestDTO itemReq = new OrderItemRequestDTO();
        itemReq.setProductId(savedProd.getProductId());
        itemReq.setQuantity(5);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUserId(savedUser.getUserId());
        request.setShippingAddressId(savedAddr.getAddressId());
        request.setBillingAddressId(savedAddr.getAddressId());
        request.setPaymentMethod("COD");
        request.setItems(Collections.singletonList(itemReq));

        // Execute Checkout
        OrderResponseDTO response = ordersService.placeOrder(savedUser.getUserId(), request);

        // Verify Results
        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals(new BigDecimal("250.00"), response.getTotalAmount());

        // Verify Stock Reduction
        Product updatedProd = productRepository.findById(savedProd.getProductId()).get();
        assertEquals(15, updatedProd.getStockQuantity(), "Stock should be reduced by 5");

        // Verify Order Items Mapping
        long itemCount = orderItemsRepository.count();
        assertTrue(itemCount > 0);
    }
}
