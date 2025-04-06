package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.WorkspaceCreateDto;
import com.projectmanagement.userservice.dto.WorkspaceDto;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.Workspace;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.UserService;
import com.projectmanagement.userservice.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/workspaces")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<WorkspaceDto>> getWorkspaces() {
        User currentUser = authService.getCurrentUser();
        List<Workspace> workspaces = workspaceService.getWorkspacesForMember(currentUser);
        List<WorkspaceDto> workspaceDtos = workspaces.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDtos);
    }
    
    @GetMapping("/owned")
    public ResponseEntity<List<WorkspaceDto>> getOwnedWorkspaces() {
        User currentUser = authService.getCurrentUser();
        List<Workspace> workspaces = workspaceService.getWorkspacesByOwner(currentUser);
        List<WorkspaceDto> workspaceDtos = workspaces.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDtos);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canManageWorkspace(#id, principal)")
    public ResponseEntity<?> getWorkspaceById(@PathVariable Long id) {
        return workspaceService.getWorkspaceById(id)
                .map(workspace -> ResponseEntity.ok(convertToDto(workspace)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createWorkspace(@Valid @RequestBody WorkspaceCreateDto workspaceDto) {
        User currentUser = authService.getCurrentUser();
        
        // Kiểm tra giới hạn theo subscription
        if (!workspaceService.canCreateWorkspace(currentUser)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("You have reached the maximum number of workspaces allowed for your subscription"));
        }
        
        Workspace workspace = new Workspace();
        workspace.setName(workspaceDto.getName());
        workspace.setDescription(workspaceDto.getDescription());
        workspace.setOwner(currentUser);
        workspace.setMembers(new ArrayList<>());
        workspace.getMembers().add(currentUser); // Thêm owner làm member
        
        Workspace savedWorkspace = workspaceService.createWorkspace(workspace);
        return ResponseEntity.ok(convertToDto(savedWorkspace));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isWorkspaceOwner(#id, principal)")
    public ResponseEntity<?> updateWorkspace(@PathVariable Long id, @Valid @RequestBody WorkspaceCreateDto workspaceDto) {
        return workspaceService.getWorkspaceById(id)
                .map(workspace -> {
                    workspace.setName(workspaceDto.getName());
                    workspace.setDescription(workspaceDto.getDescription());
                    Workspace updatedWorkspace = workspaceService.updateWorkspace(workspace);
                    return ResponseEntity.ok(convertToDto(updatedWorkspace));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isWorkspaceOwner(#id, principal)")
    public ResponseEntity<?> deleteWorkspace(@PathVariable Long id) {
        if (!workspaceService.getWorkspaceById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.ok(new MessageResponse("Workspace deleted successfully"));
    }
    
    @PostMapping("/{id}/members/{userId}")
    @PreAuthorize("@securityService.isWorkspaceOwner(#id, principal)")
    public ResponseEntity<?> addMember(@PathVariable Long id, @PathVariable Long userId) {
        return workspaceService.getWorkspaceById(id)
                .map(workspace -> {
                    return userService.getUserById(userId)
                            .map(user -> {
                                // Kiểm tra giới hạn số lượng thành viên
                                if (!workspaceService.canAddMember(workspace)) {
                                    return ResponseEntity.badRequest()
                                            .body(new MessageResponse("Workspace member limit reached for your subscription"));
                                }
                                
                                workspaceService.addMember(workspace, user);
                                return ResponseEntity.ok(new MessageResponse("Member added successfully"));
                            })
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("@securityService.isWorkspaceOwner(#id, principal)")
    public ResponseEntity<?> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        return workspaceService.getWorkspaceById(id)
                .map(workspace -> {
                    return userService.getUserById(userId)
                            .map(user -> {
                                // Không thể xóa owner
                                if (workspace.getOwner().getId().equals(user.getId())) {
                                    return ResponseEntity.badRequest()
                                            .body(new MessageResponse("Cannot remove workspace owner"));
                                }
                                
                                workspaceService.removeMember(workspace, user);
                                return ResponseEntity.ok(new MessageResponse("Member removed successfully"));
                            })
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private WorkspaceDto convertToDto(Workspace workspace) {
        WorkspaceDto dto = new WorkspaceDto();
        dto.setId(workspace.getId());
        dto.setName(workspace.getName());
        dto.setDescription(workspace.getDescription());
        dto.setOwnerId(workspace.getOwner().getId());
        
        List<Long> memberIds = workspace.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        dto.setMemberIds(memberIds);
        
        return dto;
    }
}