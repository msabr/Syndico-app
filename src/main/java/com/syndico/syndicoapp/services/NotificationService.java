package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.dto.NotificationDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {

    List<NotificationDTO> findByUserId(Long userId);
    Page<NotificationDTO> findByUserIdPaged(Long userId, int page, int size);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    void delete(Long notificationId, Long userId);
    long countUnread(Long userId);
}
