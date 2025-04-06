package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "work_lists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkList {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime createdDate;
    
    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;
    
    @ManyToOne
    @JoinColumn(name = "lead_id")
    private User lead;
    
    @ManyToMany
    @JoinTable(
        name = "worklist_members",
        joinColumns = @JoinColumn(name = "worklist_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members;
    
    @OneToMany(mappedBy = "workList", cascade = CascadeType.ALL)
    private List<Issue> issues;
    
    @OneToMany(mappedBy = "workList", cascade = CascadeType.ALL)
    private List<Board> boards;
}