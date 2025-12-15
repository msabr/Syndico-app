package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.dto.NotificationDTO;
import com.syndico.syndicoapp.models.Notification;
import com.syndico.syndicoapp.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationDTO> findByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByUser_IdOrderBySentAtDesc(userId);
        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NotificationDTO> findByUserIdPaged(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<Notification> notifications = notificationRepository.findByUser_Id(userId, pageable);
        return notifications.map(this::toDTO);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or access denied"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void delete(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or access denied"));

        notificationRepository.delete(notification);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUser_IdAndIsRead(userId, false);
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .sentAt(notification.getSentAt())
                .isRead(notification.getIsRead())
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .build();
    }
}
