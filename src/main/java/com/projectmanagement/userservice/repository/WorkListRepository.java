package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.WorkList;
import com.projectmanagement.userservice.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkListRepository extends JpaRepository<WorkList, Long> {
    List<WorkList> findByWorkspace(Workspace workspace);
    Optional<WorkList> findByCode(String code);
    boolean existsByCode(String code);
}