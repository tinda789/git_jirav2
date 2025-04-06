package com.projectmanagement.userservice.service;

import com.projectmanagement.userservice.entity.Role;
import com.projectmanagement.userservice.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }
    
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }
    
    public Role updateRole(Role role) {
        return roleRepository.save(role);
    }
    
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
    
    public void initDefaultRoles() {
        if (roleRepository.count() == 0) {
            createRoleIfNotExists("ADMIN");
            createRoleIfNotExists("PROJECT_ADMIN");
            createRoleIfNotExists("PROJECT_MANAGER");
            createRoleIfNotExists("DEVELOPER");
            createRoleIfNotExists("REPORTER");
            createRoleIfNotExists("VIEWER");
        }
    }
    
    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
}