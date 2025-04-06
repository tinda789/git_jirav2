package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.UserRoleDto;
import com.projectmanagement.userservice.entity.Role;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.service.RoleService;
import com.projectmanagement.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    @Autowired
    private RoleService roleService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRoleToUser(@RequestBody UserRoleDto userRoleDto) {
        return userService.getUserById(userRoleDto.getUserId())
                .map(user -> {
                    return roleService.getRoleById(userRoleDto.getRoleId())
                            .map(role -> {
                                Set<Role> roles = user.getRoles();
                                if (roles.stream().anyMatch(r -> r.getId().equals(role.getId()))) {
                                    return ResponseEntity.badRequest()
                                            .body(new MessageResponse("User already has this role"));
                                }
                                
                                roles.add(role);
                                user.setRoles(roles);
                                
                                User updatedUser = userService.updateUser(user);
                                return ResponseEntity.ok(new MessageResponse("Role assigned successfully"));
                            })
                            .orElse(ResponseEntity.badRequest().body(new MessageResponse("Role not found")));
                })
                .orElse(ResponseEntity.badRequest().body(new MessageResponse("User not found")));
    }
    
    @PostMapping("/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeRoleFromUser(@RequestBody UserRoleDto userRoleDto) {
        return userService.getUserById(userRoleDto.getUserId())
                .map(user -> {
                    return roleService.getRoleById(userRoleDto.getRoleId())
                            .map(role -> {
                                Set<Role> roles = user.getRoles();
                                
                                // Kiểm tra người dùng có vai trò này không
                                if (roles.stream().noneMatch(r -> r.getId().equals(role.getId()))) {
                                    return ResponseEntity.badRequest()
                                            .body(new MessageResponse("User does not have this role"));
                                }
                                
                                // Kiểm tra nếu đây là vai trò duy nhất thì không cho phép xóa
                                if (roles.size() == 1) {
                                    return ResponseEntity.badRequest()
                                            .body(new MessageResponse("Cannot remove the only role a user has"));
                                }
                                
                                roles.removeIf(r -> r.getId().equals(role.getId()));
                                user.setRoles(roles);
                                
                                User updatedUser = userService.updateUser(user);
                                return ResponseEntity.ok(new MessageResponse("Role removed successfully"));
                            })
                            .orElse(ResponseEntity.badRequest().body(new MessageResponse("Role not found")));
                })
                .orElse(ResponseEntity.badRequest().body(new MessageResponse("User not found")));
    }
}