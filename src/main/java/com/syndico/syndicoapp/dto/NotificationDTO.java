package com.syndico.syndicoapp.dto;

import com.syndico.syndicoapp.models.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private LocalDateTime sentAt;
    private Boolean isRead;
    private Long userId;
}
