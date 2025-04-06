package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.IssueStatus;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.WorkList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByWorkList(WorkList workList);
    List<Issue> findByAssignee(User assignee);
    List<Issue> findByReporter(User reporter);
    List<Issue> findByStatus(IssueStatus status);
    List<Issue> findByDueDateBefore(LocalDateTime date);
    List<Issue> findByWorkListAndStatus(WorkList workList, IssueStatus status);
    List<Issue> findByParentIssue(Issue parentIssue);
}