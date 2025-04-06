package com.projectmanagement.userservice.dto;

import com.projectmanagement.userservice.entity.IssueStatus;
import lombok.Data;

import java.util.Map;

@Data
public class WorkListReportDto {
    private Long workListId;
    
    private String workListName;
    
    private int totalIssues;
    
    private Map<IssueStatus, Long> issuesByStatus;
    
    private int overdueIssues;
    
    private double completionPercentage;
}