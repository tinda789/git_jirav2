package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    private LocalDateTime createdDate;
    
    @ManyToOne
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
    
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
}
