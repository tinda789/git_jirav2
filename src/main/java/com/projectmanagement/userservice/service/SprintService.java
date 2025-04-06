package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.Sprint;
import com.projectmanagement.userservice.entity.SprintStatus;
import com.projectmanagement.userservice.entity.WorkList;
import com.projectmanagement.userservice.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SprintService {
    
    private final SprintRepository sprintRepository;
    private final IssueService issueService;
    
    @Autowired
    public SprintService(SprintRepository sprintRepository, IssueService issueService) {
        this.sprintRepository = sprintRepository;
        this.issueService = issueService;
    }
    
    public List<Sprint> getAllSprints() {
        return sprintRepository.findAll();
    }
    
    public Optional<Sprint> getSprintById(Long id) {
        return sprintRepository.findById(id);
    }
    
    public List<Sprint> getSprintsByWorkList(WorkList workList) {
        return sprintRepository.findByWorkList(workList);
    }
    
    public List<Sprint> getActiveSprintsByWorkList(WorkList workList) {
        return sprintRepository.findByWorkListAndStatus(workList, SprintStatus.ACTIVE);
    }
    
    public Sprint createSprint(Sprint sprint) {
        sprint.setStatus(SprintStatus.PLANNING);
        return sprintRepository.save(sprint);
    }
    
    public Sprint updateSprint(Sprint sprint) {
        return sprintRepository.save(sprint);
    }
    
    @Transactional
    public Sprint startSprint(Long sprintId, LocalDateTime startDate, LocalDateTime endDate) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
        
        // Kiểm tra xem có sprint nào khác đang active không
        List<Sprint> activeSprints = sprintRepository
                .findByWorkListAndStatus(sprint.getWorkList(), SprintStatus.ACTIVE);
        
        if (!activeSprints.isEmpty()) {
            throw new RuntimeException("Another sprint is already active");
        }
        
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setStartDate(startDate);
        sprint.setEndDate(endDate);
        
        return sprintRepository.save(sprint);
    }
    
    @Transactional
    public Sprint completeSprint(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
        
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new RuntimeException("Only active sprints can be completed");
        }
        
        sprint.setStatus(SprintStatus.COMPLETED);
        
        // Xử lý các issues chưa hoàn thành
        // Có thể chuyển sang sprint tiếp theo hoặc backlog
        
        return sprintRepository.save(sprint);
    }
    
    @Transactional
    public void addIssueToSprint(Long sprintId, Long issueId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
        
        Issue issue = issueService.getIssueById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        // Kiểm tra issue thuộc cùng worklist
        if (!issue.getWorkList().getId().equals(sprint.getWorkList().getId())) {
            throw new RuntimeException("Issue must belong to the same worklist as the sprint");
        }
        
        issue.setSprint(sprint);
        issueService.updateIssue(issue);
    }
    
    @Transactional
    public void removeIssueFromSprint(Long sprintId, Long issueId) {
        Issue issue = issueService.getIssueById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        if (issue.getSprint() == null || !issue.getSprint().getId().equals(sprintId)) {
            throw new RuntimeException("Issue is not in the specified sprint");
        }
        
        issue.setSprint(null);
        issueService.updateIssue(issue);
    }
    
    public void deleteSprint(Long id) {
        sprintRepository.deleteById(id);
    }
}