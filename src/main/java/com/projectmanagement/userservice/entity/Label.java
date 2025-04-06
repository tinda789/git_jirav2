package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "labels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Label {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String color;
    
    @ManyToMany(mappedBy = "labels")
    private List<Issue> issues;
}
