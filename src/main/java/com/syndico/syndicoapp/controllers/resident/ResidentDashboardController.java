package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client/dashboard")
@RequiredArgsConstructor
public class ResidentDashboardController {

    private final ResidentService residentService;
    private final ChargeService chargeService;
    private final ReclamationService reclamationService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final AssemblyMeetingService assemblyMeetingService;

    @GetMapping
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // Get resident information
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found. Please contact administration.");
                return "client/dashboard";
            }

            // Get statistics
            List<Charge> pendingCharges = chargeService.getChargesByResidentAndStatus(
                resident.getId(), ChargeStatus.EN_ATTENTE
            );

            List<Payment> paymentsThisYear = paymentService.getPaymentsByResidentAndYear(
                resident.getId(), LocalDateTime.now().getYear()
            );

            List<Reclamation> activeComplaints = reclamationService.findByResidentIdAndStatus(
                resident.getId(), ReclamationStatus.EN_COURS
            );

            List<Reservation> upcomingReservations = reservationService.getUpcomingReservationsByResident(
                resident.getId()
            );

            // Add to model
            model.addAttribute("resident", resident);
            model.addAttribute("pendingChargesCount", pendingCharges.size());
            model.addAttribute("paymentsThisYearCount", paymentsThisYear.size());
            model.addAttribute("activeComplaintsCount", activeComplaints.size());
            model.addAttribute("upcomingReservationsCount", upcomingReservations.size());

            // Get recent payments (last 3)
            List<Payment> recentPayments = paymentsThisYear.stream()
                .sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()))
                .limit(3)
                .collect(Collectors.toList());
            model.addAttribute("recentPayments", recentPayments);

            // Get upcoming events
            List<AssemblyMeeting> upcomingMeetings = assemblyMeetingService.getUpcomingMeetings();
            model.addAttribute("upcomingMeetings", upcomingMeetings);

            return "client/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading dashboard: " + e.getMessage());
            // Set default empty values to prevent errors
            model.addAttribute("pendingChargesCount", 0);
            model.addAttribute("paymentsThisYearCount", 0);
            model.addAttribute("activeComplaintsCount", 0);
            model.addAttribute("upcomingReservationsCount", 0);
            model.addAttribute("recentPayments", new ArrayList<>());
            model.addAttribute("upcomingMeetings", new ArrayList<>());
            return "client/dashboard";
        }
    }
}

