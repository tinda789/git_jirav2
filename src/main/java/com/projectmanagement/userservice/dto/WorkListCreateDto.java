package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkListCreateDto {
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
    
    @NotBlank
    @Size(min = 2, max = 10)
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Code must contain only uppercase letters and numbers")
    private String code;
    
    private String description;
    
    @NotNull
    private Long workspaceId;
    
    private Long leadId;
}