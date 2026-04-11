package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.Building;
import com.syndico.syndicoapp.services.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/buildings")
@RequiredArgsConstructor
public class AdminBuildingController {

    private final BuildingService buildingService;

    @GetMapping
    public String listBuildings(Model model) {
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("activePage", "buildings");
        return "admin/buildings/list";
    }

    @GetMapping("/new")
    public String newBuildingForm(Model model) {
        model.addAttribute("building", new Building());
        model.addAttribute("activePage", "buildings");
        return "admin/buildings/form";
    }

    @GetMapping("/edit/{id}")
    public String editBuildingForm(@PathVariable Long id, Model model) {
        model.addAttribute("building", buildingService.findById(id));
        model.addAttribute("activePage", "buildings");
        return "admin/buildings/form";
    }

    @PostMapping("/save")
    public String saveBuilding(@ModelAttribute Building building, RedirectAttributes redirectAttributes) {
        try {
            buildingService.save(building);
            redirectAttributes.addFlashAttribute("successMessage", "Building saved successfully!");
            return "redirect:/admin/buildings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving building: " + e.getMessage());
            return "redirect:/admin/buildings/new";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBuilding(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            buildingService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Building deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting building: " + e.getMessage());
        }
        return "redirect:/admin/buildings";
    }
}

