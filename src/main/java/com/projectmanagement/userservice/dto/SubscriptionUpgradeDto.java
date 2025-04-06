package com.projectmanagement.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionUpgradeDto {
    @NotNull
    private Long subscriptionId;
    
    private BigDecimal amount;
    
    private String transactionId;
}