package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.dto.IssueCreateDto;
import com.projectmanagement.userservice.dto.IssueDto;
import com.projectmanagement.userservice.entity.*;
import com.projectmanagement.userservice.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IssueService {
    
    private final IssueRepository issueRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WorkListService workListService;
    
    @Autowired
    private SprintService sprintService;
    
    @Autowired
    private AutomationService automationService;
    
    @Autowired
    public IssueService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
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
    
    // Lấy issues theo workList ID
    public List<Issue> getIssuesByWorkListId(Long workListId) {
        return workListService.getWorkListById(workListId)
                .map(this::getIssuesByWorkList)
                .orElse(null);
    }
    
    // Lấy issues theo sprint ID
    public List<Issue> getIssuesBySprintId(Long sprintId) {
        return sprintService.getSprintById(sprintId)
                .map(sprint -> sprint.getIssues())
                .orElse(null);
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
    
    // Lấy sub-issues theo ID của issue cha
    public List<Issue> getSubIssues(Long parentIssueId) {
        return getIssueById(parentIssueId)
                .map(this::getSubIssues)
                .orElse(null);
    }
    
    // Tạo issue mới
    @Transactional
    public Issue createNewIssue(IssueCreateDto issueDto, User currentUser) {
        WorkList workList = workListService.getWorkListById(issueDto.getWorkListId())
                .orElseThrow(() -> new RuntimeException("WorkList not found"));
        
        // Kiểm tra quyền tạo issue trong workList
        if (!workListService.isWorkListMember(currentUser, workList) && 
            !workListService.isWorkListLead(currentUser, workList)) {
            throw new RuntimeException("You don't have permission to create issues in this workList");
        }
        
        Issue issue = new Issue();
        issue.setTitle(issueDto.getTitle());
        issue.setDescription(issueDto.getDescription());
        issue.setType(issueDto.getType());
        issue.setPriority(issueDto.getPriority());
        issue.setWorkList(workList);
        issue.setReporter(currentUser);
        issue.setCreatedDate(LocalDateTime.now());
        
        // Set default status
        issue.setStatus(IssueStatus.TODO);
        
        // Thiết lập assignee nếu có
        if (issueDto.getAssigneeId() != null) {
            userService.getUserById(issueDto.getAssigneeId())
                    .ifPresent(issue::setAssignee);
        }
        
        // Thiết lập parent issue nếu có
        if (issueDto.getParentIssueId() != null) {
            getIssueById(issueDto.getParentIssueId())
                    .ifPresent(issue::setParentIssue);
        }
        
        if (issueDto.getDueDate() != null) {
            issue.setDueDate(issueDto.getDueDate());
        }
        
        issue.setEstimatedHours(issueDto.getEstimatedHours());
        
        // Set Story Points nếu có
        if (issueDto.getStoryPoints() != null) {
            issue.setStoryPoints(issueDto.getStoryPoints());
        }
        
        // Thiết lập Sprint nếu có
        if (issueDto.getSprintId() != null) {
            sprintService.getSprintById(issueDto.getSprintId())
                    .ifPresent(issue::setSprint);
        }
        
        // Thiết lập labels nếu có
        if (issueDto.getLabelIds() != null && !issueDto.getLabelIds().isEmpty()) {
            issue.setLabels(new ArrayList<>());
            // Xử lý labels khi có LabelService
        }
        
        Issue savedIssue = createIssue(issue);
        
        // Kích hoạt automation rule cho sự kiện tạo issue
        try {
            automationService.processEvent(TriggerEvent.ISSUE_CREATED, savedIssue, currentUser);
        } catch (Exception e) {
            // Log error nhưng vẫn tiếp tục
        }
        
        return savedIssue;
    }
    
    // Cập nhật issue hiện có
    @Transactional
    public Issue updateExistingIssue(Long issueId, IssueDto issueDto, User currentUser) {
        Issue issue = getIssueById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        IssueStatus oldStatus = issue.getStatus();
        User oldAssignee = issue.getAssignee();
        
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
        } else if (issueDto.getAssigneeId() == null && issue.getAssignee() != null) {
            // Nếu assigneeId là null và issue đang có assignee, loại bỏ assignee
            issue.setAssignee(null);
        }
        
        if (issueDto.getDueDate() != null) {
            issue.setDueDate(issueDto.getDueDate());
        }
        
        if (issueDto.getEstimatedHours() != null) {
            issue.setEstimatedHours(issueDto.getEstimatedHours());
        }
        
        if (issueDto.getStoryPoints() != null) {
            issue.setStoryPoints(issueDto.getStoryPoints());
        }
        
        if (issueDto.getSprintId() != null) {
            sprintService.getSprintById(issueDto.getSprintId())
                    .ifPresent(issue::setSprint);
        } else if (issueDto.getSprintId() == null && issue.getSprint() != null) {
            // Nếu sprintId là null và issue đang có sprint, loại bỏ issue khỏi sprint
            issue.setSprint(null);
        }
        
        // Cập nhật labels nếu có
        // Sẽ triển khai khi có LabelService
        
        Issue updatedIssue = updateIssue(issue);
        
        // Kích hoạt automation rule cho các sự kiện cập nhật
        try {
            // Sự kiện cập nhật issue
            automationService.processEvent(TriggerEvent.ISSUE_UPDATED, updatedIssue, currentUser);
            
            // Sự kiện thay đổi trạng thái
            if (oldStatus != updatedIssue.getStatus()) {
                automationService.processEvent(TriggerEvent.STATUS_CHANGED, updatedIssue, currentUser);
            }
            
            // Sự kiện thay đổi người được gán
            if ((oldAssignee == null && updatedIssue.getAssignee() != null) ||
                (oldAssignee != null && updatedIssue.getAssignee() == null) ||
                (oldAssignee != null && updatedIssue.getAssignee() != null && 
                 !oldAssignee.getId().equals(updatedIssue.getAssignee().getId()))) {
                automationService.processEvent(TriggerEvent.ASSIGNEE_CHANGED, updatedIssue, currentUser);
            }
        } catch (Exception e) {
            // Log error nhưng vẫn tiếp tục
        }
        
        return updatedIssue;
    }
    
    // Cập nhật trạng thái của issue
    @Transactional
    public Issue updateIssueStatus(Long issueId, IssueStatus status, User currentUser) {
        Issue issue = getIssueById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        IssueStatus oldStatus = issue.getStatus();
        issue.setStatus(status);
        Issue updatedIssue = updateIssue(issue);
        
        // Kích hoạt automation rule cho sự kiện thay đổi trạng thái
        try {
            if (oldStatus != status) {
                automationService.processEvent(TriggerEvent.STATUS_CHANGED, updatedIssue, currentUser);
            }
        } catch (Exception e) {
            // Log error nhưng vẫn tiếp tục
        }
        
        return updatedIssue;
    }
    
    // Cập nhật người được gán cho issue
    @Transactional
    public Issue updateIssueAssignee(Long issueId, Long assigneeId, User currentUser) {
        Issue issue = getIssueById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        User oldAssignee = issue.getAssignee();
        
        if (assigneeId != null) {
            User assignee = userService.getUserById(assigneeId)
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            issue.setAssignee(assignee);
        } else {
            issue.setAssignee(null);
        }
        
        Issue updatedIssue = updateIssue(issue);
        
        // Kích hoạt automation rule cho sự kiện thay đổi người được gán
        try {
            boolean hasAssigneeChanged = 
                (oldAssignee == null && issue.getAssignee() != null) ||
                (oldAssignee != null && issue.getAssignee() == null) ||
                (oldAssignee != null && issue.getAssignee() != null && 
                 !oldAssignee.getId().equals(issue.getAssignee().getId()));
            
            if (hasAssigneeChanged) {
                automationService.processEvent(TriggerEvent.ASSIGNEE_CHANGED, updatedIssue, currentUser);
            }
        } catch (Exception e) {
            // Log error nhưng vẫn tiếp tục
        }
        
        return updatedIssue;
    }
    
    // Cập nhật sprint cho issue
    @Transactional
    public Issue updateIssueSprint(Long issueId, Long sprintId) {
        Issue issue = getIssueById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        if (sprintId != null) {
            Sprint sprint = sprintService.getSprintById(sprintId)
                    .orElseThrow(() -> new RuntimeException("Sprint not found"));
            issue.setSprint(sprint);
        } else {
            issue.setSprint(null);
        }
        
        return updateIssue(issue);
    }
    
    public Issue createIssue(Issue issue) {
        return issueRepository.save(issue);
    }
    
    public Issue updateIssue(Issue issue) {
        return issueRepository.save(issue);
    }
    
    @Transactional
    public void deleteIssue(Long id) {
        Optional<Issue> issueOpt = issueRepository.findById(id);
        if (!issueOpt.isPresent()) {
            throw new RuntimeException("Issue not found");
        }
        
        // Xử lý các sub-issues nếu có
        Issue issue = issueOpt.get();
        List<Issue> subIssues = getSubIssues(issue);
        if (subIssues != null && !subIssues.isEmpty()) {
            for (Issue subIssue : subIssues) {
                subIssue.setParentIssue(null);
                updateIssue(subIssue);
            }
        }
        
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