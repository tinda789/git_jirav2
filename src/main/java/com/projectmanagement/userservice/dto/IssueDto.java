package com.projectmanagement.userservice.dto;

import com.projectmanagement.userservice.entity.IssuePriority;
import com.projectmanagement.userservice.entity.IssueStatus;
import com.projectmanagement.userservice.entity.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IssueDto {
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 200)
    private String title;
    
    private String description;
    
    private IssueType type;
    
    private IssuePriority priority;
    
    private IssueStatus status;
    
    private LocalDateTime createdDate;
    
    private LocalDateTime dueDate;
    
    private Long estimatedHours;
    
    private Integer storyPoints;
    
    private Long workListId;
    
    private Long reporterId;
    
    private Long assigneeId;
    
    private Long parentIssueId;
    
    private Long sprintId;
    
    private List<Long> labelIds;
}