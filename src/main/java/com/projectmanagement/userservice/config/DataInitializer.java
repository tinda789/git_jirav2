package com.projectmanagement.userservice.config;

import com.projectmanagement.userservice.entity.Role;
import com.projectmanagement.userservice.entity.Subscription;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.repository.RoleRepository;
import com.projectmanagement.userservice.repository.SubscriptionRepository;
import com.projectmanagement.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Khởi tạo các gói subscription
        createSubscriptionsIfNotExists();
        
        // Khởi tạo các vai trò
        createRolesIfNotExists();
        
        // Tạo admin user
        createAdminUserIfNotExists();
    }

    private void createSubscriptionsIfNotExists() {
        if (subscriptionRepository.count() == 0) {
            // Gói Free
            Subscription free = new Subscription();
            free.setName("Free");
            free.setPrice(BigDecimal.ZERO);
            free.setMaxProjects(3);
            free.setMaxUsersPerWorkspace(5);
            free.setMaxStorageGB(1);
            free.setHasAdvancedReporting(false);
            free.setHasCustomFields(false);
            free.setHasPrioritySuppport(false);
            subscriptionRepository.save(free);
            
            // Gói Basic
            Subscription basic = new Subscription();
            basic.setName("Basic");
            basic.setPrice(new BigDecimal("9.99"));
            basic.setMaxProjects(10);
            basic.setMaxUsersPerWorkspace(15);
            basic.setMaxStorageGB(5);
            basic.setHasAdvancedReporting(false);
            basic.setHasCustomFields(true);
            basic.setHasPrioritySuppport(false);
            subscriptionRepository.save(basic);
            
            // Gói Pro
            Subscription pro = new Subscription();
            pro.setName("Pro");
            pro.setPrice(new BigDecimal("19.99"));
            pro.setMaxProjects(50);
            pro.setMaxUsersPerWorkspace(50);
            pro.setMaxStorageGB(20);
            pro.setHasAdvancedReporting(true);
            pro.setHasCustomFields(true);
            pro.setHasPrioritySuppport(true);
            subscriptionRepository.save(pro);
        }
    }

    private void createRolesIfNotExists() {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);
            
            Role pmRole = new Role();
            pmRole.setName("PROJECT_MANAGER");
            roleRepository.save(pmRole);
            
            Role devRole = new Role();
            devRole.setName("DEVELOPER");
            roleRepository.save(devRole);
            
            Role viewerRole = new Role();
            viewerRole.setName("VIEWER");
            roleRepository.save(viewerRole);
        }
    }

    private void createAdminUserIfNotExists() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setFullName("Administrator");
            admin.setRegistrationDate(LocalDateTime.now());
            
            // Thiết lập vai trò ADMIN
            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
            roles.add(adminRole);
            admin.setRoles(roles);
            
            // Thiết lập gói Pro
            Subscription proSubscription = subscriptionRepository.findByName("Pro")
                    .orElseThrow(() -> new RuntimeException("Subscription Pro not found"));
            admin.setSubscription(proSubscription);
            
            userRepository.save(admin);
        }
    }
}