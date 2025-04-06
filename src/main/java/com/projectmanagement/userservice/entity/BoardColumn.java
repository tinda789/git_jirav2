package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "board_columns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumn {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private Integer position;
    
    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;
    
    @OneToMany
    @JoinTable(
        name = "column_issues",
        joinColumns = @JoinColumn(name = "column_id"),
        inverseJoinColumns = @JoinColumn(name = "issue_id")
    )
    private List<Issue> issues;
}