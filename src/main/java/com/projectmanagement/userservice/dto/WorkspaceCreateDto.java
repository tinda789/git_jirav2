package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceCreateDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;
    
    private String description;
}