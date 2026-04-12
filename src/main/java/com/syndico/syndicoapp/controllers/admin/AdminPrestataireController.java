package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.Prestataire;
import com.syndico.syndicoapp.services.PrestataireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/service-providers")
@RequiredArgsConstructor
public class AdminPrestataireController {

    private final PrestataireService prestataireService;

    @GetMapping
    public String listServiceProviders(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            Model model) {

        List<Prestataire> prestataires;

        if (search != null && !search.isEmpty()) {
            prestataires = prestataireService.searchByCompanyName(search);
        } else if (specialty != null && !specialty.isEmpty()) {
            prestataires = prestataireService.getPrestatairesBySpecialty(specialty);
        } else if (active != null && active) {
            prestataires = prestataireService.getActivePrestataires();
        } else {
            prestataires = prestataireService.getAllPrestataires();
        }

        // Get statistics
        PrestataireService.PrestataireStatistics stats = prestataireService.getPrestataireStatistics();

        // Get all specialties for filter dropdown
        List<String> specialties = prestataireService.getAllPrestataires().stream()
                .map(Prestataire::getSpecialty)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("prestataires", prestataires);
        model.addAttribute("statistics", stats);
        model.addAttribute("specialties", specialties);
        model.addAttribute("selectedSpecialty", specialty);
        model.addAttribute("searchQuery", search);
        model.addAttribute("activeFilter", active);

        return "admin/service-providers/list";
    }

    @GetMapping("/new")
    public String showCreateServiceProviderForm(Model model) {
        model.addAttribute("prestataire", new Prestataire());
        return "admin/service-providers/form";
    }

    @PostMapping("/create")
    public String createServiceProvider(@ModelAttribute Prestataire prestataire,
                                        RedirectAttributes redirectAttributes) {
        try {
            prestataireService.createPrestataire(prestataire);
            redirectAttributes.addFlashAttribute("successMessage", "Service provider created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/admin/service-providers/new";
        }
        return "redirect:/admin/service-providers";
    }

    @GetMapping("/edit/{id}")
    public String showEditServiceProviderForm(@PathVariable Long id, Model model,
                                              RedirectAttributes redirectAttributes) {
        try {
            Prestataire prestataire = prestataireService.getPrestataireById(id)
                    .orElseThrow(() -> new RuntimeException("Service provider not found"));

            model.addAttribute("prestataire", prestataire);
            return "admin/service-providers/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Service provider not found!");
            return "redirect:/admin/service-providers";
        }
    }

    @PostMapping("/update/{id}")
    public String updateServiceProvider(@PathVariable Long id, @ModelAttribute Prestataire prestataire,
                                        RedirectAttributes redirectAttributes) {
        try {
            prestataireService.updatePrestataire(id, prestataire);
            redirectAttributes.addFlashAttribute("successMessage", "Service provider updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/service-providers";
    }

    @PostMapping("/delete/{id}")
    public String deleteServiceProvider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            prestataireService.deletePrestataire(id);
            redirectAttributes.addFlashAttribute("successMessage", "Service provider deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/service-providers";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleServiceProviderStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            prestataireService.toggleActiveStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/service-providers";
    }

    @PostMapping("/{id}/update-rating")
    public String updateServiceProviderRating(@PathVariable Long id, @RequestParam Double rating,
                                              RedirectAttributes redirectAttributes) {
        try {
            prestataireService.updateRating(id, rating);
            redirectAttributes.addFlashAttribute("successMessage", "Rating updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/service-providers";
    }
}

