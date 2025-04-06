package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.CommentDto;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.entity.Comment;
import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.CommentService;
import com.projectmanagement.userservice.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommentController {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private IssueService issueService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/issue/{issueId}")
    @PreAuthorize("@securityService.canModifyIssue(#issueId, principal)")
    public ResponseEntity<?> getCommentsByIssue(@PathVariable Long issueId) {
        Optional<Issue> issueOptional = issueService.getIssueById(issueId);
        if (!issueOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Issue issue = issueOptional.get();
        List<Comment> comments = commentService.getCommentsByIssue(issue);
        List<CommentDto> commentDtos = comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(commentDtos);
    }
    
    @PostMapping
    @PreAuthorize("@securityService.canModifyIssue(#commentDto.issueId, principal)")
    public ResponseEntity<?> createComment(@Valid @RequestBody CommentDto commentDto) {
        User currentUser = authService.getCurrentUser();
        
        Optional<Issue> issueOptional = issueService.getIssueById(commentDto.getIssueId());
        if (!issueOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Issue not found"));
        }
        
        Issue issue = issueOptional.get();
        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setIssue(issue);
        comment.setAuthor(currentUser);
        
        Comment savedComment = commentService.createComment(comment);
        return ResponseEntity.ok(convertToDto(savedComment));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isCommentAuthor(#id, principal)")
    public ResponseEntity<?> updateComment(@PathVariable Long id, @Valid @RequestBody CommentDto commentDto) {
        Optional<Comment> commentOptional = commentService.getCommentById(id);
        if (!commentOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Comment comment = commentOptional.get();
        comment.setContent(commentDto.getContent());
        Comment updatedComment = commentService.updateComment(comment);
        return ResponseEntity.ok(convertToDto(updatedComment));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isCommentAuthor(#id, principal) or hasRole('ADMIN')")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        if (!commentService.getCommentById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        commentService.deleteComment(id);
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully"));
    }
    
    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedDate(comment.getCreatedDate());
        dto.setIssueId(comment.getIssue().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        return dto;
    }
}