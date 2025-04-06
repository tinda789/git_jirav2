package com.projectmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscription_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionHistory {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private BigDecimal paidAmount;
    
    private String transactionId;
}