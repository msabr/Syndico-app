package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.Apartment;
import com.syndico.syndicoapp.services.ApartmentService;
import com.syndico.syndicoapp.services.BuildingService;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/apartments")
@RequiredArgsConstructor
public class AdminApartmentController {

    private final ApartmentService apartmentService;
    private final BuildingService buildingService;
    private final ResidentService residentService;

    @GetMapping
    public String listApartments(Model model) {
        model.addAttribute("apartments", apartmentService.findAll());
        model.addAttribute("activePage", "apartments");
        return "admin/apartments/list";
    }

    @GetMapping("/new")
    public String newApartmentForm(Model model) {
        model.addAttribute("apartment", new Apartment());
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("residents", residentService.findAll());
        model.addAttribute("activePage", "apartments");
        return "admin/apartments/form";
    }

    @GetMapping("/edit/{id}")
    public String editApartmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("apartment", apartmentService.findById(id));
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("residents", residentService.findAll());
        model.addAttribute("activePage", "apartments");
        return "admin/apartments/form";
    }

    @PostMapping("/save")
    public String saveApartment(@ModelAttribute Apartment apartment, RedirectAttributes redirectAttributes) {
        try {
            apartmentService.save(apartment);
            redirectAttributes.addFlashAttribute("successMessage", "Apartment saved successfully!");
            return "redirect:/admin/apartments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving apartment: " + e.getMessage());
            return "redirect:/admin/apartments/new";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteApartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            apartmentService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Apartment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting apartment: " + e.getMessage());
        }
        return "redirect:/admin/apartments";
    }
}

