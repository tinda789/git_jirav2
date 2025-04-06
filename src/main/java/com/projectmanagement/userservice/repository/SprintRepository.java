package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.Sprint;
import com.projectmanagement.userservice.entity.SprintStatus;
import com.projectmanagement.userservice.entity.WorkList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByWorkList(WorkList workList);
    List<Sprint> findByWorkListAndStatus(WorkList workList, SprintStatus status);
}