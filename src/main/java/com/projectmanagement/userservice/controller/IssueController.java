package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.IssueCreateDto;
import com.projectmanagement.userservice.dto.IssueDto;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.entity.*;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.IssueService;
import com.projectmanagement.userservice.service.UserService;
import com.projectmanagement.userservice.service.WorkListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/issues")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IssueController {

    @Autowired
    private IssueService issueService;
    
    @Autowired
    private WorkListService workListService;
    
    @Autowired
    private UserService userService;
    
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
        return workListService.getWorkListById(workListId)
                .map(workList -> {
                    List<Issue> issues = issueService.getIssuesByWorkList(workList);
                    List<IssueDto> issueDtos = issues.stream()
                            .map(this::convertToDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(issueDtos);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
    public ResponseEntity<?> getIssueById(@PathVariable Long id) {
        return issueService.getIssueById(id)
                .map(issue -> ResponseEntity.ok(convertToDto(issue)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createIssue(@Valid @RequestBody IssueCreateDto issueDto) {
        User currentUser = authService.getCurrentUser();
        
        return workListService.getWorkListById(issueDto.getWorkListId())
                .map(workList -> {
                    // Kiểm tra quyền tạo issue trong worklist
                    if (!workListService.isWorkListMember(currentUser, workList) && 
                        !workListService.isWorkListLead(currentUser, workList)) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("You don't have permission to create issues in this worklist"));
                    }
                    
                    Issue issue = new Issue();
                    issue.setTitle(issueDto.getTitle());
                    issue.setDescription(issueDto.getDescription());
                    issue.setType(issueDto.getType());
                    issue.setPriority(issueDto.getPriority());
                    issue.setWorkList(workList);
                    issue.setReporter(currentUser);
                    
                    // Thiết lập assignee nếu có
                    if (issueDto.getAssigneeId() != null) {
                        userService.getUserById(issueDto.getAssigneeId())
                                .ifPresent(issue::setAssignee);
                    }
                    
                    // Thiết lập parent issue nếu có
                    if (issueDto.getParentIssueId() != null) {
                        issueService.getIssueById(issueDto.getParentIssueId())
                                .ifPresent(issue::setParentIssue);
                    }
                    
                    if (issueDto.getDueDate() != null) {
                        issue.setDueDate(issueDto.getDueDate());
                    }
                    
                    issue.setEstimatedHours(issueDto.getEstimatedHours());
                    
                    // Thiết lập labels nếu có
                    if (issueDto.getLabelIds() != null && !issueDto.getLabelIds().isEmpty()) {
                        issue.setLabels(new ArrayList<>());
                        // Xử lý labels sẽ được triển khai khi có LabelService
                    }
                    
                    Issue savedIssue = issueService.createIssue(issue);
                    return ResponseEntity.ok(convertToDto(savedIssue));
                })
                .orElse(ResponseEntity.badRequest().body(new MessageResponse("WorkList not found")));
   }
   
   @PutMapping("/{id}")
   @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
   public ResponseEntity<?> updateIssue(@PathVariable Long id, @Valid @RequestBody IssueDto issueDto) {
       return issueService.getIssueById(id)
               .map(issue -> {
                   issue.setTitle(issueDto.getTitle());
                   if (issueDto.getDescription() != null) {
                       issue.setDescription(issueDto.getDescription());
                   }
                   
                   if (issueDto.getType() != null) {
                       issue.setType(issueDto.getType());
                   }
                   
                   if (issueDto.getPriority() != null) {
                       issue.setPriority(issueDto.getPriority());
                   }
                   
                   if (issueDto.getStatus() != null) {
                       issue.setStatus(issueDto.getStatus());
                   }
                   
                   if (issueDto.getAssigneeId() != null) {
                       userService.getUserById(issueDto.getAssigneeId())
                               .ifPresent(issue::setAssignee);
                   }
                   
                   if (issueDto.getDueDate() != null) {
                       issue.setDueDate(issueDto.getDueDate());
                   }
                   
                   if (issueDto.getEstimatedHours() != null) {
                       issue.setEstimatedHours(issueDto.getEstimatedHours());
                   }
                   
                   // Cập nhật labels nếu có sự thay đổi
                   // Sẽ được triển khai khi có LabelService
                   
                   Issue updatedIssue = issueService.updateIssue(issue);
                   return ResponseEntity.ok(convertToDto(updatedIssue));
               })
               .orElse(ResponseEntity.notFound().build());
   }
   
   @DeleteMapping("/{id}")
   @PreAuthorize("@securityService.canModifyIssue(#id, principal)")
   public ResponseEntity<?> deleteIssue(@PathVariable Long id) {
       if (!issueService.getIssueById(id).isPresent()) {
           return ResponseEntity.notFound().build();
       }
       
       issueService.deleteIssue(id);
       return ResponseEntity.ok(new MessageResponse("Issue deleted successfully"));
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
       
       if (issue.getLabels() != null) {
           List<Long> labelIds = issue.getLabels().stream()
                   .map(Label::getId)
                   .collect(Collectors.toList());
           dto.setLabelIds(labelIds);
       }
       
       return dto;
   }
}