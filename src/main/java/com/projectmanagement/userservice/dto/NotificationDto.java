package com.projectmanagement.userservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    
    private String message;
    
    private String link;
    
    private Boolean isRead;
    
    private LocalDateTime createdAt;
    
    private Long issueId;
}