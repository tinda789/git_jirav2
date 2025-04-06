package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    List<WorkLog> findByIssueOrderByStartTimeDesc(Issue issue);
    List<WorkLog> findByUserOrderByStartTimeDesc(User user);
    
    @Query("SELECT SUM(w.timeSpentSeconds) FROM WorkLog w WHERE w.issue = :issue")
    Long sumTimeSpentByIssue(Issue issue);
    
    @Query("SELECT SUM(w.timeSpentSeconds) FROM WorkLog w WHERE w.user = :user AND w.startTime BETWEEN :startDate AND :endDate")
    Long sumTimeSpentByUserInPeriod(User user, LocalDateTime startDate, LocalDateTime endDate);
}