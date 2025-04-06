package com.projectmanagement.userservice.dto;

import com.projectmanagement.userservice.entity.IssuePriority;
import com.projectmanagement.userservice.entity.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IssueCreateDto {
    @NotBlank
    @Size(min = 3, max = 200)
    private String title;
    
    private String description;
    
    private IssueType type;
    
    private IssuePriority priority;
    
    @NotNull
    private Long workListId;
    
    private Long assigneeId;
    
    private Long parentIssueId;
    
    private LocalDateTime dueDate;
    
    private Long estimatedHours;
    
    private List<Long> labelIds;
}