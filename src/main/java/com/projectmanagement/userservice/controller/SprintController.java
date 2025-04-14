package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.SprintDto;
import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.Sprint;
import com.projectmanagement.userservice.entity.WorkList;
import com.projectmanagement.userservice.service.SprintService;
import com.projectmanagement.userservice.service.WorkListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sprints")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SprintController {

    @Autowired
    private SprintService sprintService;
    
    @Autowired
    private WorkListService workListService;
    
    @GetMapping("/worklist/{workListId}")
    @PreAuthorize("@securityService.canManageWorkList(#workListId, principal)")
    public ResponseEntity<?> getSprintsByWorkList(@PathVariable Long workListId) {
        Optional<WorkList> workListOptional = workListService.getWorkListById(workListId);
        if (!workListOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkList workList = workListOptional.get();
        List<Sprint> sprints = sprintService.getSprintsByWorkList(workList);
        List<SprintDto> sprintDtos = sprints.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sprintDtos);
    }
    
    @GetMapping("/active/worklist/{workListId}")
    @PreAuthorize("@securityService.canManageWorkList(#workListId, principal)")
    public ResponseEntity<?> getActiveSprintsByWorkList(@PathVariable Long workListId) {
        Optional<WorkList> workListOptional = workListService.getWorkListById(workListId);
        if (!workListOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkList workList = workListOptional.get();
        List<Sprint> sprints = sprintService.getActiveSprintsByWorkList(workList);
        List<SprintDto> sprintDtos = sprints.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sprintDtos);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canManageSprint(#id, principal)")
    public ResponseEntity<?> getSprintById(@PathVariable Long id) {
        Optional<Sprint> sprintOptional = sprintService.getSprintById(id);
        return sprintOptional
                .map(sprint -> ResponseEntity.ok(convertToDto(sprint)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("@securityService.canManageWorkList(#sprintDto.workListId, principal)")
    public ResponseEntity<?> createSprint(@Valid @RequestBody SprintDto sprintDto) {
        Optional<WorkList> workListOptional = workListService.getWorkListById(sprintDto.getWorkListId());
        
        if (!workListOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("WorkList not found"));
        }
        
        WorkList workList = workListOptional.get();
        Sprint sprint = new Sprint();
        sprint.setName(sprintDto.getName());
        sprint.setGoal(sprintDto.getGoal());
        sprint.setWorkList(workList);
        
        Sprint savedSprint = sprintService.createSprint(sprint);
        return ResponseEntity.ok(convertToDto(savedSprint));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canManageSprint(#id, principal)")
    public ResponseEntity<?> updateSprint(@PathVariable Long id, @Valid @RequestBody SprintDto sprintDto) {
        Optional<Sprint> sprintOptional = sprintService.getSprintById(id);
        
        if (!sprintOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Sprint sprint = sprintOptional.get();
        sprint.setName(sprintDto.getName());
        sprint.setGoal(sprintDto.getGoal());
        
        Sprint updatedSprint = sprintService.updateSprint(sprint);
        return ResponseEntity.ok(convertToDto(updatedSprint));
    }
    
    @PostMapping("/{id}/start")
    @PreAuthorize("@securityService.canManageSprint(#id, principal)")
    public ResponseEntity<?> startSprint(
            @PathVariable Long id,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        try {
            Sprint sprint = sprintService.startSprint(id, startDate, endDate);
            return ResponseEntity.ok(convertToDto(sprint));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/complete")
    @PreAuthorize("@securityService.canManageSprint(#id, principal)")
    public ResponseEntity<?> completeSprint(@PathVariable Long id) {
        try {
            Sprint sprint = sprintService.completeSprint(id);
            return ResponseEntity.ok(convertToDto(sprint));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/{sprintId}/issues/{issueId}")
    @PreAuthorize("@securityService.canManageSprint(#sprintId, principal) and @securityService.canModifyIssue(#issueId, principal)")
    public ResponseEntity<?> addIssueToSprint(@PathVariable Long sprintId, @PathVariable Long issueId) {
        try {
            sprintService.addIssueToSprint(sprintId, issueId);
            return ResponseEntity.ok(new MessageResponse("Issue added to sprint successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{sprintId}/issues/{issueId}")
    @PreAuthorize("@securityService.canManageSprint(#sprintId, principal) and @securityService.canModifyIssue(#issueId, principal)")
    public ResponseEntity<?> removeIssueFromSprint(@PathVariable Long sprintId, @PathVariable Long issueId) {
        try {
            sprintService.removeIssueFromSprint(sprintId, issueId);
            return ResponseEntity.ok(new MessageResponse("Issue removed from sprint successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canManageSprint(#id, principal)")
    public ResponseEntity<?> deleteSprint(@PathVariable Long id) {
        Optional<Sprint> sprintOptional = sprintService.getSprintById(id);
        if (!sprintOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        sprintService.deleteSprint(id);
        return ResponseEntity.ok(new MessageResponse("Sprint deleted successfully"));
    }
    
    private SprintDto convertToDto(Sprint sprint) {
        SprintDto dto = new SprintDto();
        dto.setId(sprint.getId());
        dto.setName(sprint.getName());
        dto.setGoal(sprint.getGoal());
        dto.setStatus(sprint.getStatus());
        dto.setStartDate(sprint.getStartDate());
        dto.setEndDate(sprint.getEndDate());
        dto.setWorkListId(sprint.getWorkList().getId());
        
        if (sprint.getIssues() != null) {
            List<Long> issueIds = sprint.getIssues().stream()
                    .map(Issue::getId)
                    .collect(Collectors.toList());
            dto.setIssueIds(issueIds);
        }
        
        return dto;
    }
}