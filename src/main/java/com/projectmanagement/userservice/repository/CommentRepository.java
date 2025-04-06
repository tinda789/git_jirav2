package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.Comment;
import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByIssue(Issue issue);
    List<Comment> findByAuthor(User author);
}