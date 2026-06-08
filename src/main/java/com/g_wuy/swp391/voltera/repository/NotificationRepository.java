package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Notification;
import com.g_wuy.swp391.voltera.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUseridOrderByCreatedAtDesc(User user);
}
