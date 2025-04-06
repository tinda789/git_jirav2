package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sprints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sprint {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String goal;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Enumerated(EnumType.STRING)
    private SprintStatus status;
    
    @ManyToOne
    @JoinColumn(name = "worklist_id", nullable = false)
    private WorkList workList;
    
    @OneToMany(mappedBy = "sprint")
    private List<Issue> issues;
}