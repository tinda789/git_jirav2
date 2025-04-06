package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkListDto {
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
    
    @NotBlank
    @Size(min = 2, max = 10)
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Code must contain only uppercase letters and numbers")
    private String code;
    
    private String description;
    
    private LocalDateTime createdDate;
    
    private Long workspaceId;
    
    private Long leadId;
    
    private List<Long> memberIds;
}