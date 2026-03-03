package com.revature.revshop.service;

import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Notification;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.NotificationRepository;
import com.revature.revshop.repository.UserRepository;
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
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User sampleUser;
    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1L);

        sampleNotification = new Notification();
        sampleNotification.setNotificationId(1L);
        sampleNotification.setRecipient(sampleUser);
        sampleNotification.setTitle("Test Title");
        sampleNotification.setMessage("Test Message");
        sampleNotification.setIsRead(false);
    }

    @Test
    void testCreateNotification_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        assertDoesNotThrow(() -> notificationService.createNotification(1L, "New Title", "New Message"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testCreateNotification_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> notificationService.createNotification(1L, "Title", "Message"));
    }

    @Test
    void testGetNotificationsByUserId_Success() {
        when(notificationRepository.findByRecipient_UserId(1L)).thenReturn(Arrays.asList(sampleNotification));

        List<Notification> notifications = notificationService.getNotificationsByUserId(1L);

        assertEquals(1, notifications.size());
        assertEquals("Test Title", notifications.get(0).getTitle());
    }

    @Test
    void testMarkAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        notificationService.markAsRead(1L);

        assertTrue(sampleNotification.getIsRead());
        verify(notificationRepository).save(sampleNotification);
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(1L));
    }

    @Test
    void testDeleteNotification_Success() {
        doNothing().when(notificationRepository).deleteById(1L);

        assertDoesNotThrow(() -> notificationService.deleteNotification(1L));
        verify(notificationRepository).deleteById(1L);
    }
}
