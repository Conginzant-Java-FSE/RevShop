package com.revature.revshop;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.revature.revshop.controller.CartController;
import com.revature.revshop.dto.CartItemDTO;
import com.revature.revshop.model.Cart;
import com.revature.revshop.model.CartItem;
import com.revature.revshop.model.Product;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.ProductRepository;
import com.revature.revshop.repository.UserRepository;
import com.revature.revshop.service.CartItemService;
import com.revature.revshop.service.CartService;

public class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    public void testGetCart() throws Exception {
        User user = new User();
        user.setUserId(1L);

        Cart cart = new Cart();
        cart.setCartId(1);
        cart.setUser(user);
        cart.setCartItems(Collections.emptyList());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartService.getCartByUser(user)).thenReturn(Optional.of(cart));

        mockMvc.perform(get("/api/carts/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    public void testClearCart() throws Exception {
        User user = new User();
        user.setUserId(1L);

        Cart cart = new Cart();
        cart.setCartId(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartService.getCartByUser(user)).thenReturn(Optional.of(cart));
        doNothing().when(cartItemService).clearCart(cart);

        mockMvc.perform(delete("/api/carts/user/1/clear"))
                .andExpect(status().isOk());

        verify(cartItemService, times(1)).clearCart(cart);
    }
}
