package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custom_field_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFieldValue {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String value;
    
    @ManyToOne
    @JoinColumn(name = "custom_field_id", nullable = false)
    private CustomField customField;
    
    @ManyToOne
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
}