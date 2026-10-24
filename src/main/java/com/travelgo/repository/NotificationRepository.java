package com.travelgo.repository;

import com.travelgo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for User Notifications (alerts for booking confirmations, visa status updates, payments).
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Retrieves all notifications for a specific user, sorted from newest to oldest.
     * 
     * [SPRING DATA JPA MAGIC]:
     * - Generates: "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC"
     */
    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
