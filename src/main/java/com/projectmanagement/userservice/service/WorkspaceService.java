package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.Workspace;
import com.projectmanagement.userservice.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkspaceService {
    
    private final WorkspaceRepository workspaceRepository;
    
    @Autowired
    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }
    
    public List<Workspace> getAllWorkspaces() {
        return workspaceRepository.findAll();
    }
    
    public Optional<Workspace> getWorkspaceById(Long id) {
        return workspaceRepository.findById(id);
    }
    
    public List<Workspace> getWorkspacesByOwner(User owner) {
        return workspaceRepository.findByOwner(owner);
    }
    
    public List<Workspace> getWorkspacesForMember(User member) {
        return workspaceRepository.findByMembersContaining(member);
    }
    
    public Workspace createWorkspace(Workspace workspace) {
        return workspaceRepository.save(workspace);
    }
    
    public Workspace updateWorkspace(Workspace workspace) {
        return workspaceRepository.save(workspace);
    }
    
    public void deleteWorkspace(Long id) {
        workspaceRepository.deleteById(id);
    }
    
    public boolean canCreateWorkspace(User user) {
        // Kiểm tra giới hạn theo subscription
        int currentWorkspaces = workspaceRepository.findByOwner(user).size();
        int maxWorkspaces = user.getSubscription().getMaxProjects();
        return maxWorkspaces == -1 || currentWorkspaces < maxWorkspaces;
    }
    
    public boolean canAddMember(Workspace workspace) {
        // Kiểm tra giới hạn người dùng theo subscription
        int currentMembers = workspace.getMembers().size();
        int maxMembers = workspace.getOwner().getSubscription().getMaxUsersPerWorkspace();
        return maxMembers == -1 || currentMembers < maxMembers;
    }
    
    public boolean isWorkspaceOwner(User user, Workspace workspace) {
        return workspace.getOwner().getId().equals(user.getId());
    }
    
    public boolean isWorkspaceMember(User user, Workspace workspace) {
        return workspace.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
    }
    
    public void addMember(Workspace workspace, User user) {
        if (!isWorkspaceMember(user, workspace)) {
            workspace.getMembers().add(user);
            workspaceRepository.save(workspace);
        }
    }
    
    public void removeMember(Workspace workspace, User user) {
        workspace.getMembers().removeIf(member -> member.getId().equals(user.getId()));
        workspaceRepository.save(workspace);
    }
}