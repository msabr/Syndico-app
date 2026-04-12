package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.MessageService;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ResidentMessageController {

    private final MessageService messageService;
    private final ResidentService residentService;

    @GetMapping("/messages")
    public String messages(
            @RequestParam(required = false) String search,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            User user = userDetails.getUser();

            List<Message> receivedMessages;
            if (search != null && !search.isEmpty()) {
                receivedMessages = messageService.searchMessages(user.getId(), search);
            } else {
                receivedMessages = messageService.getReceivedMessages(user.getId());
            }

            List<Message> sentMessages = messageService.getSentMessages(user.getId());
            long unreadCount = messageService.getUnreadCount(user.getId());

            model.addAttribute("resident", resident);
            model.addAttribute("receivedMessages", receivedMessages);
            model.addAttribute("sentMessages", sentMessages);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("searchQuery", search);

            return "client/information/messages";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading messages: " + e.getMessage());
            model.addAttribute("receivedMessages", new ArrayList<>());
            model.addAttribute("sentMessages", new ArrayList<>());
            return "client/information/messages";
        }
    }

    @PostMapping("/messages/send")
    public String sendMessage(
            @RequestParam Long receiverId,
            @RequestParam String subject,
            @RequestParam String content,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User sender = userDetails.getUser();
            messageService.sendMessage(sender.getId(), receiverId, subject, content);
            redirectAttributes.addFlashAttribute("successMessage", "Message sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error sending message: " + e.getMessage());
        }
        return "redirect:/client/messages";
    }

    @PostMapping("/messages/{id}/read")
    public String markMessageAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            messageService.markAsRead(id);
            redirectAttributes.addFlashAttribute("successMessage", "Message marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error marking message: " + e.getMessage());
        }
        return "redirect:/client/messages";
    }
}

