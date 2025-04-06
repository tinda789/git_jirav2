package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Label;
import com.projectmanagement.userservice.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LabelService {
    
    private final LabelRepository labelRepository;
    
    @Autowired
    public LabelService(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }
    
    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }
    
    public Optional<Label> getLabelById(Long id) {
        return labelRepository.findById(id);
    }
    
    public Label createLabel(Label label) {
        return labelRepository.save(label);
    }
    
    public Label updateLabel(Label label) {
        return labelRepository.save(label);
    }
    
    public void deleteLabel(Long id) {
        labelRepository.deleteById(id);
    }
    
    public boolean existsByName(String name) {
        return labelRepository.existsByName(name);
    }
}