package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleDto {
    @NotNull
    private Long userId;
    
    @NotNull
    private Long roleId;
}