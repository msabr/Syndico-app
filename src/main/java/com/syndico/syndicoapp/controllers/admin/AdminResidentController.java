package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.dto.ResidentDTO;
import com.syndico.syndicoapp.models.Building;
import com.syndico.syndicoapp.services.BuildingService;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/residents")
@RequiredArgsConstructor
public class AdminResidentController {

    private final ResidentService residentService;
    private final BuildingService buildingService;

    @GetMapping
    public String listResidents(Model model,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String filter) {
        List<ResidentDTO> residents;

        if (search != null && !search.isEmpty()) {
            residents = residentService.searchResidents(search).stream()
                    .map(r -> residentService.getResidentDTOById(r.getId()))
                    .collect(Collectors.toList());
        } else if ("owners".equals(filter)) {
            residents = residentService.getOwners().stream()
                    .map(r -> residentService.getResidentDTOById(r.getId()))
                    .collect(Collectors.toList());
        } else if ("tenants".equals(filter)) {
            residents = residentService.getTenants().stream()
                    .map(r -> residentService.getResidentDTOById(r.getId()))
                    .collect(Collectors.toList());
        } else {
            residents = residentService.getAllResidentsDTO();
        }

        model.addAttribute("residents", residents);
        model.addAttribute("totalResidents", residentService.countResidents());
        model.addAttribute("searchTerm", search);
        model.addAttribute("filter", filter);

        return "admin/residents/list";
    }

    @GetMapping("/{id}")
    public String viewResident(@PathVariable Long id, Model model) {
        ResidentDTO resident = residentService.getResidentDTOById(id);
        model.addAttribute("resident", resident);
        return "admin/residents/details";
    }

    @GetMapping("/new")
    public String newResidentForm(Model model) {
        List<Building> buildings = buildingService.getAllBuildings();
        model.addAttribute("residentDTO", new ResidentDTO());
        model.addAttribute("buildings", buildings);
        return "admin/residents/form";
    }

    @PostMapping("/create")
    public String createResident(@ModelAttribute ResidentDTO residentDTO,
                                 RedirectAttributes redirectAttributes) {
        try {
            residentService.createResident(residentDTO);
            redirectAttributes.addFlashAttribute("success", "Resident created successfully");
            return "redirect:/admin/residents";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create resident: " + e.getMessage());
            return "redirect:/admin/residents/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String editResidentForm(@PathVariable Long id, Model model) {
        ResidentDTO residentDTO = residentService.getResidentDTOById(id);
        List<Building> buildings = buildingService.getAllBuildings();
        model.addAttribute("residentDTO", residentDTO);
        model.addAttribute("buildings", buildings);
        return "admin/residents/form";
    }

    @PostMapping("/update/{id}")
    public String updateResident(@PathVariable Long id,
                                 @ModelAttribute ResidentDTO residentDTO,
                                 RedirectAttributes redirectAttributes) {
        try {
            residentService.updateResident(id, residentDTO);
            redirectAttributes.addFlashAttribute("success", "Resident updated successfully");
            return "redirect:/admin/residents";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update resident: " + e.getMessage());
            return "redirect:/admin/residents/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteResident(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            residentService.deleteResident(id);
            redirectAttributes.addFlashAttribute("success", "Resident deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete resident: " + e.getMessage());
        }
        return "redirect:/admin/residents";
    }
}

