package com.projectmanagement.userservice.dto;

import com.projectmanagement.userservice.entity.ActionType;
import com.projectmanagement.userservice.entity.TriggerEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AutomationRuleDto {
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotNull
    private TriggerEvent triggerEvent;
    
    private String conditions;
    
    @NotNull
    private ActionType actionType;
    
    private String actionParameters;
    
    private Boolean isActive;
    
    @NotNull
    private Long workListId;
}