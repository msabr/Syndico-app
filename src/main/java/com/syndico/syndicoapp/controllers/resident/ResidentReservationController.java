package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.ReservationService;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ResidentReservationController {

    private final ReservationService reservationService;
    private final ResidentService residentService;

    @GetMapping("/my-reservations")
    public String myReservations(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            // Get all reservations for the resident
            List<Reservation> allReservations = reservationService.getReservationsByResident(resident.getId());

            // Filter by status
            List<Reservation> upcomingReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE
                          && r.getStartDateTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

            List<Reservation> confirmedReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .collect(Collectors.toList());

            List<Reservation> pendingReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.EN_ATTENTE)
                .collect(Collectors.toList());

            List<Reservation> cancelledReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.ANNULEE)
                .collect(Collectors.toList());

            model.addAttribute("resident", resident);
            model.addAttribute("reservations", allReservations);
            model.addAttribute("upcomingCount", upcomingReservations.size());
            model.addAttribute("confirmedCount", confirmedReservations.size());
            model.addAttribute("pendingCount", pendingReservations.size());
            model.addAttribute("cancelledCount", cancelledReservations.size());

            return "client/facilities/my-reservations";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading reservations: " + e.getMessage());
            model.addAttribute("reservations", new ArrayList<>());
            return "client/facilities/my-reservations";
        }
    }

    @GetMapping("/book-space")
    public String bookSpace(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            // Get all space types
            SpaceType[] spaceTypes = SpaceType.values();

            model.addAttribute("resident", resident);
            model.addAttribute("spaceTypes", spaceTypes);
            model.addAttribute("reservation", new Reservation());

            return "client/facilities/book-space";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading booking page: " + e.getMessage());
            return "client/facilities/book-space";
        }
    }

    @PostMapping("/book-space")
    public String submitReservation(
            @RequestParam SpaceType spaceType,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            Reservation reservation = Reservation.builder()
                .resident(resident)
                .spaceType(spaceType)
                .startDateTime(LocalDateTime.parse(startDateTime))
                .endDateTime(LocalDateTime.parse(endDateTime))
                .notes(notes)
                .status(ReservationStatus.EN_ATTENTE)
                .build();

            reservationService.createReservation(reservation);

            redirectAttributes.addFlashAttribute("successMessage", "Reservation submitted successfully!");
            return "redirect:/client/my-reservations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating reservation: " + e.getMessage());
            return "redirect:/client/book-space";
        }
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling reservation: " + e.getMessage());
        }
        return "redirect:/client/my-reservations";
    }
}

