package com.projectmanagement.userservice.dto;

import com.projectmanagement.userservice.entity.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BoardDto {
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;
    
    private BoardType type;
    
    @NotNull
    private Long workListId;
}