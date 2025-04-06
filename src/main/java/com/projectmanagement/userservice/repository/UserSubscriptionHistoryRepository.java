package com.projectmanagement.userservice.repository;

import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.entity.UserSubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscriptionHistoryRepository extends JpaRepository<UserSubscriptionHistory, Long> {
    List<UserSubscriptionHistory> findByUser(User user);
    List<UserSubscriptionHistory> findByUserOrderByStartDateDesc(User user);
}