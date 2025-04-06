package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private BigDecimal price;
    
    private Integer maxProjects;
    
    private Integer maxUsersPerWorkspace;
    
    private Integer maxStorageGB;
    
    private Boolean hasAdvancedReporting;
    
    private Boolean hasCustomFields;
    
    private Boolean hasPrioritySuppport;
    
    @OneToMany(mappedBy = "subscription")
    private List<User> users;
}