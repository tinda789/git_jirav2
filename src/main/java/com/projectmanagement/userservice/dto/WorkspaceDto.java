package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class WorkspaceDto {
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;
    
    private String description;
    
    private Long ownerId;
    
    private List<Long> memberIds;
}