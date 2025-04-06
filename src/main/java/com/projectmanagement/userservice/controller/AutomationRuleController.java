package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.AutomationRuleDto;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.entity.AutomationRule;
import com.projectmanagement.userservice.entity.WorkList;
import com.projectmanagement.userservice.service.AutomationService;
import com.projectmanagement.userservice.service.WorkListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/automation-rules")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AutomationRuleController {

    @Autowired
    private AutomationService automationService;
    
    @Autowired
    private WorkListService workListService;
    
    @GetMapping("/worklist/{workListId}")
    @PreAuthorize("@securityService.canManageWorkList(#workListId, principal)")
    public ResponseEntity<?> getRulesByWorkList(@PathVariable Long workListId) {
        Optional<WorkList> workListOptional = workListService.getWorkListById(workListId);
        if (!workListOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkList workList = workListOptional.get();
        List<AutomationRule> rules = automationService.getRulesByWorkList(workList);
        List<AutomationRuleDto> ruleDtos = rules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ruleDtos);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canManageAutomationRule(#id, principal)")
    public ResponseEntity<?> getRuleById(@PathVariable Long id) {
        Optional<AutomationRule> ruleOptional = automationService.getRuleById(id);
        if (!ruleOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        AutomationRule rule = ruleOptional.get();
        return ResponseEntity.ok(convertToDto(rule));
    }
    
    @PostMapping
    @PreAuthorize("@securityService.canManageWorkList(#ruleDto.workListId, principal)")
    public ResponseEntity<?> createRule(@Valid @RequestBody AutomationRuleDto ruleDto) {
        Optional<WorkList> workListOptional = workListService.getWorkListById(ruleDto.getWorkListId());
        if (!workListOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("WorkList not found"));
        }
        
        WorkList workList = workListOptional.get();
        AutomationRule rule = new AutomationRule();
        rule.setName(ruleDto.getName());
        rule.setTriggerEvent(ruleDto.getTriggerEvent());
        rule.setConditions(ruleDto.getConditions());
        rule.setActionType(ruleDto.getActionType());
        rule.setActionParameters(ruleDto.getActionParameters());
        rule.setWorkList(workList);
        rule.setIsActive(true); // Mặc định là active khi tạo mới
        
        AutomationRule savedRule = automationService.createRule(rule);
        return ResponseEntity.ok(convertToDto(savedRule));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canManageAutomationRule(#id, principal)")
    public ResponseEntity<?> updateRule(@PathVariable Long id, @Valid @RequestBody AutomationRuleDto ruleDto) {
        Optional<AutomationRule> ruleOptional = automationService.getRuleById(id);
        if (!ruleOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        AutomationRule rule = ruleOptional.get();
        rule.setName(ruleDto.getName());
        rule.setTriggerEvent(ruleDto.getTriggerEvent());
        rule.setConditions(ruleDto.getConditions());
        rule.setActionType(ruleDto.getActionType());
        rule.setActionParameters(ruleDto.getActionParameters());
        
        if (ruleDto.getIsActive() != null) {
            rule.setIsActive(ruleDto.getIsActive());
        }
        
        AutomationRule updatedRule = automationService.updateRule(rule);
        return ResponseEntity.ok(convertToDto(updatedRule));
    }
    
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("@securityService.canManageAutomationRule(#id, principal)")
    public ResponseEntity<?> toggleRuleActive(@PathVariable Long id) {
        Optional<AutomationRule> ruleOptional = automationService.getRuleById(id);
        if (!ruleOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        AutomationRule rule = ruleOptional.get();
        rule.setIsActive(!rule.getIsActive());
        AutomationRule updatedRule = automationService.updateRule(rule);
        return ResponseEntity.ok(convertToDto(updatedRule));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canManageAutomationRule(#id, principal)")
    public ResponseEntity<?> deleteRule(@PathVariable Long id) {
        if (!automationService.getRuleById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        automationService.deleteRule(id);
        return ResponseEntity.ok(new MessageResponse("Automation rule deleted successfully"));
    }
    
    private AutomationRuleDto convertToDto(AutomationRule rule) {
        AutomationRuleDto dto = new AutomationRuleDto();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setTriggerEvent(rule.getTriggerEvent());
        dto.setConditions(rule.getConditions());
        dto.setActionType(rule.getActionType());
        dto.setActionParameters(rule.getActionParameters());
        dto.setIsActive(rule.getIsActive());
        
        if (rule.getWorkList() != null) {
            dto.setWorkListId(rule.getWorkList().getId());
        }
        
        return dto;
    }
}