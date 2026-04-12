package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.ReclamationService;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ResidentComplaintController {

    private final ReclamationService reclamationService;
    private final ResidentService residentService;

    @GetMapping("/my-complaints")
    public String myComplaints(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "client/services/my-complaints";
            }

            // Get all complaints for the resident
            List<Reclamation> complaints = reclamationService.getReclamationsByResident(resident.getId());

            // Calculate statistics
            long totalComplaints = complaints.size();
            long pendingComplaints = complaints.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.NOUVELLE)
                .count();
            long inProgressComplaints = complaints.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.EN_COURS)
                .count();
            long resolvedComplaints = complaints.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.RESOLUE)
                .count();

            // Create statistics map
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", totalComplaints);
            statistics.put("pending", pendingComplaints);
            statistics.put("inProgress", inProgressComplaints);
            statistics.put("resolved", resolvedComplaints);

            model.addAttribute("complaints", complaints);
            model.addAttribute("statistics", statistics);
            model.addAttribute("resident", resident);

            return "client/services/my-complaints";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading complaints: " + e.getMessage());
            model.addAttribute("complaints", new ArrayList<>());
            return "client/services/my-complaints";
        }
    }

    @GetMapping("/new-complaint")
    public String newComplaintPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "redirect:/client/my-complaints";
            }

            model.addAttribute("resident", resident);
            model.addAttribute("categories", ReclamationCategory.values());

            return "client/services/new-complaint";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/client/my-complaints";
        }
    }

    @PostMapping("/new-complaint")
    public String submitComplaint(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam(required = false) String priority,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Resident profile not found.");
                return "redirect:/client/my-complaints";
            }

            // Create new complaint
            Reclamation complaint = new Reclamation();
            complaint.setResident(resident);
            complaint.setTitle(title);
            complaint.setDescription(description);

            // Set category
            try {
                complaint.setCategory(ReclamationCategory.valueOf(category));
            } catch (IllegalArgumentException e) {
                complaint.setCategory(ReclamationCategory.AUTRE);
            }

            // Set priority
            if (priority != null && !priority.isEmpty()) {
                try {
                    complaint.setPriority(Priority.valueOf(priority));
                } catch (IllegalArgumentException e) {
                    complaint.setPriority(Priority.MOYENNE);
                }
            } else {
                complaint.setPriority(Priority.MOYENNE);
            }

            complaint.setStatus(ReclamationStatus.NOUVELLE);

            // Save complaint
            reclamationService.createReclamation(complaint);

            redirectAttributes.addFlashAttribute("successMessage", "Complaint submitted successfully!");
            return "redirect:/client/my-complaints";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting complaint: " + e.getMessage());
            return "redirect:/client/new-complaint";
        }
    }
}

