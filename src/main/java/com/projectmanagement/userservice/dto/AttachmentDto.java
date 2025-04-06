package com.projectmanagement.userservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentDto {
    private Long id;
    
    private String fileName;
    
    private String fileUrl;
    
    private Long fileSize;
    
    private LocalDateTime uploadDate;
    
    private Long issueId;
    
    private Long uploaderId;
}