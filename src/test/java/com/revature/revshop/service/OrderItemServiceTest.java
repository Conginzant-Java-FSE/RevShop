package com.revature.revshop.service;

import com.revature.revshop.model.OrderItems;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.Product;
import com.revature.revshop.repository.OrderItemsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderItemServiceTest {

    @Mock
    private OrderItemsRepository orderItemsRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrderItem_ShouldLockPriceAtPurchase() {
        // Arrange
        Orders order = new Orders();
        Product product = new Product();
        product.setSellingPrice(new BigDecimal("99.99"));
        Integer quantity = 2;

        OrderItems savedItem = new OrderItems();
        savedItem.setPriceAtPurchase(product.getSellingPrice());
        savedItem.setQuantity(quantity);

        when(orderItemsRepository.save(any(OrderItems.class))).thenReturn(savedItem);

        // Act
        OrderItems result = orderItemService.createOrderItem(order, product, quantity);

        // Assert
        assertEquals(new BigDecimal("99.99"), result.getPriceAtPurchase(),
                "Price should be locked at the current selling price");
        verify(orderItemsRepository, times(1)).save(any(OrderItems.class));
    }

    @Test
    void calculateSubtotal_ShouldReturnCorrectValue() {
        // Arrange
        OrderItems item = new OrderItems();
        item.setPriceAtPurchase(new BigDecimal("50.00"));
        item.setQuantity(3);

        // Act
        BigDecimal subtotal = orderItemService.calculateSubtotal(item);

        // Assert
        assertEquals(new BigDecimal("150.00"), subtotal);
    }

    @Test
    void calculateSubtotal_WithNullValues_ShouldReturnZero() {
        // Arrange
        OrderItems item = new OrderItems();

        // Act & Assert
        assertEquals(BigDecimal.ZERO, orderItemService.calculateSubtotal(item));
    }
}
