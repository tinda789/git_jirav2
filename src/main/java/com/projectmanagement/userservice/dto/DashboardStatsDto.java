package com.projectmanagement.userservice.dto;

import com.projectmanagement.userservice.entity.IssueStatus;
import lombok.Data;

import java.util.Map;

@Data
public class DashboardStatsDto {
    private int totalWorkLists;
    
    private int totalWorkspaces;
    
    private int totalAssignedIssues;
    
    private int overdueIssues;
    
    private Map<IssueStatus, Long> issuesByStatus;
}