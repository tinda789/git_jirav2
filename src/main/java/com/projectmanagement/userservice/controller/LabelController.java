package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.LabelDto;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.entity.Label;
import com.projectmanagement.userservice.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/labels")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LabelController {

    @Autowired
    private LabelService labelService;
    
    @GetMapping
    public ResponseEntity<List<LabelDto>> getAllLabels() {
        List<Label> labels = labelService.getAllLabels();
        List<LabelDto> labelDtos = labels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(labelDtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getLabelById(@PathVariable Long id) {
        return labelService.getLabelById(id)
                .map(label -> ResponseEntity.ok(convertToDto(label)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROJECT_ADMIN')")
    public ResponseEntity<?> createLabel(@Valid @RequestBody LabelDto labelDto) {
        if (labelService.existsByName(labelDto.getName())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Label name already exists"));
        }
        
        Label label = new Label();
        label.setName(labelDto.getName());
        label.setColor(labelDto.getColor());
        
        Label savedLabel = labelService.createLabel(label);
        return ResponseEntity.ok(convertToDto(savedLabel));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROJECT_ADMIN')")
    public ResponseEntity<?> updateLabel(@PathVariable Long id, @Valid @RequestBody LabelDto labelDto) {
        return labelService.getLabelById(id)
                .map(label -> {
                    // Kiểm tra nếu tên bị thay đổi và đã tồn tại
                    if (!label.getName().equals(labelDto.getName()) && labelService.existsByName(labelDto.getName())) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Label name already exists"));
                    }
                    
                    label.setName(labelDto.getName());
                    label.setColor(labelDto.getColor());
                    Label updatedLabel = labelService.updateLabel(label);
                    return ResponseEntity.ok(convertToDto(updatedLabel));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROJECT_ADMIN')")
    public ResponseEntity<?> deleteLabel(@PathVariable Long id) {
        if (!labelService.getLabelById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        labelService.deleteLabel(id);
        return ResponseEntity.ok(new MessageResponse("Label deleted successfully"));
    }
    
    private LabelDto convertToDto(Label label) {
        LabelDto dto = new LabelDto();
        dto.setId(label.getId());
        dto.setName(label.getName());
        dto.setColor(label.getColor());
        return dto;
    }
}