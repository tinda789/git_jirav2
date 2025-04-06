package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByOwner(User owner);
    List<Workspace> findByMembersContaining(User member);
}