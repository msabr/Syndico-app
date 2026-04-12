package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String notifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getId();
        model.addAttribute("notifications", notificationService.findByUserId(userId));
        model.addAttribute("unreadCount", notificationService.countUnread(userId));
        model.addAttribute("activePage", "notifications");
        return "admin/notifications";
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            notificationService.markAsRead(id, userDetails.getId());
            return ResponseEntity.ok(Map.of("status", "success", "message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/read-all")
    public String markAllNotificationsAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAllAsRead(userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "All notifications have been marked as read");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notifications";
    }

    @PostMapping("/{id}/delete")
    public String deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            notificationService.delete(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Notification deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notifications";
    }
}

