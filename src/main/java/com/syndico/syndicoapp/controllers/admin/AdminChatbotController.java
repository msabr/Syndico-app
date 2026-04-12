package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.ChatbotQA;
import com.syndico.syndicoapp.services.ChatbotQAService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/chatbot-config")
@RequiredArgsConstructor
public class AdminChatbotController {

    private final ChatbotQAService chatbotQAService;

    @GetMapping
    public String chatbotConfig(@RequestParam(required = false) String search, Model model) {
        List<ChatbotQA> qas;

        if (search != null && !search.isEmpty()) {
            qas = chatbotQAService.searchQAs(search);
        } else {
            qas = chatbotQAService.getAllQAs();
        }

        ChatbotQAService.ChatbotStatistics stats = chatbotQAService.getStatistics();

        model.addAttribute("qas", qas);
        model.addAttribute("statistics", stats);
        model.addAttribute("searchQuery", search);

        return "admin/chatbot/config";
    }

    @PostMapping("/create")
    public String createChatbotQA(@ModelAttribute ChatbotQA chatbotQA, RedirectAttributes redirectAttributes) {
        try {
            chatbotQAService.createQA(chatbotQA);
            redirectAttributes.addFlashAttribute("successMessage", "Q&A created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/chatbot-config";
    }

    @PostMapping("/update/{id}")
    public String updateChatbotQA(@PathVariable Long id, @ModelAttribute ChatbotQA chatbotQA,
                                  RedirectAttributes redirectAttributes) {
        try {
            chatbotQAService.updateQA(id, chatbotQA);
            redirectAttributes.addFlashAttribute("successMessage", "Q&A updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/chatbot-config";
    }

    @PostMapping("/delete/{id}")
    public String deleteChatbotQA(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            chatbotQAService.deleteQA(id);
            redirectAttributes.addFlashAttribute("successMessage", "Q&A deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/chatbot-config";
    }

    @PostMapping("/{id}/toggle")
    public String toggleChatbotQAStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            chatbotQAService.toggleActiveStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/chatbot-config";
    }
}

