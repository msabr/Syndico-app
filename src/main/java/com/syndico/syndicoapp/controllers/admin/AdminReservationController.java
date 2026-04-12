package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.Reservation;
import com.syndico.syndicoapp.models.enums.ReservationStatus;
import com.syndico.syndicoapp.models.enums.SpaceType;
import com.syndico.syndicoapp.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public String listReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) SpaceType spaceType,
            Model model) {

        List<Reservation> reservations;

        if (status != null) {
            reservations = reservationService.getReservationsByStatus(status);
        } else if (spaceType != null) {
            reservations = reservationService.getReservationsBySpaceType(spaceType);
        } else {
            reservations = reservationService.getAllReservations();
        }

        // Get statistics
        ReservationService.ReservationStatistics stats = reservationService.getReservationStatistics();

        // Get upcoming reservations
        List<Reservation> upcomingReservations = reservationService.getUpcomingReservations();

        model.addAttribute("reservations", reservations);
        model.addAttribute("statistics", stats);
        model.addAttribute("upcomingReservations", upcomingReservations);
        model.addAttribute("reservationStatuses", ReservationStatus.values());
        model.addAttribute("spaceTypes", SpaceType.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSpaceType", spaceType);

        return "admin/reservations/list";
    }

    @GetMapping("/{id}")
    public String viewReservationDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Reservation reservation = reservationService.getReservationById(id)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            model.addAttribute("reservation", reservation);
            model.addAttribute("reservationStatuses", ReservationStatus.values());

            return "admin/reservations/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reservation not found!");
            return "redirect:/admin/reservations";
        }
    }

    @PostMapping("/{id}/approve")
    public String approveReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.approveReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/reject")
    public String rejectReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.rejectReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation cancelled!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @PostMapping("/delete/{id}")
    public String deleteReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.deleteReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting reservation: " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    @GetMapping("/check-availability")
    @ResponseBody
    public boolean checkAvailability(
            @RequestParam SpaceType spaceType,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime,
            @RequestParam(required = false) Long excludeId) {

        try {
            LocalDateTime start = LocalDateTime.parse(startDateTime);
            LocalDateTime end = LocalDateTime.parse(endDateTime);
            return reservationService.isSpaceAvailable(spaceType, start, end, excludeId);
        } catch (Exception e) {
            return false;
        }
    }
}

