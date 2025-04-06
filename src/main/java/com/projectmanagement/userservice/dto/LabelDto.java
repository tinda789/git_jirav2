package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LabelDto {
    private Long id;
    
    @NotBlank
    @Size(min = 1, max = 30)
    private String name;
    
    private String color;
}