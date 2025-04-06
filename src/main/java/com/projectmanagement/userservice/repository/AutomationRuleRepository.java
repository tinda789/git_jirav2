package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.AutomationRule;
import com.projectmanagement.userservice.entity.TriggerEvent;
import com.projectmanagement.userservice.entity.WorkList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {
    List<AutomationRule> findByWorkListAndIsActiveTrue(WorkList workList);
    List<AutomationRule> findByTriggerEventAndIsActiveTrue(TriggerEvent event);
}