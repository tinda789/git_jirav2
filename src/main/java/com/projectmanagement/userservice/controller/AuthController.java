package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.JwtResponse;
import com.projectmanagement.userservice.dto.LoginRequest;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.RegisterRequest;
import com.projectmanagement.userservice.entity.Role;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Đăng nhập và lấy token
            String jwt = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            
            // Lấy thông tin user
            User user = authService.getCurrentUser();
            
            // Lấy danh sách roles
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            
            // Trả về thông tin token và user
            return ResponseEntity.ok(new JwtResponse(
                    jwt, 
                    user.getId(), 
                    user.getUsername(), 
                    user.getEmail(), 
                    roles));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
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

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(new MessageResponse("Auth API is working!"));
    }
}