package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.Notification;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    public Notification sendNotification(User user, String message, Issue issue) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setIssue(issue);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        if (issue != null) {
            notification.setLink("/issues/" + issue.getId());
        }
        
        return notificationRepository.save(notification);
    }
    
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Notification> getUserUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }
    
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
    
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(user);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
    
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}