package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.NotificationService;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ResidentNotificationController {

    private final NotificationService notificationService;
    private final ResidentService residentService;

    @GetMapping("/notifications")
    public String notifications(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Boolean unreadOnly,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            Resident resident = residentService.getResidentByUserId(user.getId());

            List<Notification> notifications;

            if (Boolean.TRUE.equals(unreadOnly)) {
                notifications = notificationService.getUnreadNotifications(user.getId());
            } else {
                notifications = notificationService.getNotificationsByUser(user.getId());
            }

            // Filter by type if specified
            if (type != null) {
                notifications = notifications.stream()
                    .filter(n -> n.getType() == type)
                    .collect(Collectors.toList());
            }

            // Get statistics
            long totalNotifications = notifications.size();
            long unreadCount = notificationService.getUnreadCount(user.getId());
            long todayCount = notifications.stream()
                .filter(n -> n.getSentAt().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();

            model.addAttribute("resident", resident);
            model.addAttribute("notifications", notifications);
            model.addAttribute("types", NotificationType.values());
            model.addAttribute("selectedType", type);
            model.addAttribute("unreadOnly", unreadOnly);
            model.addAttribute("totalNotifications", totalNotifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("todayCount", todayCount);

            return "client/information/notifications";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading notifications: " + e.getMessage());
            model.addAttribute("notifications", new ArrayList<>());
            return "client/information/notifications";
        }
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notification marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error marking notification: " + e.getMessage());
        }
        return "redirect:/client/notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userDetails.getUser();
            notificationService.markAllAsRead(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error marking notifications: " + e.getMessage());
        }
        return "redirect:/client/notifications";
    }
}

