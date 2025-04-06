package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Role;
import com.projectmanagement.userservice.entity.Subscription;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.repository.RoleRepository;
import com.projectmanagement.userservice.repository.SubscriptionRepository;
import com.projectmanagement.userservice.repository.UserRepository;
import com.projectmanagement.userservice.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    
    @Autowired
    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            SubscriptionRepository subscriptionRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }
    
    public User register(String username, String email, String password, String fullName) {
        // Kiểm tra username và email đã tồn tại chưa
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username is already taken!");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Tạo user mới
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRegistrationDate(LocalDateTime.now());
        
        // Thiết lập vai trò mặc định (VIEWER)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("VIEWER")
                .orElseThrow(() -> new RuntimeException("Role not found."));
        roles.add(userRole);
        user.setRoles(roles);
        
        // Thiết lập gói subscription mặc định (FREE)
        Subscription freeSubscription = subscriptionRepository.findByName("Free")
                .orElseThrow(() -> new RuntimeException("Default subscription not found."));
        user.setSubscription(freeSubscription);
        
        // Lưu user
        return userRepository.save(user);
    }
    
    public String login(String username, String password) {
        // Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        
        // Thiết lập thông tin xác thực vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Tạo JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails);
        
        // Cập nhật thời gian đăng nhập của user
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
        }
        
        return jwt;
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}