package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.dto.NotificationDTO;
import com.syndico.syndicoapp.models.Notification;
import com.syndico.syndicoapp.models.User;
import com.syndico.syndicoapp.models.enums.NotificationType;
import com.syndico.syndicoapp.repositories.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Notification testNotification1;
    private Notification testNotification2;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("testuser@example.com");

        testNotification1 = new Notification();
        testNotification1.setId(1L);
        testNotification1.setTitle("Test Notification 1");
        testNotification1.setMessage("Test message 1");
        testNotification1.setType(NotificationType.INFO);
        testNotification1.setIsRead(false);
        testNotification1.setSentAt(LocalDateTime.now().minusDays(1));
        testNotification1.setUser(testUser);

        testNotification2 = new Notification();
        testNotification2.setId(2L);
        testNotification2.setTitle("Test Notification 2");
        testNotification2.setMessage("Test message 2");
        testNotification2.setType(NotificationType.WARNING);
        testNotification2.setIsRead(true);
        testNotification2.setSentAt(LocalDateTime.now());
        testNotification2.setUser(testUser);
    }

    @Test
    void testFindByUserId() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification1, testNotification2);
        when(notificationRepository.findByUser_IdOrderBySentAtDesc(1L)).thenReturn(notifications);

        // Act
        List<NotificationDTO> result = notificationService.findByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Notification 1", result.get(0).getTitle());
        assertEquals("Test Notification 2", result.get(1).getTitle());
        verify(notificationRepository, times(1)).findByUser_IdOrderBySentAtDesc(1L);
    }

    @Test
    void testFindByUserIdPaged() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification1, testNotification2);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        when(notificationRepository.findByUser_Id(anyLong(), any(Pageable.class))).thenReturn(notificationPage);

        // Act
        Page<NotificationDTO> result = notificationService.findByUserIdPaged(1L, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(notificationRepository, times(1)).findByUser_Id(anyLong(), any(Pageable.class));
    }

    @Test
    void testMarkAsRead_Success() {
        // Arrange
        when(notificationRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(testNotification1));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification1);

        // Act
        notificationService.markAsRead(1L, 1L);

        // Assert
        assertTrue(testNotification1.getIsRead());
        verify(notificationRepository, times(1)).findByIdAndUser_Id(1L, 1L);
        verify(notificationRepository, times(1)).save(testNotification1);
    }

    @Test
    void testMarkAsRead_NotFound() {
        // Arrange
        when(notificationRepository.findByIdAndUser_Id(999L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(999L, 1L);
        });
        verify(notificationRepository, times(1)).findByIdAndUser_Id(999L, 1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testMarkAllAsRead() {
        // Arrange
        when(notificationRepository.markAllAsReadByUserId(1L)).thenReturn(3);

        // Act
        notificationService.markAllAsRead(1L);

        // Assert
        verify(notificationRepository, times(1)).markAllAsReadByUserId(1L);
    }

    @Test
    void testDelete_Success() {
        // Arrange
        when(notificationRepository.findByIdAndUser_Id(1L, 1L)).thenReturn(Optional.of(testNotification1));
        doNothing().when(notificationRepository).delete(any(Notification.class));

        // Act
        notificationService.delete(1L, 1L);

        // Assert
        verify(notificationRepository, times(1)).findByIdAndUser_Id(1L, 1L);
        verify(notificationRepository, times(1)).delete(testNotification1);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(notificationRepository.findByIdAndUser_Id(999L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.delete(999L, 1L);
        });
        verify(notificationRepository, times(1)).findByIdAndUser_Id(999L, 1L);
        verify(notificationRepository, never()).delete(any(Notification.class));
    }

    @Test
    void testCountUnread() {
        // Arrange
        when(notificationRepository.countByUser_IdAndIsRead(1L, false)).thenReturn(5L);

        // Act
        long count = notificationService.countUnread(1L);

        // Assert
        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countByUser_IdAndIsRead(1L, false);
    }
}
