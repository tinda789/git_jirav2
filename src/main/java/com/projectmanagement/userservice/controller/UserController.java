package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.PasswordChangeDto;
import com.projectmanagement.userservice.dto.UserDto;
import com.projectmanagement.userservice.dto.UserProfileDto;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id, principal)")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(convertToDto(currentUser));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileDto profileDto) {
        User currentUser = authService.getCurrentUser();
        
        currentUser.setFullName(profileDto.getFullName());
        if (profileDto.getAvatarUrl() != null) {
            currentUser.setAvatarUrl(profileDto.getAvatarUrl());
        }
        
        User updatedUser = userService.updateUser(currentUser);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeDto passwordDto) {
        User currentUser = authService.getCurrentUser();
        
        if (userService.changePassword(currentUser, passwordDto.getCurrentPassword(), passwordDto.getNewPassword())) {
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Current password is incorrect"));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        return userService.getUserById(id)
                .map(user -> {
                    if (userDto.getFullName() != null) {
                        user.setFullName(userDto.getFullName());
                    }
                    if (userDto.getEmail() != null) {
                        user.setEmail(userDto.getEmail());
                    }
                    // Không cho phép update username
                    
                    User updatedUser = userService.updateUser(user);
                    return ResponseEntity.ok(convertToDto(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userService.getUserById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }
    
    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFullName(user.getFullName());
        userDto.setAvatarUrl(user.getAvatarUrl());
        return userDto;
    }
}