package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Subscription;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.UserSubscriptionHistory;
import com.projectmanagement.userservice.repository.SubscriptionRepository;
import com.projectmanagement.userservice.repository.UserRepository;
import com.projectmanagement.userservice.repository.UserSubscriptionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionHistoryRepository historyRepository;
    
    @Autowired
    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            UserSubscriptionHistoryRepository historyRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
    }
    
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }
    
    public Optional<Subscription> getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id);
    }
    
    public Optional<Subscription> getSubscriptionByName(String name) {
        return subscriptionRepository.findByName(name);
    }
    
    @Transactional
    public User upgradeUserSubscription(User user, Subscription newSubscription, String transactionId, BigDecimal amount) {
        // Lưu thông tin subscription cũ
        Subscription oldSubscription = user.getSubscription();
        
        // Tạo lịch sử thanh toán
        UserSubscriptionHistory history = new UserSubscriptionHistory();
        history.setUser(user);
        history.setSubscription(newSubscription);
        history.setStartDate(LocalDateTime.now());
        history.setPaidAmount(amount);
        history.setTransactionId(transactionId);
        
        // Nếu đang từ gói trả phí xuống gói free
        if (oldSubscription != null && oldSubscription.getPrice().compareTo(BigDecimal.ZERO) > 0 
                && newSubscription.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            // Thêm ngày kết thúc cho gói cũ
            List<UserSubscriptionHistory> histories = historyRepository.findByUserOrderByStartDateDesc(user);
            if (!histories.isEmpty()) {
                UserSubscriptionHistory lastHistory = histories.get(0);
                lastHistory.setEndDate(LocalDateTime.now());
                historyRepository.save(lastHistory);
            }
        }
        
        // Lưu lịch sử mới
        historyRepository.save(history);
        
        // Cập nhật subscription cho user
        user.setSubscription(newSubscription);
        return userRepository.save(user);
    }
    
    public List<UserSubscriptionHistory> getUserSubscriptionHistory(User user) {
        return historyRepository.findByUserOrderByStartDateDesc(user);
    }
    
    public void initDefaultSubscriptions() {
        if (subscriptionRepository.count() == 0) {
            // Gói Free
            createSubscriptionIfNotExists("Free", BigDecimal.ZERO, 3, 5, 1, false, false, false);
            
            // Gói Basic
            createSubscriptionIfNotExists("Basic", new BigDecimal("9.99"), 10, 15, 5, false, true, false);
            
            // Gói Pro
            createSubscriptionIfNotExists("Pro", new BigDecimal("19.99"), 50, 50, 20, true, true, true);
            
            // Gói Enterprise
            createSubscriptionIfNotExists("Enterprise", new BigDecimal("49.99"), -1, -1, 100, true, true, true);
        }
    }
    
    private void createSubscriptionIfNotExists(String name, BigDecimal price, 
                                             Integer maxProjects, Integer maxUsers, 
                                             Integer maxStorage, Boolean reporting, 
                                             Boolean customFields, Boolean support) {
        if (subscriptionRepository.findByName(name).isEmpty()) {
            Subscription subscription = new Subscription();
            subscription.setName(name);
            subscription.setPrice(price);
            subscription.setMaxProjects(maxProjects);
            subscription.setMaxUsersPerWorkspace(maxUsers);
            subscription.setMaxStorageGB(maxStorage);
            subscription.setHasAdvancedReporting(reporting);
            subscription.setHasCustomFields(customFields);
            subscription.setHasPrioritySuppport(support);
            subscriptionRepository.save(subscription);
        }
    }
}