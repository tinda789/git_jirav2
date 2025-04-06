package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkLog {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long timeSpentSeconds;
    
    private String description;
    
    private LocalDateTime startTime;
    
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}