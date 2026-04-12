package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.Priority;
import com.syndico.syndicoapp.models.enums.ReclamationCategory;
import com.syndico.syndicoapp.models.enums.ReclamationStatus;
import com.syndico.syndicoapp.repositories.PrestataireRepository;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.CommentService;
import com.syndico.syndicoapp.services.ReclamationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final ReclamationService reclamationService;
    private final CommentService commentService;
    private final PrestataireRepository prestataireRepository;

    @GetMapping
    public String listComplaints(
            @RequestParam(required = false) ReclamationStatus status,
            @RequestParam(required = false) ReclamationCategory category,
            @RequestParam(required = false) Priority priority,
            Model model) {

        List<Reclamation> reclamations;

        if (status != null) {
            reclamations = reclamationService.getReclamationsByStatus(status);
        } else if (category != null) {
            reclamations = reclamationService.getReclamationsByCategory(category);
        } else if (priority != null) {
            reclamations = reclamationService.getReclamationsByPriority(priority);
        } else {
            reclamations = reclamationService.getAllReclamations();
        }

        // Get statistics
        ReclamationService.ReclamationStatistics stats = reclamationService.getReclamationStatistics();

        model.addAttribute("reclamations", reclamations);
        model.addAttribute("statistics", stats);
        model.addAttribute("reclamationStatuses", ReclamationStatus.values());
        model.addAttribute("reclamationCategories", ReclamationCategory.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedPriority", priority);

        return "admin/complaints/list";
    }

    @GetMapping("/{id}")
    public String viewComplaintDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Reclamation reclamation = reclamationService.getReclamationById(id)
                    .orElseThrow(() -> new RuntimeException("Reclamation not found"));

            List<Comment> comments = commentService.getCommentsByReclamation(id);
            List<Prestataire> prestataires = prestataireRepository.findAll();

            model.addAttribute("reclamation", reclamation);
            model.addAttribute("comments", comments);
            model.addAttribute("prestataires", prestataires);
            model.addAttribute("reclamationStatuses", ReclamationStatus.values());

            return "admin/complaints/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Complaint not found!");
            return "redirect:/admin/complaints";
        }
    }

    @PostMapping("/{id}/update-status")
    public String updateComplaintStatus(@PathVariable Long id,
                                        @RequestParam ReclamationStatus status,
                                        RedirectAttributes redirectAttributes) {
        try {
            reclamationService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assignComplaintToPrestataire(@PathVariable Long id,
                                               @RequestParam Long prestataireId,
                                               RedirectAttributes redirectAttributes) {
        try {
            reclamationService.assignToPrestataire(id, prestataireId);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/{id}/add-comment")
    public String addCommentToComplaint(@PathVariable Long id,
                                        @RequestParam String content,
                                        @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                        RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            User currentUser = ((CustomUserDetails) userDetails).getUser();

            // Add comment as admin
            commentService.addComment(id, currentUser.getId(), content, true);

            redirectAttributes.addFlashAttribute("successMessage", "Comment added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding comment: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteComplaint(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.deleteReclamation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting complaint: " + e.getMessage());
        }
        return "redirect:/admin/complaints";
    }

    @PostMapping("/{id}/resolve")
    public String resolveComplaint(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.resolveReclamation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint resolved!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/{id}/close")
    public String closeComplaint(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.closeReclamation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint closed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/comments/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam Long reclamationId,
                                RedirectAttributes redirectAttributes) {
        try {
            commentService.deleteComment(commentId);
            redirectAttributes.addFlashAttribute("successMessage", "Comment deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + reclamationId;
    }
}

