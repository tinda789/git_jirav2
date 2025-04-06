package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileDto {
    @Size(min = 3, max = 100)
    private String fullName;
    
    private String avatarUrl;
}