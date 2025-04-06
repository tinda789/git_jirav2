package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.CustomFieldDto;
import com.projectmanagement.userservice.dto.CustomFieldValueDto;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.entity.CustomField;
import com.projectmanagement.userservice.entity.CustomFieldValue;
import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.CustomFieldService;
import com.projectmanagement.userservice.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/custom-fields")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomFieldController {

    @Autowired
    private CustomFieldService customFieldService;
    
    @Autowired
    private IssueService issueService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<CustomFieldDto>> getAllCustomFields() {
        User currentUser = authService.getCurrentUser();
        
        List<CustomField> customFields;
        if (customFieldService.canAccessPremiumFields(currentUser)) {
            // Người dùng Premium có thể xem tất cả các trường
            customFields = customFieldService.getAllCustomFields();
        } else {
            // Người dùng thường chỉ xem được các trường không phải Premium
            customFields = customFieldService.getNonPremiumCustomFields();
        }
        
        List<CustomFieldDto> customFieldDtos = customFields.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customFieldDtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomFieldById(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        
        Optional<CustomField> fieldOptional = customFieldService.getCustomFieldById(id);
        if (!fieldOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        CustomField field = fieldOptional.get();
        // Kiểm tra quyền truy cập trường Premium
        if (field.getIsPremiumOnly() && !customFieldService.canAccessPremiumFields(currentUser)) {
            return ResponseEntity.status(403).body(new MessageResponse("Premium feature required"));
        }
        
        return ResponseEntity.ok(convertToDto(field));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCustomField(@Valid @RequestBody CustomFieldDto fieldDto) {
        CustomField customField = new CustomField();
        customField.setName(fieldDto.getName());
        customField.setType(fieldDto.getType());
        customField.setIsPremiumOnly(fieldDto.getIsPremiumOnly());
        
        CustomField savedField = customFieldService.createCustomField(customField);
        return ResponseEntity.ok(convertToDto(savedField));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCustomField(@PathVariable Long id, @Valid @RequestBody CustomFieldDto fieldDto) {
        Optional<CustomField> fieldOptional = customFieldService.getCustomFieldById(id);
        if (!fieldOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        CustomField field = fieldOptional.get();
        field.setName(fieldDto.getName());
        if (fieldDto.getType() != null) {
            field.setType(fieldDto.getType());
        }
        if (fieldDto.getIsPremiumOnly() != null) {
            field.setIsPremiumOnly(fieldDto.getIsPremiumOnly());
        }
        
        CustomField updatedField = customFieldService.updateCustomField(field);
        return ResponseEntity.ok(convertToDto(updatedField));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCustomField(@PathVariable Long id) {
        if (!customFieldService.getCustomFieldById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        customFieldService.deleteCustomField(id);
        return ResponseEntity.ok(new MessageResponse("Custom field deleted successfully"));
    }
    
    @GetMapping("/issue/{issueId}")
    @PreAuthorize("@securityService.canModifyIssue(#issueId, principal)")
    public ResponseEntity<?> getCustomFieldValuesByIssue(@PathVariable Long issueId) {
        User currentUser = authService.getCurrentUser();
        
        Optional<Issue> issueOptional = issueService.getIssueById(issueId);
        if (!issueOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Issue issue = issueOptional.get();
        List<CustomFieldValue> values = customFieldService.getCustomFieldValuesByIssue(issue);
        
        // Nếu không phải user Premium, lọc bỏ các giá trị của trường Premium
        if (!customFieldService.canAccessPremiumFields(currentUser)) {
            values = values.stream()
                    .filter(value -> !value.getCustomField().getIsPremiumOnly())
                    .collect(Collectors.toList());
        }
        
        List<CustomFieldValueDto> valueDtos = values.stream()
                .map(this::convertValueToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(valueDtos);
    }
    
    @PostMapping("/values")
    @PreAuthorize("@securityService.canModifyIssue(#valueDto.issueId, principal)")
    public ResponseEntity<?> setCustomFieldValue(@Valid @RequestBody CustomFieldValueDto valueDto) {
        User currentUser = authService.getCurrentUser();
        
        Optional<CustomField> customFieldOptional = customFieldService.getCustomFieldById(valueDto.getCustomFieldId());
        if (!customFieldOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Custom field not found"));
        }
        
        CustomField customField = customFieldOptional.get();
        // Kiểm tra quyền truy cập trường Premium
        if (customField.getIsPremiumOnly() && !customFieldService.canAccessPremiumFields(currentUser)) {
            return ResponseEntity.status(403).body(new MessageResponse("Premium feature required"));
        }
        
        Optional<Issue> issueOptional = issueService.getIssueById(valueDto.getIssueId());
        if (!issueOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Issue not found"));
        }
        
        Issue issue = issueOptional.get();
        CustomFieldValue value = customFieldService.setCustomFieldValue(
                issue, customField, valueDto.getValue());
        return ResponseEntity.ok(convertValueToDto(value));
    }
    
    @DeleteMapping("/values/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCustomFieldValue(@PathVariable Long id) {
        customFieldService.deleteCustomFieldValue(id);
        return ResponseEntity.ok(new MessageResponse("Custom field value deleted successfully"));
    }
    
    private CustomFieldDto convertToDto(CustomField customField) {
        CustomFieldDto dto = new CustomFieldDto();
        dto.setId(customField.getId());
        dto.setName(customField.getName());
        dto.setType(customField.getType());
        dto.setIsPremiumOnly(customField.getIsPremiumOnly());
        return dto;
    }
    
    private CustomFieldValueDto convertValueToDto(CustomFieldValue value) {
        CustomFieldValueDto dto = new CustomFieldValueDto();
        dto.setId(value.getId());
        dto.setValue(value.getValue());
        dto.setCustomFieldId(value.getCustomField().getId());
        dto.setIssueId(value.getIssue().getId());
        return dto;
    }
}