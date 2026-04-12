package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.Resident;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/client/support")
@RequiredArgsConstructor
public class ResidentSupportController {

    private final ResidentService residentService;

    @GetMapping("/help")
    public String helpPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            model.addAttribute("resident", resident);
            return "client/support/help-faq";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading help page: " + e.getMessage());
            return "client/support/help-faq";
        }
    }
}

