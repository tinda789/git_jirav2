package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    
    @NotBlank
    private String content;
    
    private LocalDateTime createdDate;
    
    @NotNull
    private Long issueId;
    
    private Long authorId;
}