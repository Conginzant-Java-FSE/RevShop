package com.revature.revshop;

import java.util.HashMap;
import java.util.Map;

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

import com.revature.revshop.controller.CartItemController;
import com.revature.revshop.model.CartItem;
import com.revature.revshop.service.CartItemService;

public class CartItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartItemService cartItemService;

    @InjectMocks
    private CartItemController cartItemController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cartItemController).build();
    }

    @Test
    public void testUpdateItemQuantity() throws Exception {
        CartItem item = new CartItem();
        item.setCartItemId(1L);
        item.setQuantity(5);

        when(cartItemService.updateItemQuantity(1L, 5)).thenReturn(item);

        String jsonRequest = "{\"quantity\": 5}";

        mockMvc.perform(put("/api/cart-items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    public void testUpdateItemQuantity_MissingBody() throws Exception {
        String jsonRequest = "{}"; // Empty map, missing 'quantity'

        mockMvc.perform(put("/api/cart-items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRemoveItemFromCart() throws Exception {
        doNothing().when(cartItemService).removeItemFromCart(1L);

        mockMvc.perform(delete("/api/cart-items/1"))
                .andExpect(status().isOk());

        verify(cartItemService, times(1)).removeItemFromCart(1L);
    }
}
