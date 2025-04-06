package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.Attachment;
import com.projectmanagement.userservice.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByIssue(Issue issue);
}