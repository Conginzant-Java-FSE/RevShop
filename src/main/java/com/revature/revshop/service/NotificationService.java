package com.revature.revshop.service;

import com.revature.revshop.exception.ResourceNotFoundException;
import com.revature.revshop.exception.UserNotFoundException;
import com.revature.revshop.model.Notification;
import com.revature.revshop.model.User;
import com.revature.revshop.repository.NotificationRepository;
import com.revature.revshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void createNotification(Long userId, String title, String message) {
        log.info("Creating notification for userId={} title={}", userId, title);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByRecipient_UserId(userId);
    }

    public void markAsRead(Long notificationId) {
        log.info("Marking notification id={} as read", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
