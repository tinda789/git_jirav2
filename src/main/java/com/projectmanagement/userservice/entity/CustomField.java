package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "custom_fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomField {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    private CustomFieldType type;
    
    private Boolean isPremiumOnly;
    
    @OneToMany(mappedBy = "customField", cascade = CascadeType.ALL)
    private List<CustomFieldValue> values;
}