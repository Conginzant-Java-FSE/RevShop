package com.revature.revshop.service;

import com.revature.revshop.model.Cart;
import com.revature.revshop.model.CartItem;
import com.revature.revshop.model.Product;
import com.revature.revshop.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartItemService cartItemService;

    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setCartId(1);

        product = new Product();
        product.setProductId(1L);
        product.setSellingPrice(new java.math.BigDecimal("10.0"));

        cartItem = new CartItem(cart, product, 2);
        cartItem.setCartItemId(1L);
    }

    @Test
    void testAddItemToCart_NewItem() {
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArguments()[0]);

        CartItem savedItem = cartItemService.addItemToCart(cart, product, 3);

        assertNotNull(savedItem);
        assertEquals(3, savedItem.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testAddItemToCart_ExistingItem() {
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        CartItem savedItem = cartItemService.addItemToCart(cart, product, 3);

        assertNotNull(savedItem);
        // Original was 2, added 3 -> 5
        assertEquals(5, savedItem.getQuantity());
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    void testUpdateItemQuantity_Exists() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        CartItem updatedItem = cartItemService.updateItemQuantity(1L, 10);

        assertNotNull(updatedItem);
        assertEquals(10, updatedItem.getQuantity());
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    void testUpdateItemQuantity_NotExists() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());

        CartItem updatedItem = cartItemService.updateItemQuantity(1L, 10);

        assertNull(updatedItem);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void testRemoveItemFromCart() {
        doNothing().when(cartItemRepository).deleteById(1L);

        cartItemService.removeItemFromCart(1L);

        verify(cartItemRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetCartItemsByCart() {
        when(cartItemRepository.findByCart(cart)).thenReturn(Arrays.asList(cartItem));

        List<CartItem> items = cartItemService.getCartItemsByCart(cart);

        assertFalse(items.isEmpty());
        assertEquals(1, items.size());
        assertEquals(cartItem.getCartItemId(), items.get(0).getCartItemId());
    }

    @Test
    void testClearCart() {
        doNothing().when(cartItemRepository).deleteByCart(cart);

        cartItemService.clearCart(cart);

        verify(cartItemRepository, times(1)).deleteByCart(cart);
    }

    @Test
    void testGetCartItemByCartAndProduct_Exists() {
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));

        Optional<CartItem> result = cartItemService.getCartItemByCartAndProduct(cart, product);

        assertTrue(result.isPresent());
        assertEquals(cartItem.getCartItemId(), result.get().getCartItemId());
    }

    @Test
    void testGetCartItemByCartAndProduct_NotExists() {
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        Optional<CartItem> result = cartItemService.getCartItemByCartAndProduct(cart, product);

        assertFalse(result.isPresent());
    }
}
