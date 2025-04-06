package com.projectmanagement.userservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionDto {
    private Long id;
    
    private String name;
    
    private BigDecimal price;
    
    private Integer maxProjects;
    
    private Integer maxUsersPerWorkspace;
    
    private Integer maxStorageGB;
    
    private Boolean hasAdvancedReporting;
    
    private Boolean hasCustomFields;
    
    private Boolean hasPrioritySuppport;
}