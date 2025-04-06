package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.dto.DashboardStatsDto;
import com.projectmanagement.userservice.dto.UserProductivityDto;
import com.projectmanagement.userservice.dto.WorkListReportDto;
import com.projectmanagement.userservice.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final WorkspaceService workspaceService;
    private final WorkListService workListService;
    private final IssueService issueService;
    private final UserService userService;

    @Autowired
    public DashboardService(
            WorkspaceService workspaceService,
            WorkListService workListService,
            IssueService issueService,
            UserService userService) {
        this.workspaceService = workspaceService;
        this.workListService = workListService;
        this.issueService = issueService;
        this.userService = userService;
    }

    public DashboardStatsDto getDashboardStats(User currentUser) {
        // Lấy các worklist mà người dùng tham gia
        List<WorkList> userWorkLists = workListService.getWorkListsForUser(currentUser);
        
        // Tổng số issue được giao
        List<Issue> assignedIssues = issueService.getIssuesByAssignee(currentUser);
        
        // Số lượng issue theo trạng thái
        Map<IssueStatus, Long> issuesByStatus = countIssuesByStatus(assignedIssues);
        
        // Các issue quá hạn
        List<Issue> overdueIssues = assignedIssues.stream()
                .filter(issue -> issue.getDueDate() != null && issue.getDueDate().isBefore(LocalDateTime.now()))
                .filter(issue -> issue.getStatus() != IssueStatus.DONE)
                .toList();
        
        // Tạo đối tượng thống kê
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalWorkLists(userWorkLists.size());
        stats.setTotalWorkspaces(workspaceService.getWorkspacesForMember(currentUser).size());
        stats.setTotalAssignedIssues(assignedIssues.size());
        stats.setOverdueIssues(overdueIssues.size());
        stats.setIssuesByStatus(issuesByStatus);
        
        return stats;
    }
    
    public WorkListReportDto getWorkListReport(WorkList workList) {
        List<Issue> workListIssues = issueService.getIssuesByWorkList(workList);
        
        // Tổng số issue
        int totalIssues = workListIssues.size();
        
        // Số lượng issue theo trạng thái
        Map<IssueStatus, Long> issuesByStatus = workListIssues.stream()
                .collect(Collectors.groupingBy(Issue::getStatus, Collectors.counting()));
        
        // Issue quá hạn
        long overdueIssues = workListIssues.stream()
                .filter(issue -> issue.getDueDate() != null && issue.getDueDate().isBefore(LocalDateTime.now()))
                .filter(issue -> issue.getStatus() != IssueStatus.DONE)
                .count();
        
        // Tỷ lệ hoàn thành
        long completedIssues = issuesByStatus.getOrDefault(IssueStatus.DONE, 0L);
        double completionPercentage = totalIssues > 0 ? (double) completedIssues / totalIssues * 100 : 0;
        
        // Tạo đối tượng báo cáo
        WorkListReportDto report = new WorkListReportDto();
        report.setWorkListId(workList.getId());
        report.setWorkListName(workList.getName());
        report.setTotalIssues(totalIssues);
        report.setIssuesByStatus(issuesByStatus);
        report.setOverdueIssues((int) overdueIssues);
        report.setCompletionPercentage(completionPercentage);
        
        return report;
    }
    
    public List<UserProductivityDto> getUserProductivity(WorkList workList) {
        List<User> users = workList.getMembers();
        List<Issue> allIssues = issueService.getIssuesByWorkList(workList);
        
        List<UserProductivityDto> productivity = new ArrayList<>();
        
        for (User user : users) {
            // Lấy issues được giao cho người dùng này
            List<Issue> userIssues = allIssues.stream()
                    .filter(issue -> issue.getAssignee() != null && issue.getAssignee().getId().equals(user.getId()))
                    .toList();
            
            // Đếm số lượng issue đã hoàn thành
            long completedIssues = userIssues.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.DONE)
                    .count();
            
            // Tổng số issue
            int totalIssues = userIssues.size();
            
            // Tạo đối tượng năng suất
            UserProductivityDto userProductivity = new UserProductivityDto();
            userProductivity.setUserId(user.getId());
            userProductivity.setUsername(user.getUsername());
            userProductivity.setFullName(user.getFullName());
            userProductivity.setTotalAssignedIssues(totalIssues);
            userProductivity.setCompletedIssues((int) completedIssues);
            userProductivity.setCompletionRate(totalIssues > 0 ? (double) completedIssues / totalIssues : 0);
            
            productivity.add(userProductivity);
        }
        
        return productivity;
    }
    
    private Map<IssueStatus, Long> countIssuesByStatus(List<Issue> issues) {
        Map<IssueStatus, Long> result = new HashMap<>();
        
        for (IssueStatus status : IssueStatus.values()) {
            long count = issues.stream()
                    .filter(issue -> issue.getStatus() == status)
                    .count();
            result.put(status, count);
        }
        
        return result;
    }
}