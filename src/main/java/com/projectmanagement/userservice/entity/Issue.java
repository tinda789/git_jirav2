package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "issues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private IssueType type;
    
    @Enumerated(EnumType.STRING)
    private IssuePriority priority;
    
    @Enumerated(EnumType.STRING)
    private IssueStatus status;
    
    private LocalDateTime createdDate;
    
    private LocalDateTime dueDate;
    
    private Long estimatedHours;
    
    private Integer storyPoints;
    
    @ManyToOne
    @JoinColumn(name = "worklist_id", nullable = false)
    private WorkList workList;
    
    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;
    
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;
    
    @OneToMany(mappedBy = "parentIssue", cascade = CascadeType.ALL)
    private List<Issue> subIssues;
    
    @ManyToOne
    @JoinColumn(name = "parent_issue_id")
    private Issue parentIssue;
    
    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;
    
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL)
    private List<Comment> comments;
    
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL)
    private List<Attachment> attachments;
    
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL)
    private List<WorkLog> workLogs;
    
    @ManyToMany
    @JoinTable(
        name = "issue_labels",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private List<Label> labels;
    
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL)
    private List<CustomFieldValue> customFieldValues;
}