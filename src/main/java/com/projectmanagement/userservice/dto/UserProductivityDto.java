package com.projectmanagement.userservice.dto;

import lombok.Data;

@Data
public class UserProductivityDto {
    private Long userId;
    
    private String username;
    
    private String fullName;
    
    private int totalAssignedIssues;
    
    private int completedIssues;
    
    private double completionRate;
}