package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.IssueCreateDto;
import com.projectmanagement.userservice.dto.IssueDto;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.entity.*;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/issues")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IssueController {

    @Autowired
    private IssueService issueService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<IssueDto>> getAllIssues() {
        User currentUser = authService.getCurrentUser();
        List<Issue> issues = issueService.getIssuesByAssignee(currentUser);
        List<IssueDto> issueDtos = issues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(issueDtos);
    }
    
    @GetMapping("/worklist/{workListId}")
    @PreAuthorize("@securityService.canManageWorkList(#workListId, principal)")
    public ResponseEntity<?> getIssuesByWorkList(@PathVariable Long workListId) {
        List<Issue> issues = issueService.getIssuesByWorkListId(workListId);
        if (issues == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<IssueDto> issueDtos = issues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(issueDtos);
    }
    
    @GetMapping("/sprint/{sprintId}")
    @PreAuthorize("@securityService.canManageSprint(#sprintId, principal)")
    public ResponseEntity<?> getIssuesBySprint(@PathVariable Long sprintId) {
        List<Issue> issues = issueService.getIssuesBySprintId(sprintId);
        if (issues == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<IssueDto> issueDtos = issues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(issueDtos);
    }
    
    @GetMapping("/assigned")
    public ResponseEntity<List<IssueDto>> getAssignedIssues() {
        User currentUser = authService.getCurrentUser();
        List<Issue> issues = issueService.getIssuesByAssignee(currentUser);
        List<IssueDto> issueDtos = issues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(issueDtos);
    }
    
    @GetMapping("/reported")
    public ResponseEntity<List<IssueDto>> getReportedIssues() {
        User currentUser = authService.getCurrentUser();
        List<Issue> issues = issueService.getIssuesByReporter(currentUser);
        List<IssueDto> issueDtos = issues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(issueDtos);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
    public ResponseEntity<?> getIssueById(@PathVariable Long id) {
        return issueService.getIssueById(id)
                .map(issue -> ResponseEntity.ok(convertToDto(issue)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/sub-issues")
    @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
    public ResponseEntity<?> getSubIssues(@PathVariable Long id) {
        List<Issue> subIssues = issueService.getSubIssues(id);
        if (subIssues == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<IssueDto> subIssueDtos = subIssues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subIssueDtos);
    }
    
    @PostMapping
    public ResponseEntity<?> createIssue(@Valid @RequestBody IssueCreateDto issueDto) {
        User currentUser = authService.getCurrentUser();
        
        try {
            Issue savedIssue = issueService.createNewIssue(issueDto, currentUser);
            return ResponseEntity.ok(convertToDto(savedIssue));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
   
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
    public ResponseEntity<?> updateIssue(@PathVariable Long id, @Valid @RequestBody IssueDto issueDto) {
        User currentUser = authService.getCurrentUser();
        
        try {
            Issue updatedIssue = issueService.updateExistingIssue(id, issueDto, currentUser);
            return ResponseEntity.ok(convertToDto(updatedIssue));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
   
    @PatchMapping("/{id}/status")
    @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
    public ResponseEntity<?> updateIssueStatus(
            @PathVariable Long id, 
            @RequestParam IssueStatus status) {
        User currentUser = authService.getCurrentUser();
        
        try {
            Issue updatedIssue = issueService.updateIssueStatus(id, status, currentUser);
            return ResponseEntity.ok(convertToDto(updatedIssue));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
   
    @PatchMapping("/{id}/assignee")
    @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
    public ResponseEntity<?> updateIssueAssignee(
            @PathVariable Long id, 
            @RequestParam(required = false) Long assigneeId) {
        User currentUser = authService.getCurrentUser();
        
        try {
            Issue updatedIssue = issueService.updateIssueAssignee(id, assigneeId, currentUser);
            return ResponseEntity.ok(convertToDto(updatedIssue));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
   
    @PatchMapping("/{id}/sprint")
    @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
    public ResponseEntity<?> updateIssueSprint(
            @PathVariable Long id, 
            @RequestParam(required = false) Long sprintId) {
        try {
            Issue updatedIssue = issueService.updateIssueSprint(id, sprintId);
            return ResponseEntity.ok(convertToDto(updatedIssue));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
   
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
    public ResponseEntity<?> deleteIssue(@PathVariable Long id) {
        try {
            issueService.deleteIssue(id);
            return ResponseEntity.ok(new MessageResponse("Issue deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
   
    private IssueDto convertToDto(Issue issue) {
        IssueDto dto = new IssueDto();
        dto.setId(issue.getId());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());
        dto.setType(issue.getType());
        dto.setPriority(issue.getPriority());
        dto.setStatus(issue.getStatus());
        dto.setCreatedDate(issue.getCreatedDate());
        dto.setDueDate(issue.getDueDate());
        dto.setEstimatedHours(issue.getEstimatedHours());
        dto.setStoryPoints(issue.getStoryPoints());
        dto.setWorkListId(issue.getWorkList().getId());
       
        if (issue.getReporter() != null) {
            dto.setReporterId(issue.getReporter().getId());
        }
       
        if (issue.getAssignee() != null) {
            dto.setAssigneeId(issue.getAssignee().getId());
        }
       
        if (issue.getParentIssue() != null) {
            dto.setParentIssueId(issue.getParentIssue().getId());
        }
       
        if (issue.getSprint() != null) {
            dto.setSprintId(issue.getSprint().getId());
        }
       
        if (issue.getLabels() != null) {
            List<Long> labelIds = issue.getLabels().stream()
                    .map(Label::getId)
                    .collect(Collectors.toList());
            dto.setLabelIds(labelIds);
        }
       
        return dto;
    }
}