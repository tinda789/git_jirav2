package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.CustomField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomFieldRepository extends JpaRepository<CustomField, Long> {
    List<CustomField> findByIsPremiumOnly(Boolean isPremiumOnly);
}