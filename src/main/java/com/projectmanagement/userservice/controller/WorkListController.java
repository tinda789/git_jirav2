package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.WorkListCreateDto;
import com.projectmanagement.userservice.dto.WorkListDto;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.WorkList;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.UserService;
import com.projectmanagement.userservice.service.WorkListService;
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
@RequestMapping("/worklists")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WorkListController {

    @Autowired
    private WorkListService workListService;
    
    @Autowired
    private WorkspaceService workspaceService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<WorkListDto>> getAllWorkLists() {
        User currentUser = authService.getCurrentUser();
        List<WorkList> workLists = workListService.getWorkListsForUser(currentUser);
        List<WorkListDto> workListDtos = workLists.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workListDtos);
    }
    
    @GetMapping("/workspace/{workspaceId}")
    @PreAuthorize("@securityService.canManageWorkspace(#workspaceId, principal)")
    public ResponseEntity<?> getWorkListsByWorkspace(@PathVariable Long workspaceId) {
        return workspaceService.getWorkspaceById(workspaceId)
                .map(workspace -> {
                    List<WorkList> workLists = workListService.getWorkListsByWorkspace(workspace);
                    List<WorkListDto> workListDtos = workLists.stream()
                            .map(this::convertToDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(workListDtos);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canManageWorkList(#id, principal)")
    public ResponseEntity<?> getWorkListById(@PathVariable Long id) {
        return workListService.getWorkListById(id)
                .map(workList -> ResponseEntity.ok(convertToDto(workList)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createWorkList(@Valid @RequestBody WorkListCreateDto workListDto) {
        return workspaceService.getWorkspaceById(workListDto.getWorkspaceId())
                .map(workspace -> {
                    User currentUser = authService.getCurrentUser();
                    
                    // Kiểm tra quyền tạo worklist trong workspace
                    if (!workspaceService.isWorkspaceOwner(currentUser, workspace) && 
                        !workspaceService.isWorkspaceMember(currentUser, workspace)) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("You don't have permission to create worklists in this workspace"));
                    }
                    
                    // Kiểm tra giới hạn số lượng worklist
                    if (!workListService.canCreateWorkList(workspace)) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("You have reached the maximum number of worklists allowed for your subscription"));
                    }
                    
                    // Kiểm tra code độc nhất
                    if (!workListService.isCodeUnique(workListDto.getCode())) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Worklist code already exists"));
                    }
                    
                    WorkList workList = new WorkList();
                    workList.setName(workListDto.getName());
                    workList.setCode(workListDto.getCode());
                    workList.setDescription(workListDto.getDescription());
                    workList.setWorkspace(workspace);
                    workList.setMembers(new ArrayList<>());
                    workList.getMembers().add(currentUser); // Thêm creator làm member
                    
                    // Thiết lập lead nếu có
                    if (workListDto.getLeadId() != null) {
                        userService.getUserById(workListDto.getLeadId())
                                .ifPresent(workList::setLead);
                    } else {
                        // Nếu không có lead, mặc định người tạo là lead
                        workList.setLead(currentUser);
                    }
                    
                    WorkList savedWorkList = workListService.createWorkList(workList);
                    return ResponseEntity.ok(convertToDto(savedWorkList));
                })
                .orElse(ResponseEntity.badRequest().body(new MessageResponse("Workspace not found")));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canManageWorkList(#id, principal)")
    public ResponseEntity<?> updateWorkList(@PathVariable Long id, @Valid @RequestBody WorkListCreateDto workListDto) {
        return workListService.getWorkListById(id)
                .map(workList -> {
                    workList.setName(workListDto.getName());
                    workList.setDescription(workListDto.getDescription());
                    
                    // Chỉ update code nếu mã mới khác mã cũ và là duy nhất
                    if (!workList.getCode().equals(workListDto.getCode())) {
                        if (!workListService.isCodeUnique(workListDto.getCode())) {
                            return ResponseEntity.badRequest()
                                    .body(new MessageResponse("Worklist code already exists"));
                        }
                        workList.setCode(workListDto.getCode());
                    }
                    
                    // Update lead nếu có thay đổi
                    if (workListDto.getLeadId() != null && 
                            (workList.getLead() == null || !workList.getLead().getId().equals(workListDto.getLeadId()))) {
                        userService.getUserById(workListDto.getLeadId())
                                .ifPresent(workList::setLead);
                    }
                    
                    WorkList updatedWorkList = workListService.updateWorkList(workList);
                    return ResponseEntity.ok(convertToDto(updatedWorkList));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canManageWorkList(#id, principal)")
    public ResponseEntity<?> deleteWorkList(@PathVariable Long id) {
        if (!workListService.getWorkListById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        workListService.deleteWorkList(id);
        return ResponseEntity.ok(new MessageResponse("WorkList deleted successfully"));
    }
    
    @PostMapping("/{id}/members/{userId}")
    @PreAuthorize("@securityService.canManageWorkList(#id, principal)")
    public ResponseEntity<?> addMember(@PathVariable Long id, @PathVariable Long userId) {
        return workListService.getWorkListById(id)
                .map(workList -> {
                    return userService.getUserById(userId)
                            .map(user -> {
                                // Kiểm tra user có phải member của workspace không
                                if (!workspaceService.isWorkspaceMember(user, workList.getWorkspace())) {
                                    return ResponseEntity.badRequest()
                                            .body(new MessageResponse("User must be a member of the workspace first"));
                                }
                                
                                workListService.addMember(workList, user);
                                return ResponseEntity.ok(new MessageResponse("Member added successfully"));
                            })
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("@securityService.canManageWorkList(#id, principal)")
    public ResponseEntity<?> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        return workListService.getWorkListById(id)
                .map(workList -> {
                    return userService.getUserById(userId)
                            .map(user -> {
                                // Không thể xóa lead
                                if (workList.getLead() != null && workList.getLead().getId().equals(user.getId())) {
                                    return ResponseEntity.badRequest()
                                            .body(new MessageResponse("Cannot remove worklist lead"));
                                }
                                
                                workListService.removeMember(workList, user);
                                return ResponseEntity.ok(new MessageResponse("Member removed successfully"));
                            })
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private WorkListDto convertToDto(WorkList workList) {
        WorkListDto dto = new WorkListDto();
        dto.setId(workList.getId());
        dto.setName(workList.getName());
        dto.setCode(workList.getCode());
        dto.setDescription(workList.getDescription());
        dto.setCreatedDate(workList.getCreatedDate());
        dto.setWorkspaceId(workList.getWorkspace().getId());
        
        if (workList.getLead() != null) {
            dto.setLeadId(workList.getLead().getId());
        }
        
        List<Long> memberIds = workList.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        dto.setMemberIds(memberIds);
        
        return dto;
    }
}