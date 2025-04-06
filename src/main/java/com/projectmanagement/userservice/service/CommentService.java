package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Comment;
import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    
    private final CommentRepository commentRepository;
    
    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
    
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }
    
    public List<Comment> getCommentsByIssue(Issue issue) {
        return commentRepository.findByIssue(issue);
    }
    
    public List<Comment> getCommentsByAuthor(User author) {
        return commentRepository.findByAuthor(author);
    }
    
    public Comment createComment(Comment comment) {
        comment.setCreatedDate(LocalDateTime.now());
        return commentRepository.save(comment);
    }
    
    public Comment updateComment(Comment comment) {
        return commentRepository.save(comment);
    }
    
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
    
    public boolean isCommentAuthor(User user, Comment comment) {
        return comment.getAuthor().getId().equals(user.getId());
    }
}