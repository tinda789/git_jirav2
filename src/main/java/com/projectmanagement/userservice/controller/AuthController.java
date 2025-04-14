package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.JwtResponse;
import com.projectmanagement.userservice.dto.LoginRequest;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.RegisterRequest;
import com.projectmanagement.userservice.entity.Role;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Đăng nhập với username: {}", loginRequest.getUsername());
            
            // Đăng nhập và lấy token
            String jwt = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            logger.info("Tạo JWT token thành công: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
            
            // Lấy thông tin user
            User user = authService.getCurrentUser();
            logger.info("Lấy thông tin user thành công: {}", user.getUsername());
            
            // Lấy danh sách roles
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            
            // Trả về thông tin token và user
            JwtResponse response = new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
            logger.info("Trả về response thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Lỗi đăng nhập: {}", e.getMessage(), e);
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Đăng ký với username: {}", registerRequest.getUsername());
            
            // Kiểm tra username đã tồn tại chưa
            if (!authService.isUsernameAvailable(registerRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Username is already taken!"));
            }

            // Kiểm tra email đã tồn tại chưa
            if (!authService.isEmailAvailable(registerRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Email is already in use!"));
            }

            // Tạo user mới
            User user = authService.register(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getFullName());
            
            logger.info("Đăng ký thành công cho user: {}", user.getUsername());
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            logger.error("Lỗi đăng ký: {}", e.getMessage(), e);
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        logger.info("Test endpoint called");
        return ResponseEntity.ok(new MessageResponse("Auth API is working!"));
    }
}