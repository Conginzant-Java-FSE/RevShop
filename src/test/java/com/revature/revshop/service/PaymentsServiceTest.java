package com.revature.revshop.service;

import com.revature.revshop.exception.OrderNotFoundException;
import com.revature.revshop.exception.PaymentFailedException;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.Payments;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.OrdersRepository;
import com.revature.revshop.repository.PaymentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentsServiceTest {

    @Mock
    private PaymentsRepository paymentsRepository;

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentsService paymentsService;

    private Orders sampleOrder;
    private Payments samplePayment;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setName("John Doe");

        sampleOrder = new Orders();
        sampleOrder.setOrderId(100L);
        sampleOrder.setUser(sampleUser);
        sampleOrder.setOrderNumber("ORD-100");
        sampleOrder.setTotalAmount(new BigDecimal("250.00"));

        samplePayment = new Payments();
        samplePayment.setPaymentId(1);
        samplePayment.setOrder(sampleOrder);
        samplePayment.setAmount(new BigDecimal("250.00"));
        samplePayment.setPaymentMethod(Payments.PaymentMethod.CREDIT_CARD);
        samplePayment.setPaymentStatus(Payments.PaymentStatus.PENDING);
        samplePayment.setTransactionId("TXN-001");
        samplePayment.setPaymentDate(LocalDateTime.now());
    }

    @Test
    void testCreatePayment_Success() {
        when(ordersRepository.findById(100L)).thenReturn(Optional.of(sampleOrder));
        when(paymentsRepository.save(any(Payments.class))).thenReturn(samplePayment);

        Payments newPayment = new Payments();
        newPayment.setAmount(new BigDecimal("250.00"));
        newPayment.setPaymentMethod(Payments.PaymentMethod.CREDIT_CARD);
        newPayment.setPaymentStatus(Payments.PaymentStatus.PENDING);
        newPayment.setPaymentDate(LocalDateTime.now());

        Payments result = paymentsService.createPayment(newPayment, 100L);

        assertNotNull(result);
        assertEquals(sampleOrder, newPayment.getOrder());
        verify(ordersRepository).findById(100L);
        verify(paymentsRepository).save(newPayment);
    }

    @Test
    void testCreatePayment_OrderNotFound() {
        when(ordersRepository.findById(999L)).thenReturn(Optional.empty());

        Payments newPayment = new Payments();
        newPayment.setAmount(new BigDecimal("100.00"));

        assertThrows(OrderNotFoundException.class,
                () -> paymentsService.createPayment(newPayment, 999L));
    }

    @Test
    void testCreatePayment_DefaultsPendingStatus() {
        when(ordersRepository.findById(100L)).thenReturn(Optional.of(sampleOrder));
        when(paymentsRepository.save(any(Payments.class))).thenAnswer(inv -> inv.getArgument(0));

        Payments newPayment = new Payments();
        newPayment.setAmount(new BigDecimal("250.00"));
        newPayment.setPaymentMethod(Payments.PaymentMethod.UPI);
        newPayment.setPaymentDate(LocalDateTime.now());

        Payments result = paymentsService.createPayment(newPayment, 100L);

        assertEquals(Payments.PaymentStatus.PENDING, result.getPaymentStatus());
    }

    @Test
    void testCreatePayment_DefaultsPaymentDate() {
        when(ordersRepository.findById(100L)).thenReturn(Optional.of(sampleOrder));
        when(paymentsRepository.save(any(Payments.class))).thenAnswer(inv -> inv.getArgument(0));

        Payments newPayment = new Payments();
        newPayment.setAmount(new BigDecimal("250.00"));
        newPayment.setPaymentMethod(Payments.PaymentMethod.DEBIT_CARD);

        Payments result = paymentsService.createPayment(newPayment, 100L);

        assertNotNull(result.getPaymentDate());
    }

    @Test
    void testUpdatePaymentStatus_Success() {
        when(paymentsRepository.findById(1)).thenReturn(Optional.of(samplePayment));
        when(paymentsRepository.save(any(Payments.class))).thenReturn(samplePayment);
        doNothing().when(notificationService).createNotification(anyLong(), anyString(), anyString());

        Payments result = paymentsService.updatePaymentStatus(1, Payments.PaymentStatus.SUCCESS);

        assertEquals(Payments.PaymentStatus.SUCCESS, samplePayment.getPaymentStatus());
        verify(notificationService).createNotification(
                eq(sampleUser.getUserId()),
                eq("Payment Update"),
                contains("SUCCESS"));
    }

    @Test
    void testUpdatePaymentStatus_ToFailed() {
        when(paymentsRepository.findById(1)).thenReturn(Optional.of(samplePayment));
        when(paymentsRepository.save(any(Payments.class))).thenReturn(samplePayment);
        doNothing().when(notificationService).createNotification(anyLong(), anyString(), anyString());

        Payments result = paymentsService.updatePaymentStatus(1, Payments.PaymentStatus.FAILED);

        assertEquals(Payments.PaymentStatus.FAILED, samplePayment.getPaymentStatus());
        verify(notificationService).createNotification(
                eq(sampleUser.getUserId()),
                eq("Payment Update"),
                contains("FAILED"));
    }

    @Test
    void testUpdatePaymentStatus_ToRefunded() {
        when(paymentsRepository.findById(1)).thenReturn(Optional.of(samplePayment));
        when(paymentsRepository.save(any(Payments.class))).thenReturn(samplePayment);
        doNothing().when(notificationService).createNotification(anyLong(), anyString(), anyString());

        Payments result = paymentsService.updatePaymentStatus(1, Payments.PaymentStatus.REFUNDED);

        assertEquals(Payments.PaymentStatus.REFUNDED, samplePayment.getPaymentStatus());
        verify(notificationService).createNotification(
                eq(sampleUser.getUserId()),
                eq("Payment Update"),
                contains("REFUNDED"));
    }

    @Test
    void testUpdatePaymentStatus_PaymentNotFound() {
        when(paymentsRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(PaymentFailedException.class,
                () -> paymentsService.updatePaymentStatus(999, Payments.PaymentStatus.SUCCESS));
    }

    @Test
    void testGetPaymentById_Success() {
        when(paymentsRepository.findById(1)).thenReturn(Optional.of(samplePayment));

        Optional<Payments> result = paymentsService.getPaymentById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getPaymentId());
        assertEquals("TXN-001", result.get().getTransactionId());
    }

    @Test
    void testGetPaymentById_NotFound() {
        when(paymentsRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Payments> result = paymentsService.getPaymentById(999);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetPaymentByOrderId_Success() {
        when(paymentsRepository.findByOrder_OrderId(100L)).thenReturn(Optional.of(samplePayment));

        Optional<Payments> result = paymentsService.getPaymentByOrderId(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getOrder().getOrderId());
    }

    @Test
    void testGetAllPayments() {
        Payments secondPayment = new Payments();
        secondPayment.setPaymentId(2);
        secondPayment.setAmount(new BigDecimal("500.00"));

        when(paymentsRepository.findAll()).thenReturn(Arrays.asList(samplePayment, secondPayment));

        List<Payments> result = paymentsService.getAllPayments();

        assertEquals(2, result.size());
    }

    @Test
    void testDeletePayment_Success() {
        when(paymentsRepository.existsById(1)).thenReturn(true);
        doNothing().when(paymentsRepository).deleteById(1);

        assertDoesNotThrow(() -> paymentsService.deletePayment(1));
        verify(paymentsRepository).deleteById(1);
    }

    @Test
    void testDeletePayment_NotFound() {
        when(paymentsRepository.existsById(999)).thenReturn(false);

        assertThrows(PaymentFailedException.class,
                () -> paymentsService.deletePayment(999));
    }
}
