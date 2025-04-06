package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.WorkLogDto;
import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.WorkLog;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.IssueService;
import com.projectmanagement.userservice.service.WorkLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/worklogs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WorkLogController {

    @Autowired
    private WorkLogService workLogService;
    
    @Autowired
    private IssueService issueService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/issue/{issueId}")
    @PreAuthorize("@securityService.canModifyIssue(#issueId, principal)")
    public ResponseEntity<?> getWorkLogsByIssue(@PathVariable Long issueId) {
        Issue issue = issueService.getIssueById(issueId)
                .orElse(null);
        
        if (issue == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<WorkLog> workLogs = workLogService.getWorkLogsByIssue(issue);
        List<WorkLogDto> workLogDtos = workLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workLogDtos);
    }
    
    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUserWorkLogs() {
        User currentUser = authService.getCurrentUser();
        List<WorkLog> workLogs = workLogService.getWorkLogsByUser(currentUser);
        List<WorkLogDto> workLogDtos = workLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workLogDtos);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canManageWorkLog(#id, principal)")
    public ResponseEntity<?> getWorkLogById(@PathVariable Long id) {
        return workLogService.getWorkLogById(id)
                .map(workLog -> ResponseEntity.ok(convertToDto(workLog)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("@securityService.canModifyIssue(#workLogDto.issueId, principal)")
    public ResponseEntity<?> createWorkLog(@Valid @RequestBody WorkLogDto workLogDto) {
        User currentUser = authService.getCurrentUser();
        
        Issue issue = issueService.getIssueById(workLogDto.getIssueId())
                .orElse(null);
        
        if (issue == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Issue not found"));
        }
        
        WorkLog workLog = workLogService.addWorkLog(
                issue, 
                currentUser, 
                workLogDto.getTimeSpentSeconds(), 
                workLogDto.getDescription(), 
                workLogDto.getStartTime() != null ? workLogDto.getStartTime() : LocalDateTime.now()
        );
        
        return ResponseEntity.ok(convertToDto(workLog));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canManageWorkLog(#id, principal)")
    public ResponseEntity<?> updateWorkLog(@PathVariable Long id, @Valid @RequestBody WorkLogDto workLogDto) {
        return workLogService.getWorkLogById(id)
                .map(workLog -> {
                    workLog.setTimeSpentSeconds(workLogDto.getTimeSpentSeconds());
                    workLog.setDescription(workLogDto.getDescription());
                    if (workLogDto.getStartTime() != null) {
                        workLog.setStartTime(workLogDto.getStartTime());
                    }
                    
                    WorkLog updatedWorkLog = workLogService.updateWorkLog(workLog);
                    return ResponseEntity.ok(convertToDto(updatedWorkLog));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canManageWorkLog(#id, principal)")
    public ResponseEntity<?> deleteWorkLog(@PathVariable Long id) {
        return workLogService.getWorkLogById(id)
                .map(workLog -> {
                    workLogService.deleteWorkLog(id);
                    return ResponseEntity.ok(new MessageResponse("WorkLog deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/total/issue/{issueId}")
    @PreAuthorize("@securityService.canModifyIssue(#issueId, principal)")
    public ResponseEntity<?> getTotalTimeSpentOnIssue(@PathVariable Long issueId) {
        return issueService.getIssueById(issueId)
                .map(issue -> {
                    Long totalSeconds = workLogService.getTotalTimeSpentOnIssue(issue);
                    return ResponseEntity.ok(totalSeconds);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/total/user")
    public ResponseEntity<?> getUserTimeSpentInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        User currentUser = authService.getCurrentUser();
        Long totalSeconds = workLogService.getUserTimeSpentInPeriod(currentUser, startDate, endDate);
        return ResponseEntity.ok(totalSeconds);
    }
    
    private WorkLogDto convertToDto(WorkLog workLog) {
        WorkLogDto dto = new WorkLogDto();
        dto.setId(workLog.getId());
        dto.setTimeSpentSeconds(workLog.getTimeSpentSeconds());
        dto.setDescription(workLog.getDescription());
        dto.setStartTime(workLog.getStartTime());
        dto.setCreatedAt(workLog.getCreatedAt());
        dto.setIssueId(workLog.getIssue().getId());
        dto.setUserId(workLog.getUser().getId());
        return dto;
    }
}