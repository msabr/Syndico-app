package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.Message;
import com.syndico.syndicoapp.models.User;
import com.syndico.syndicoapp.repositories.UserRepository;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    @GetMapping
    public String listMessages(Model model,
                               @RequestParam(required = false) String filter,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long adminId = userDetails.getId();
        List<Message> messages;

        if ("unread".equals(filter)) {
            messages = messageService.getInboxMessages(adminId).stream()
                    .filter(m -> !m.getIsRead())
                    .collect(Collectors.toList());
        } else if ("sent".equals(filter)) {
            messages = messageService.getSentMessages(adminId);
        } else {
            messages = messageService.getInboxMessages(adminId);
        }

        // Get sender/receiver information
        messages.forEach(message -> {
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            User receiver = userRepository.findById(message.getReceiverId()).orElse(null);
            model.addAttribute("sender_" + message.getId(), sender);
            model.addAttribute("receiver_" + message.getId(), receiver);
        });

        long unreadCount = messageService.getUnreadMessagesCount(adminId);

        model.addAttribute("messages", messages);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("filter", filter);

        return "admin/messages/list";
    }

    @GetMapping("/{id}")
    public String viewMessage(@PathVariable Long id,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Message message = messageService.getMessageById(id);

        // Mark as read if current user is the receiver
        if (message.getReceiverId().equals(userDetails.getId()) && !message.getIsRead()) {
            messageService.markAsRead(id);
        }

        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        User receiver = userRepository.findById(message.getReceiverId()).orElse(null);

        model.addAttribute("message", message);
        model.addAttribute("sender", sender);
        model.addAttribute("receiver", receiver);

        return "admin/messages/details";
    }

    @GetMapping("/compose")
    public String composeMessageForm(Model model) {
        List<User> allResidents = userRepository.findByRole(
                com.syndico.syndicoapp.models.enums.UserRole.RESIDENT
        );

        model.addAttribute("residents", allResidents);
        return "admin/messages/compose";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam("recipientType") String recipientType,
                              @RequestParam(value = "recipientIds", required = false) String recipientIds,
                              @RequestParam("subject") String subject,
                              @RequestParam("content") String content,
                              RedirectAttributes redirectAttributes,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long senderId = userDetails.getId();

            if ("all".equals(recipientType)) {
                // Send to all residents
                messageService.sendMessageToAllResidents(senderId, subject, content);
                redirectAttributes.addFlashAttribute("success", "Message sent to all residents successfully");
            } else if ("individual".equals(recipientType) && recipientIds != null && !recipientIds.isEmpty()) {
                // Send to selected residents
                List<Long> receiverIds = Arrays.stream(recipientIds.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                messageService.sendBroadcastMessage(senderId, receiverIds, subject, content);
                redirectAttributes.addFlashAttribute("success",
                        "Message sent to " + receiverIds.size() + " resident(s) successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Please select at least one recipient");
                return "redirect:/admin/messages/compose";
            }

            return "redirect:/admin/messages?filter=sent";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send message: " + e.getMessage());
            return "redirect:/admin/messages/compose";
        }
    }

    @PostMapping("/reply/{id}")
    public String replyToMessage(@PathVariable Long id,
                                 @RequestParam("content") String content,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Message originalMessage = messageService.getMessageById(id);
            Long senderId = userDetails.getId();
            Long receiverId = originalMessage.getSenderId(); // Reply to sender
            String subject = "Re: " + originalMessage.getSubject();

            messageService.sendMessage(senderId, receiverId, subject, content);

            redirectAttributes.addFlashAttribute("success", "Reply sent successfully");
            return "redirect:/admin/messages/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send reply: " + e.getMessage());
            return "redirect:/admin/messages/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            messageService.deleteMessage(id);
            redirectAttributes.addFlashAttribute("success", "Message deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete message: " + e.getMessage());
        }
        return "redirect:/admin/messages";
    }

    @PostMapping("/mark-read/{id}")
    public String markAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            messageService.markAsRead(id);
            redirectAttributes.addFlashAttribute("success", "Message marked as read");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update message");
        }
        return "redirect:/admin/messages";
    }

    @PostMapping("/mark-unread/{id}")
    public String markAsUnread(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            messageService.markAsUnread(id);
            redirectAttributes.addFlashAttribute("success", "Message marked as unread");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update message");
        }
        return "redirect:/admin/messages";
    }
}

