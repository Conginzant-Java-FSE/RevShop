package com.revature.revshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.revshop.dto.*;
import com.revature.revshop.service.OrdersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrdersControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrdersService ordersService;

    @InjectMocks
    private OrdersController ordersController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private OrderRequestDTO validRequest;
    private OrderResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ordersController).build();

        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductId(1L);
        item.setQuantity(2);

        validRequest = new OrderRequestDTO();
        validRequest.setUserId(1L);
        validRequest.setShippingAddressId(1L);
        validRequest.setBillingAddressId(1L);
        validRequest.setPaymentMethod("COD");
        validRequest.setItems(Collections.singletonList(item));

        mockResponse = new OrderResponseDTO();
        mockResponse.setOrderId(101L);
        mockResponse.setOrderNumber("ORD-123");
        mockResponse.setTotalAmount(new BigDecimal("200.00"));
        mockResponse.setStatus("PENDING");
        mockResponse.setOrderDate(LocalDateTime.now());
        mockResponse.setPaymentMethod("COD");
        mockResponse.setBuyerName("Test Buyer");
        mockResponse.setBuyerEmail("test@buyer.com");
        mockResponse.setItems(new ArrayList<>());
    }

    @Test
    void placeOrder_ValidRequest_ShouldReturnCreated() throws Exception {
        when(ordersService.placeOrder(eq(1L), any(OrderRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/orders/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Order placed successfully"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-123"));
    }

    @Test
    void getUserOrders_ShouldReturnOk() throws Exception {
        when(ordersService.getOrdersByUser(1L)).thenReturn(Collections.singletonList(mockResponse));

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].orderNumber").value("ORD-123"));
    }

    @Test
    void updateOrderStatus_ShouldReturnOk() throws Exception {
        when(ordersService.updateOrderStatus(eq(101L), eq("SHIPPED"), eq(1L))).thenReturn(mockResponse);

        String jsonBody = "{\"status\": \"SHIPPED\", \"sellerId\": \"1\"}";

        mockMvc.perform(put("/api/orders/101/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order status updated"));
    }
}
