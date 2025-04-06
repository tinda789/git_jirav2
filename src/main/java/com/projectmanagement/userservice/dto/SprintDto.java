package com.projectmanagement.userservice.dto;

import com.projectmanagement.userservice.entity.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SprintDto {
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
    
    private String goal;
    
    private SprintStatus status;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @NotNull
    private Long workListId;
    
    private List<Long> issueIds;
}