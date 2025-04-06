package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.dto.IssueCreateDto;
import com.projectmanagement.userservice.entity.*;
import com.projectmanagement.userservice.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IssueService {
    
    private final IssueRepository issueRepository;
    private final UserService userService;
    
    @Autowired
    public IssueService(IssueRepository issueRepository, UserService userService) {
        this.issueRepository = issueRepository;
        this.userService = userService;
    }
    
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }
    
    public Optional<Issue> getIssueById(Long id) {
        return issueRepository.findById(id);
    }
    
    public List<Issue> getIssuesByWorkList(WorkList workList) {
        return issueRepository.findByWorkList(workList);
    }
    
    public List<Issue> getIssuesByAssignee(User assignee) {
        return issueRepository.findByAssignee(assignee);
    }
    
    public List<Issue> getIssuesByReporter(User reporter) {
        return issueRepository.findByReporter(reporter);
    }
    
    public List<Issue> getIssuesByStatus(IssueStatus status) {
        return issueRepository.findByStatus(status);
    }
    
    public List<Issue> getIssuesByWorkListAndStatus(WorkList workList, IssueStatus status) {
        return issueRepository.findByWorkListAndStatus(workList, status);
    }
    
    public List<Issue> getSubIssues(Issue parentIssue) {
        return issueRepository.findByParentIssue(parentIssue);
    }
    
    public Issue createNewIssue(IssueCreateDto issueDto, WorkList workList, User currentUser) {
        Issue issue = new Issue();
        issue.setTitle(issueDto.getTitle());
        issue.setDescription(issueDto.getDescription());
        issue.setType(issueDto.getType());
        issue.setPriority(issueDto.getPriority());
        issue.setWorkList(workList);
        issue.setReporter(currentUser);
        
        // Set default status
        if (issue.getStatus() == null) {
            issue.setStatus(IssueStatus.TODO);
        }
        
        issue.setCreatedDate(LocalDateTime.now());
        issue.setDueDate(issueDto.getDueDate());
        issue.setEstimatedHours(issueDto.getEstimatedHours());
        
        // Set assignee
        if (issueDto.getAssigneeId() != null) {
            userService.getUserById(issueDto.getAssigneeId())
                    .ifPresent(issue::setAssignee);
        }
        
        // Set parent issue
        if (issueDto.getParentIssueId() != null) {
            getIssueById(issueDto.getParentIssueId())
                    .ifPresent(issue::setParentIssue);
        }
        
        return createIssue(issue);
    }
    
    public Issue updateExistingIssue(Issue existingIssue, IssueCreateDto issueDto) {
        existingIssue.setTitle(issueDto.getTitle());
        existingIssue.setDescription(issueDto.getDescription());
        existingIssue.setType(issueDto.getType());
        existingIssue.setPriority(issueDto.getPriority());
        
        // Update assignee
        if (issueDto.getAssigneeId() != null) {
            userService.getUserById(issueDto.getAssigneeId())
                    .ifPresent(existingIssue::setAssignee);
        }
        
        // Update parent issue
        if (issueDto.getParentIssueId() != null) {
            getIssueById(issueDto.getParentIssueId())
                    .ifPresent(existingIssue::setParentIssue);
        }
        
        existingIssue.setDueDate(issueDto.getDueDate());
        existingIssue.setEstimatedHours(issueDto.getEstimatedHours());
        
        return updateIssue(existingIssue);
    }
    
    public Issue createIssue(Issue issue) {
        return issueRepository.save(issue);
    }
    
    public Issue updateIssue(Issue issue) {
        return issueRepository.save(issue);
    }
    
    public void deleteIssue(Long id) {
        issueRepository.deleteById(id);
    }
    
    public List<Issue> getOverdueIssues() {
        return issueRepository.findByDueDateBefore(LocalDateTime.now());
    }
    
    public boolean canModifyIssue(User user, Issue issue) {
        // Reporter hoặc assignee có thể sửa
        if (issue.getReporter() != null && issue.getReporter().getId().equals(user.getId())) {
            return true;
        }
        if (issue.getAssignee() != null && issue.getAssignee().getId().equals(user.getId())) {
            return true;
        }
        
        // WorkList lead có thể sửa
        WorkList workList = issue.getWorkList();
        if (workList.getLead() != null && workList.getLead().getId().equals(user.getId())) {
            return true;
        }
        
        // Admin luôn có thể sửa
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
    }
}