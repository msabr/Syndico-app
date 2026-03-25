package com.syndico.syndicoapp.config;

import com.syndico.syndicoapp.models.Resident;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ResidentService residentService;

    @ModelAttribute("currentResident")
    public Resident getCurrentResident(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getUser().getRole().name().equals("RESIDENT")) {
            try {
                return residentService.getResidentByUserId(userDetails.getId());
            } catch (Exception e) {
                // Resident profile not found, return null
                return null;
            }
        }
        return null;
    }
}

