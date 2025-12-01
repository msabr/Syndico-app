package com.syndico.syndicoapp.controllers;

import com.syndico.syndicoapp.dto.ResidentDTO;
import com.syndico.syndicoapp.models.Building;
import com.syndico.syndicoapp.models.Resident;
import com.syndico.syndicoapp.services.BuildingService;
import com.syndico.syndicoapp.services.ResidentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;
    private final BuildingService buildingService;

    // List all residents with filters
    @GetMapping
    public String listResidents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long building,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<Resident> residentPage = residentService.findAll(search, building, status, type, pageable);

        // Statistics
        long totalResidents = residentService.count();
        long activeResidents = residentService.countByActive(true);
        long owners = residentService.countByIsOwner(true);
        long tenants = residentService.countByIsOwner(false);

        List<Building> buildings = buildingService.findAll();

        model.addAttribute("residents", residentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", residentPage.getTotalPages());
        model.addAttribute("totalResidents", totalResidents);
        model.addAttribute("activeResidents", activeResidents);
        model.addAttribute("owners", owners);
        model.addAttribute("tenants", tenants);
        model.addAttribute("buildings", buildings);

        return "admin/residents/list";
    }

    // Show create form
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("resident", new ResidentDTO());
        model.addAttribute("buildings", buildingService.findAll());
        return "admin/residents/form";
    }

    // Create new resident
    @PostMapping
    public String createResident(
            @Valid @ModelAttribute("resident") ResidentDTO residentDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("buildings", buildingService.findAll());
            return "admin/residents/form";
        }

        try {
            residentService.create(residentDTO);
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message", "Resident created successfully!");
            return "redirect:/admin/residents";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("buildings", buildingService.findAll());
            return "admin/residents/form";
        }
    }

    // Show edit form
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Resident resident = residentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        model.addAttribute("resident", residentService.toDTO(resident));
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("isEdit", true);

        return "admin/residents/form";
    }

    // Update resident
    @PostMapping("/{id}")
    public String updateResident(
            @PathVariable Long id,
            @Valid @ModelAttribute("resident") ResidentDTO residentDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("buildings", buildingService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/residents/form";
        }

        try {
            residentService.update(id, residentDTO);
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message", "Resident updated successfully!");
            return "redirect:/admin/residents";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("buildings", buildingService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/residents/form";
        }
    }

    // View resident details
    @GetMapping("/{id}")
    public String viewResident(@PathVariable Long id, Model model) {
        Resident resident = residentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        model.addAttribute("resident", resident);
        return "admin/residents/view";
    }

    // Delete resident
    @PostMapping("/{id}/delete")
    public String deleteResident(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            residentService.delete(id);
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message", "Resident deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("message", "Failed to delete resident: " + e.getMessage());
        }

        return "redirect:/admin/residents";
    }

    // Export to Excel
    @GetMapping("/export")
    public void exportToExcel(
            @RequestParam(required = false) String format,
            HttpServletResponse response) throws IOException {

        List<Resident> residents = residentService.findAll();

        if ("excel".equals(format)) {
            residentService.exportToExcel(residents, response);
        }
    }
}
