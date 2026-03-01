package com.revature.revshop.service;

import com.revature.revshop.model.Cart;
import com.revature.revshop.model.CartItem;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setUserId(1L);
        testCart = new Cart(testUser);
    }

    @Test
    void testGetCartByUser() {
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        Optional<Cart> result = cartService.getCartByUser(testUser);
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get().getUser());
    }

    @Test
    void testCreateCart() {
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        Cart result = cartService.createCart(testUser);
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testSaveCart() {
        when(cartRepository.save(testCart)).thenReturn(testCart);
        Cart result = cartService.saveCart(testCart);
        assertEquals(testCart, result);
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void testFindOrCreateCart_Existing() {
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        Cart result = cartService.findOrCreateCart(testUser);
        assertEquals(testCart, result);
    }

    @Test
    void testFindOrCreateCart_New() {
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        Cart result = cartService.findOrCreateCart(testUser);
        assertEquals(testCart, result);
    }

    @Test
    void testCalculateCartTotal() {
        Product p1 = new Product();
        p1.setSellingPrice(new BigDecimal("10.00"));

        Product p2 = new Product();
        p2.setSellingPrice(new BigDecimal("20.00"));

        CartItem item1 = new CartItem(testCart, p1, 2);
        CartItem item2 = new CartItem(testCart, p2, 1);

        List<CartItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        testCart.setCartItems(items);

        BigDecimal total = cartService.calculateCartTotal(testCart);
        assertEquals(0, new BigDecimal("40.00").compareTo(total));
    }

    @Test
    void testCalculateCartTotal_Empty() {
        testCart.setCartItems(new ArrayList<>());
        BigDecimal total = cartService.calculateCartTotal(testCart);
        assertEquals(BigDecimal.ZERO, total);
    }
}
