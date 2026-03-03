package com.revature.revshop.service;

import com.revature.revshop.exception.OrderNotFoundException;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Orders;
import com.revature.revshop.model.TrackingDetails;
import com.revature.revshop.repository.OrdersRepository;
import com.revature.revshop.repository.TrackingDetailsRepository;
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
class TrackingDetailsServiceTest {

    @Mock
    private TrackingDetailsRepository trackingDetailsRepository;

    @Mock
    private OrdersRepository ordersRepository;

    @InjectMocks
    private TrackingDetailsService trackingDetailsService;

    private Orders sampleOrder;
    private TrackingDetails sampleTracking;

    @BeforeEach
    void setUp() {
        sampleOrder = new Orders();
        sampleOrder.setOrderId(100L);
        sampleOrder.setOrderNumber("ORD-100");

        sampleTracking = new TrackingDetails();
        sampleTracking.setTrackingId(1);
        sampleTracking.setOrder(sampleOrder);
        sampleTracking.setStatus("SHIPPED");
        sampleTracking.setDescription("Package has been shipped");
    }

    @Test
    void testAddTrackingDetail_Success() {
        when(ordersRepository.findById(100L)).thenReturn(Optional.of(sampleOrder));
        when(trackingDetailsRepository.save(any(TrackingDetails.class))).thenReturn(sampleTracking);

        TrackingDetails newTracking = new TrackingDetails();
        newTracking.setStatus("SHIPPED");
        newTracking.setDescription("Package has been shipped");

        TrackingDetails result = trackingDetailsService.addTrackingDetail(newTracking, 100L);

        assertNotNull(result);
        assertEquals(sampleOrder, newTracking.getOrder());
        verify(ordersRepository).findById(100L);
        verify(trackingDetailsRepository).save(newTracking);
    }

    @Test
    void testAddTrackingDetail_OrderNotFound() {
        when(ordersRepository.findById(999L)).thenReturn(Optional.empty());

        TrackingDetails newTracking = new TrackingDetails();
        newTracking.setStatus("SHIPPED");

        assertThrows(OrderNotFoundException.class,
                () -> trackingDetailsService.addTrackingDetail(newTracking, 999L));
    }

    @Test
    void testGetTrackingById_Success() {
        when(trackingDetailsRepository.findById(1)).thenReturn(Optional.of(sampleTracking));

        Optional<TrackingDetails> result = trackingDetailsService.getTrackingById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getTrackingId());
        assertEquals("SHIPPED", result.get().getStatus());
    }

    @Test
    void testGetTrackingById_NotFound() {
        when(trackingDetailsRepository.findById(999)).thenReturn(Optional.empty());

        Optional<TrackingDetails> result = trackingDetailsService.getTrackingById(999);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetTrackingByOrderId_Success() {
        when(trackingDetailsRepository.findByOrder_OrderId(100L))
                .thenReturn(Arrays.asList(sampleTracking));

        List<TrackingDetails> result = trackingDetailsService.getTrackingByOrderId(100L);

        assertEquals(1, result.size());
        assertEquals("SHIPPED", result.get(0).getStatus());
    }

    @Test
    void testGetAllTrackingDetails() {
        TrackingDetails secondTracking = new TrackingDetails();
        secondTracking.setTrackingId(2);
        secondTracking.setStatus("DELIVERED");

        when(trackingDetailsRepository.findAll())
                .thenReturn(Arrays.asList(sampleTracking, secondTracking));

        List<TrackingDetails> result = trackingDetailsService.getAllTrackingDetails();

        assertEquals(2, result.size());
    }

    @Test
    void testUpdateTrackingStatus_Success() {
        when(trackingDetailsRepository.findById(1)).thenReturn(Optional.of(sampleTracking));
        when(trackingDetailsRepository.save(any(TrackingDetails.class))).thenReturn(sampleTracking);

        trackingDetailsService.updateTrackingStatus(1, "DELIVERED", "Package delivered");

        assertEquals("DELIVERED", sampleTracking.getStatus());
        assertEquals("Package delivered", sampleTracking.getDescription());
        verify(trackingDetailsRepository).save(sampleTracking);
    }

    @Test
    void testUpdateTrackingStatus_NotFound() {
        when(trackingDetailsRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> trackingDetailsService.updateTrackingStatus(999, "DELIVERED", "Delivered"));
    }

    @Test
    void testDeleteTracking_Success() {
        when(trackingDetailsRepository.existsById(1)).thenReturn(true);
        doNothing().when(trackingDetailsRepository).deleteById(1);

        assertDoesNotThrow(() -> trackingDetailsService.deleteTracking(1));
        verify(trackingDetailsRepository).deleteById(1);
    }

    @Test
    void testDeleteTracking_NotFound() {
        when(trackingDetailsRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> trackingDetailsService.deleteTracking(999));
    }
}
