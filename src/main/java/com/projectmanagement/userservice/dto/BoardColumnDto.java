package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BoardColumnDto {
    private Long id;
    
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
    
    private Integer position;
    
    @NotNull
    private Long boardId;
    
    private List<Long> issueIds;
}