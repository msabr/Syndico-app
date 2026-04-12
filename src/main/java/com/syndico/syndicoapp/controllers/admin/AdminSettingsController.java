package com.syndico.syndicoapp.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    @GetMapping
    public String settings(Model model) {
        // Add any settings data you need
        model.addAttribute("appVersion", "1.0.0");
        model.addAttribute("appName", "Syndico");

        return "admin/settings/index";
    }

    @PostMapping("/update")
    public String updateSettings(@RequestParam Map<String, String> settings,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Save settings logic here
            redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}
