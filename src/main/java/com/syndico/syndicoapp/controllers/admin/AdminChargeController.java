package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.Charge;
import com.syndico.syndicoapp.models.enums.ChargeStatus;
import com.syndico.syndicoapp.models.enums.ChargeType;
import com.syndico.syndicoapp.services.ChargeService;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/charges")
@RequiredArgsConstructor
public class AdminChargeController {

    private final ChargeService chargeService;
    private final ResidentService residentService;

    @GetMapping
    public String listCharges(
            @RequestParam(required = false) ChargeStatus status,
            @RequestParam(required = false) ChargeType type,
            Model model) {

        List<Charge> charges;

        if (status != null) {
            charges = chargeService.getChargesByStatus(status);
        } else if (type != null) {
            charges = chargeService.getChargesByType(type);
        } else {
            charges = chargeService.getAllCharges();
        }

        // Get statistics
        ChargeService.ChargeStatistics stats = chargeService.getChargeStatistics();

        model.addAttribute("charges", charges);
        model.addAttribute("statistics", stats);
        model.addAttribute("chargeStatuses", ChargeStatus.values());
        model.addAttribute("chargeTypes", ChargeType.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);

        return "admin/charges/list";
    }

    @GetMapping("/new")
    public String showCreateChargeForm(Model model) {
        model.addAttribute("charge", new Charge());
        model.addAttribute("residents", residentService.getAllResidents());
        model.addAttribute("chargeTypes", ChargeType.values());
        model.addAttribute("chargeStatuses", ChargeStatus.values());
        return "admin/charges/form";
    }

    @PostMapping("/create")
    public String createCharge(@ModelAttribute Charge charge, RedirectAttributes redirectAttributes) {
        try {
            chargeService.createCharge(charge);
            redirectAttributes.addFlashAttribute("successMessage", "Charge created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating charge: " + e.getMessage());
        }
        return "redirect:/admin/charges";
    }

    @GetMapping("/edit/{id}")
    public String showEditChargeForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Charge charge = chargeService.getChargeById(id)
                    .orElseThrow(() -> new RuntimeException("Charge not found"));

            model.addAttribute("charge", charge);
            model.addAttribute("residents", residentService.getAllResidents());
            model.addAttribute("chargeTypes", ChargeType.values());
            model.addAttribute("chargeStatuses", ChargeStatus.values());

            return "admin/charges/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Charge not found!");
            return "redirect:/admin/charges";
        }
    }

    @PostMapping("/update/{id}")
    public String updateCharge(@PathVariable Long id, @ModelAttribute Charge charge,
                               RedirectAttributes redirectAttributes) {
        try {
            chargeService.updateCharge(id, charge);
            redirectAttributes.addFlashAttribute("successMessage", "Charge updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating charge: " + e.getMessage());
        }
        return "redirect:/admin/charges";
    }

    @PostMapping("/delete/{id}")
    public String deleteCharge(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            chargeService.deleteCharge(id);
            redirectAttributes.addFlashAttribute("successMessage", "Charge deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting charge: " + e.getMessage());
        }
        return "redirect:/admin/charges";
    }

    @PostMapping("/mark-paid/{id}")
    public String markChargeAsPaid(@PathVariable Long id,
                                   @RequestParam String paymentMethod,
                                   RedirectAttributes redirectAttributes) {
        try {
            chargeService.markAsPaid(id, paymentMethod);
            redirectAttributes.addFlashAttribute("successMessage", "Charge marked as paid!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/charges";
    }

    @PostMapping("/update-overdue")
    public String updateOverdueCharges(RedirectAttributes redirectAttributes) {
        try {
            chargeService.updateOverdueCharges();
            redirectAttributes.addFlashAttribute("successMessage", "Overdue charges updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/charges";
    }
}

