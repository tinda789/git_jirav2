package com.projectmanagement.userservice.dto;

import com.projectmanagement.userservice.entity.CustomFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomFieldDto {
    private Long id;
    
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
    
    private CustomFieldType type;
    
    private Boolean isPremiumOnly;
}