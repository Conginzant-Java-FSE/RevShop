package com.revature.revshop.service;

import com.revature.revshop.dto.*;
import com.revature.revshop.exception.*;
import com.revature.revshop.model.*;
import com.revature.revshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrdersServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaymentsRepository paymentsRepository;
    @Mock
    private TrackingDetailsRepository trackingDetailsRepository;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    private EmailService emailService;
    @Mock
    private WalletService walletService;

    private OrdersService ordersService;

    private User testUser;
    private Address testAddress;
    private Product testProduct;
    private OrderRequestDTO orderRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ordersService = new OrdersService(
                ordersRepository, userRepository,
                addressRepository, productRepository, notificationService,
                paymentsRepository, trackingDetailsRepository, orderItemService,
                emailService, walletService);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setName("Test User");

        testAddress = new Address();
        testAddress.setAddressId(1L);
        testAddress.setUser(testUser);

        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setName("Test Product");
        testProduct.setSellingPrice(new BigDecimal("100.00"));
        testProduct.setStockQuantity(10);
        testProduct.setThresholdQuantity(5);

        OrderItemRequestDTO itemRequest = new OrderItemRequestDTO();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        orderRequest = new OrderRequestDTO();
        orderRequest.setUserId(1L);
        orderRequest.setShippingAddressId(1L);
        orderRequest.setBillingAddressId(1L);
        orderRequest.setPaymentMethod("CREDIT_CARD");
        orderRequest.setItems(Collections.singletonList(itemRequest));
    }

    @Test
    void placeOrder_Success_ShouldCreateOrderAndItems() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Orders placeholderOrder = new Orders();
        placeholderOrder.setOrderId(101L);
        placeholderOrder.setOrderNumber("ORD-123");
        placeholderOrder.setStatus(Orders.OrderStatus.PENDING);
        placeholderOrder.setUser(testUser);

        when(ordersRepository.save(any(Orders.class))).thenReturn(placeholderOrder);

        OrderItems mockItem = new OrderItems();
        when(orderItemService.createOrderItem(any(), any(), any())).thenReturn(mockItem);

        when(paymentsRepository.save(any(Payments.class))).thenReturn(new Payments());
        when(trackingDetailsRepository.save(any(TrackingDetails.class))).thenReturn(new TrackingDetails());

        // Act
        OrderResponseDTO response = ordersService.placeOrder(1L, orderRequest);

        // Assert
        assertNotNull(response);
        assertEquals("ORD-123", response.getOrderNumber());
        assertEquals("PENDING", response.getStatus());
        verify(productRepository).save(any(Product.class));
        verify(orderItemService).createOrderItem(any(), any(), any());
    }

    @Test
    void placeOrder_InsufficientStock_ShouldThrowException() {
        // Arrange
        testProduct.setStockQuantity(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> ordersService.placeOrder(1L, orderRequest));
    }

    @Test
    void updateOrderStatus_ValidTransition_ShouldUpdateAndNotify() {
        // Arrange
        Orders existingOrder = new Orders();
        existingOrder.setOrderId(101L);
        existingOrder.setStatus(Orders.OrderStatus.PENDING);
        existingOrder.setUser(testUser);

        when(ordersRepository.findById(101L)).thenReturn(Optional.of(existingOrder));
        when(ordersRepository.save(any(Orders.class))).thenReturn(existingOrder);
        when(trackingDetailsRepository.save(any(TrackingDetails.class))).thenReturn(new TrackingDetails());

        // Act
        OrderResponseDTO response = ordersService.updateOrderStatus(101L, "SHIPPED", 1L);

        // Assert
        assertEquals("SHIPPED", response.getStatus());
        verify(notificationService).createNotification(eq(1L), anyString(), contains("SHIPPED"), anyString(),
                anyString());
    }

    @Test
    void cancelOrder_BeforeShipping_ShouldSucceed() {
        // Arrange
        Orders existingOrder = new Orders();
        existingOrder.setOrderId(101L);
        existingOrder.setStatus(Orders.OrderStatus.PENDING);
        existingOrder.setUser(testUser);

        when(ordersRepository.findById(101L)).thenReturn(Optional.of(existingOrder));
        when(ordersRepository.save(any(Orders.class))).thenReturn(existingOrder);
        when(trackingDetailsRepository.save(any(TrackingDetails.class))).thenReturn(new TrackingDetails());

        // Act
        ordersService.cancelOrder(101L, 1L);

        // Assert
        assertEquals(Orders.OrderStatus.CANCELLED, existingOrder.getStatus());
        verify(ordersRepository).save(existingOrder);
    }
}
