package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    boolean existsByName(String name);
}