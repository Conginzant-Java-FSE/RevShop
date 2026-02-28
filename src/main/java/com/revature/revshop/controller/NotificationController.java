package com.revature.revshop.controller;

import com.revature.revshop.dto.ApiResponse;
import com.revature.revshop.dto.NotificationDTO;
import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.model.Notification;
import com.revature.revshop.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotifications(
            @PathVariable Long userId) {

        List<Notification> notifications =
                notificationService.getNotificationsByUserId(userId);

        List<NotificationDTO> list = notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>("Notifications fetched successfully", list)
        );
    }


    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id) {

        notificationService.markAsRead(id);

        return ResponseEntity.ok(
                new ApiResponse<>("Notification marked as read", null)
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id) {

        notificationService.deleteNotification(id);

        return ResponseEntity.ok(
                new ApiResponse<>("Notification deleted successfully", null)
        );
    }


    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(notification.getNotificationId());
        dto.setUserId(notification.getRecipient().getUserId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}