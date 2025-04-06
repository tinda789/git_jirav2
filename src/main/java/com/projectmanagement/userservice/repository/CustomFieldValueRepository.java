package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.CustomField;
import com.projectmanagement.userservice.entity.CustomFieldValue;
import com.projectmanagement.userservice.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long> {
    List<CustomFieldValue> findByIssue(Issue issue);
    List<CustomFieldValue> findByCustomField(CustomField customField);
    Optional<CustomFieldValue> findByIssueAndCustomField(Issue issue, CustomField customField);
}