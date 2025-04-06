package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.CustomField;
import com.projectmanagement.userservice.entity.CustomFieldValue;
import com.projectmanagement.userservice.entity.Issue;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.repository.CustomFieldRepository;
import com.projectmanagement.userservice.repository.CustomFieldValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomFieldService {
    
    private final CustomFieldRepository customFieldRepository;
    private final CustomFieldValueRepository customFieldValueRepository;
    
    @Autowired
    public CustomFieldService(CustomFieldRepository customFieldRepository, 
                            CustomFieldValueRepository customFieldValueRepository) {
        this.customFieldRepository = customFieldRepository;
        this.customFieldValueRepository = customFieldValueRepository;
    }
    
    public List<CustomField> getAllCustomFields() {
        return customFieldRepository.findAll();
    }
    
    public Optional<CustomField> getCustomFieldById(Long id) {
        return customFieldRepository.findById(id);
    }
    
    public List<CustomField> getNonPremiumCustomFields() {
        return customFieldRepository.findByIsPremiumOnly(false);
    }
    
    public List<CustomField> getPremiumCustomFields() {
        return customFieldRepository.findByIsPremiumOnly(true);
    }
    
    public CustomField createCustomField(CustomField customField) {
        return customFieldRepository.save(customField);
    }
    
    public CustomField updateCustomField(CustomField customField) {
        return customFieldRepository.save(customField);
    }
    
    public void deleteCustomField(Long id) {
        customFieldRepository.deleteById(id);
    }
    
    public List<CustomFieldValue> getCustomFieldValuesByIssue(Issue issue) {
        return customFieldValueRepository.findByIssue(issue);
    }
    
    public Optional<CustomFieldValue> getCustomFieldValue(Issue issue, CustomField customField) {
        return customFieldValueRepository.findByIssueAndCustomField(issue, customField);
    }
    
    public CustomFieldValue setCustomFieldValue(Issue issue, CustomField customField, String value) {
        Optional<CustomFieldValue> existingValue = customFieldValueRepository.findByIssueAndCustomField(issue, customField);
        
        if (existingValue.isPresent()) {
            CustomFieldValue fieldValue = existingValue.get();
            fieldValue.setValue(value);
            return customFieldValueRepository.save(fieldValue);
        } else {
            CustomFieldValue newValue = new CustomFieldValue();
            newValue.setIssue(issue);
            newValue.setCustomField(customField);
            newValue.setValue(value);
            return customFieldValueRepository.save(newValue);
        }
    }
    
    public void deleteCustomFieldValue(Long id) {
        customFieldValueRepository.deleteById(id);
    }
    
    public boolean canAccessPremiumFields(User user) {
        return user.getSubscription() != null && user.getSubscription().getHasCustomFields();
    }
}