package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.WorkList;
import com.projectmanagement.userservice.entity.Workspace;
import com.projectmanagement.userservice.repository.WorkListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkListService {
    
    private final WorkListRepository workListRepository;
    
    @Autowired
    public WorkListService(WorkListRepository workListRepository) {
        this.workListRepository = workListRepository;
    }
    
    public List<WorkList> getAllWorkLists() {
        return workListRepository.findAll();
    }
    
    public Optional<WorkList> getWorkListById(Long id) {
        return workListRepository.findById(id);
    }
    
    public Optional<WorkList> getWorkListByCode(String code) {
        return workListRepository.findByCode(code);
    }
    
    public List<WorkList> getWorkListsByWorkspace(Workspace workspace) {
        return workListRepository.findByWorkspace(workspace);
    }
    
    public WorkList createWorkList(WorkList workList) {
        workList.setCreatedDate(LocalDateTime.now());
        return workListRepository.save(workList);
    }
    
    public WorkList updateWorkList(WorkList workList) {
        return workListRepository.save(workList);
    }
    
    public void deleteWorkList(Long id) {
        workListRepository.deleteById(id);
    }
    
    public boolean isCodeUnique(String code) {
        return !workListRepository.existsByCode(code);
    }
    
    public boolean canCreateWorkList(Workspace workspace) {
        // Kiểm tra giới hạn theo subscription
        int currentWorkLists = workListRepository.findByWorkspace(workspace).size();
        int maxWorkLists = workspace.getOwner().getSubscription().getMaxProjects();
        return maxWorkLists == -1 || currentWorkLists < maxWorkLists;
    }
    
    public boolean isWorkListMember(User user, WorkList workList) {
        return workList.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
    }
    
    public boolean isWorkListLead(User user, WorkList workList) {
        return workList.getLead() != null && workList.getLead().getId().equals(user.getId());
    }
    
    public List<WorkList> getWorkListsForUser(User user) {
        // Tìm tất cả worklist mà user là thành viên
        // Đây là một phương thức helper đơn giản, bạn có thể cần tạo query phức tạp hơn
        return workListRepository.findAll().stream()
                .filter(workList -> isWorkListMember(user, workList) || isWorkListLead(user, workList))
                .toList();
    }
    
    public void addMember(WorkList workList, User user) {
        if (!isWorkListMember(user, workList)) {
            workList.getMembers().add(user);
            workListRepository.save(workList);
        }
    }
    
    public void removeMember(WorkList workList, User user) {
        workList.getMembers().removeIf(member -> member.getId().equals(user.getId()));
        workListRepository.save(workList);
    }
}