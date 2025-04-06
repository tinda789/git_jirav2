package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "automation_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationRule {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    // Điều kiện kích hoạt rule
    @Enumerated(EnumType.STRING)
    private TriggerEvent triggerEvent;
    
    // Điều kiện để thực hiện action
    @Column(columnDefinition = "TEXT")
    private String conditions; // Có thể lưu dưới dạng JSON
    
    // Hành động sẽ thực hiện
    @Enumerated(EnumType.STRING)
    private ActionType actionType;
    
    // Tham số cho hành động
    @Column(columnDefinition = "TEXT")
    private String actionParameters; // Lưu dưới dạng JSON
    
    @ManyToOne
    @JoinColumn(name = "worklist_id")
    private WorkList workList; // Rule áp dụng cho worklist nào
    
    private Boolean isActive;
}