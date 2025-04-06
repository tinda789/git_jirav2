package com.projectmanagement.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.userservice.entity.*;
import com.projectmanagement.userservice.repository.AutomationRuleRepository;
import com.projectmanagement.userservice.repository.IssueRepository;
import com.projectmanagement.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AutomationService {
    
    private final AutomationRuleRepository ruleRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AutomationService(
            AutomationRuleRepository ruleRepository,
            IssueRepository issueRepository,
            UserRepository userRepository,
            CommentService commentService,
            NotificationService notificationService) {
        this.ruleRepository = ruleRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.commentService = commentService;
        this.notificationService = notificationService;
        this.objectMapper = new ObjectMapper();
    }
    
    public List<AutomationRule> getRulesByWorkList(WorkList workList) {
        return ruleRepository.findByWorkListAndIsActiveTrue(workList);
    }
    
    public Optional<AutomationRule> getRuleById(Long id) {
        return ruleRepository.findById(id);
    }
    
    public AutomationRule createRule(AutomationRule rule) {
        rule.setIsActive(true);
        return ruleRepository.save(rule);
    }
    
    public AutomationRule updateRule(AutomationRule rule) {
        return ruleRepository.save(rule);
    }
    
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }
    
    public void processEvent(TriggerEvent event, Issue issue, User user) {
        List<AutomationRule> rules = ruleRepository.findByTriggerEventAndIsActiveTrue(event);
        
        for (AutomationRule rule : rules) {
            if (rule.getWorkList() != null && 
                !issue.getWorkList().getId().equals(rule.getWorkList().getId())) {
                continue;
            }
            
            if (matchesConditions(rule.getConditions(), issue)) {
                executeAction(rule.getActionType(), rule.getActionParameters(), issue, user);
            }
        }
    }
    
    private boolean matchesConditions(String conditionsJson, Issue issue) {
        try {
            if (conditionsJson == null || conditionsJson.isEmpty()) {
                return true;
            }
            
            Map<String, Object> conditions = objectMapper.readValue(conditionsJson, Map.class);
            
            if (conditions.containsKey("status") && 
                !issue.getStatus().name().equals(conditions.get("status"))) {
                return false;
            }
            
            if (conditions.containsKey("priority") && 
                !issue.getPriority().name().equals(conditions.get("priority"))) {
                return false;
            }
            
            if (conditions.containsKey("type") && 
                !issue.getType().name().equals(conditions.get("type"))) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void executeAction(ActionType actionType, String parametersJson, Issue issue, User triggeredBy) {
        try {
            if (parametersJson == null || parametersJson.isEmpty()) {
                return;
            }
            
            Map<String, Object> params = objectMapper.readValue(parametersJson, Map.class);
            
            switch (actionType) {
                case UPDATE_STATUS:
                    if (params.containsKey("status")) {
                        IssueStatus newStatus = IssueStatus.valueOf(params.get("status").toString());
                        issue.setStatus(newStatus);
                        issueRepository.save(issue);
                    }
                    break;
                    
                case ASSIGN_USER:
                    if (params.containsKey("userId")) {
                        Long userId = Long.valueOf(params.get("userId").toString());
                        userRepository.findById(userId).ifPresent(user -> {
                            issue.setAssignee(user);
                            issueRepository.save(issue);
                        });
                    }
                    break;
                    
                case ADD_COMMENT:
                    if (params.containsKey("comment")) {
                        String content = params.get("comment").toString();
                        Comment comment = new Comment();
                        comment.setContent(content);
                        comment.setIssue(issue);
                        comment.setAuthor(triggeredBy);
                        commentService.createComment(comment);
                    }
                    break;
                    
                case SET_PRIORITY:
                    if (params.containsKey("priority")) {
                        IssuePriority newPriority = IssuePriority.valueOf(params.get("priority").toString());
                        issue.setPriority(newPriority);
                        issueRepository.save(issue);
                    }
                    break;
                    
                case SEND_NOTIFICATION:
                    if (params.containsKey("message") && issue.getAssignee() != null) {
                        String message = params.get("message").toString();
                        notificationService.sendNotification(issue.getAssignee(), message, issue);
                    }
                    break;
            }
        } catch (Exception e) {
            // Log error
        }
    }
}