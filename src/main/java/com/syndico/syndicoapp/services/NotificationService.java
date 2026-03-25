package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.dto.NotificationDTO;
import com.syndico.syndicoapp.models.Notification;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {

    List<NotificationDTO> findByUserId(Long userId);
    Page<NotificationDTO> findByUserIdPaged(Long userId, int page, int size);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    void delete(Long notificationId, Long userId);
    long countUnread(Long userId);

    // Additional methods for client controller
    List<Notification> getNotificationsByUser(Long userId);
    List<Notification> getUnreadNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId);
}
