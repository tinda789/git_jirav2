package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
    
    @NotBlank
    @Email
    private String email;
    
    @Size(min = 3, max = 100)
    private String fullName;
    
    private String avatarUrl;
}