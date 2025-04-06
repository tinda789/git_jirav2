package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.SubscriptionDto;
import com.projectmanagement.userservice.dto.SubscriptionUpgradeDto;
import com.projectmanagement.userservice.entity.Subscription;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.UserSubscriptionHistory;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subscriptions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        List<SubscriptionDto> subscriptionDtos = subscriptions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subscriptionDtos);
    }
    
    @GetMapping("/current")
    public ResponseEntity<SubscriptionDto> getCurrentSubscription() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(convertToDto(currentUser.getSubscription()));
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<UserSubscriptionHistory>> getSubscriptionHistory() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(subscriptionService.getUserSubscriptionHistory(currentUser));
    }
    
    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradeSubscription(@Valid @RequestBody SubscriptionUpgradeDto upgradeDto) {
        User currentUser = authService.getCurrentUser();
        
        return subscriptionService.getSubscriptionById(upgradeDto.getSubscriptionId())
                .map(newSubscription -> {
                    // Kiểm tra nếu đang cố gắng nâng cấp lên gói hiện tại
                    if (currentUser.getSubscription().getId().equals(newSubscription.getId())) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("You are already subscribed to this plan"));
                    }
                    
                    // Kiểm tra nếu đang hạ cấp từ gói cao xuống gói thấp hơn
                    if (currentUser.getSubscription().getPrice().compareTo(newSubscription.getPrice()) > 0) {
                        // Có thể thêm logic xác nhận hạ cấp ở đây
                    }
                    
                    User updatedUser = subscriptionService.upgradeUserSubscription(
                            currentUser, 
                            newSubscription, 
                            upgradeDto.getTransactionId(), 
                            upgradeDto.getAmount());
                    
                    return ResponseEntity.ok(convertToDto(updatedUser.getSubscription()));
                })
                .orElse(ResponseEntity.badRequest().body(new MessageResponse("Subscription not found")));
    }
    
    private SubscriptionDto convertToDto(Subscription subscription) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(subscription.getId());
        dto.setName(subscription.getName());
        dto.setPrice(subscription.getPrice());
        dto.setMaxProjects(subscription.getMaxProjects());
        dto.setMaxUsersPerWorkspace(subscription.getMaxUsersPerWorkspace());
        dto.setMaxStorageGB(subscription.getMaxStorageGB());
        dto.setHasAdvancedReporting(subscription.getHasAdvancedReporting());
        dto.setHasCustomFields(subscription.getHasCustomFields());
        dto.setHasPrioritySuppport(subscription.getHasPrioritySuppport());
        return dto;
    }
}