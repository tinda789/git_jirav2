package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomFieldValueDto {
    private Long id;
    
    private String value;
    
    @NotNull
    private Long customFieldId;
    
    @NotNull
    private Long issueId;
}