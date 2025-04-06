package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.NotificationDto;
import com.projectmanagement.userservice.entity.Notification;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public ResponseEntity<?> getUserNotifications() {
        User currentUser = authService.getCurrentUser();
        List<Notification> notifications = notificationService.getUserNotifications(currentUser);
        List<NotificationDto> notificationDtos = notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notificationDtos);
    }
    
    @GetMapping("/unread")
    public ResponseEntity<?> getUserUnreadNotifications() {
        User currentUser = authService.getCurrentUser();
        List<Notification> notifications = notificationService.getUserUnreadNotifications(currentUser);
        List<NotificationDto> notificationDtos = notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notificationDtos);
    }
    
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }
    
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        User currentUser = authService.getCurrentUser();
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(new MessageResponse("Notification deleted successfully"));
    }
    
    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setLink(notification.getLink());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        
        if (notification.getIssue() != null) {
            dto.setIssueId(notification.getIssue().getId());
        }
        
        return dto;
    }
}