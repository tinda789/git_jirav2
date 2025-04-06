package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkLogDto {
    private Long id;
    
    @NotNull
    @Positive
    private Long timeSpentSeconds;
    
    private String description;
    
    private LocalDateTime startTime;
    
    private LocalDateTime createdAt;
    
    @NotNull
    private Long issueId;
    
    private Long userId;
}