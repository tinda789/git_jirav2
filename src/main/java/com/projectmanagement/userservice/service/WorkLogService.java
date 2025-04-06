package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.WorkLog;
import com.projectmanagement.userservice.repository.WorkLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkLogService {
    
    private final WorkLogRepository workLogRepository;
    
    @Autowired
    public WorkLogService(WorkLogRepository workLogRepository) {
        this.workLogRepository = workLogRepository;
    }
    
    public List<WorkLog> getAllWorkLogs() {
        return workLogRepository.findAll();
    }
    
    public Optional<WorkLog> getWorkLogById(Long id) {
        return workLogRepository.findById(id);
    }
    
    public List<WorkLog> getWorkLogsByIssue(Issue issue) {
        return workLogRepository.findByIssueOrderByStartTimeDesc(issue);
    }
    
    public List<WorkLog> getWorkLogsByUser(User user) {
        return workLogRepository.findByUserOrderByStartTimeDesc(user);
    }
    
    public WorkLog addWorkLog(Issue issue, User user, Long timeSpentSeconds, 
            String description, LocalDateTime startTime) {
        WorkLog workLog = new WorkLog();
        workLog.setIssue(issue);
        workLog.setUser(user);
        workLog.setTimeSpentSeconds(timeSpentSeconds);
        workLog.setDescription(description);
        workLog.setStartTime(startTime);
        workLog.setCreatedAt(LocalDateTime.now());
        
        return workLogRepository.save(workLog);
    }
    
    public WorkLog updateWorkLog(WorkLog workLog) {
        return workLogRepository.save(workLog);
    }
    
    public void deleteWorkLog(Long id) {
        workLogRepository.deleteById(id);
    }
    
    public Long getTotalTimeSpentOnIssue(Issue issue) {
        return workLogRepository.sumTimeSpentByIssue(issue);
    }
    
    public Long getUserTimeSpentInPeriod(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return workLogRepository.sumTimeSpentByUserInPeriod(user, startDate, endDate);
    }
}