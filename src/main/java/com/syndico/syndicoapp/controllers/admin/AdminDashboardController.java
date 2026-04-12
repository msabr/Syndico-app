package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.ReclamationStatus;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ResidentService residentService;
    private final BuildingService buildingService;
    private final ApartmentService apartmentService;
    private final ChargeService chargeService;
    private final PaymentService paymentService;
    private final ReclamationService reclamationService;
    private final ReservationService reservationService;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Statistics Cards
        model.addAttribute("totalResidents", residentService.getAllResidents().size());
        model.addAttribute("totalBuildings", buildingService.findAll().size());
        model.addAttribute("totalApartments", apartmentService.findAll().size());

        // Financial Stats
        List<Payment> allPayments = paymentService.getAllPayments();
        double totalRevenue = allPayments.stream()
                .filter(p -> p.getPaymentDate() != null &&
                            p.getPaymentDate().isAfter(LocalDateTime.now().minusMonths(1)))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0.0)
                .sum();
        model.addAttribute("monthlyRevenue", totalRevenue);

        // Complaints Stats
        List<Reclamation> allComplaints = reclamationService.getAllReclamations();
        long pendingComplaints = allComplaints.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.NOUVELLE ||
                            r.getStatus() == ReclamationStatus.EN_COURS ||
                            r.getStatus() == ReclamationStatus.ASSIGNEE)
                .count();
        model.addAttribute("pendingComplaints", pendingComplaints);

        // Reservations Today
        List<Reservation> allReservations = reservationService.getAllReservations();
        long todayReservations = allReservations.stream()
                .filter(r -> r.getStartDateTime() != null &&
                            r.getStartDateTime().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();
        model.addAttribute("todayReservations", todayReservations);

        // Recent Complaints (last 5)
        List<Reclamation> recentComplaints = allComplaints.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentComplaints", recentComplaints);

        // Revenue data for chart (last 7 days)
        Map<String, Double> revenueByDay = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dayLabel = date.getDayOfWeek().toString().substring(0, 3);
            double dayRevenue = allPayments.stream()
                    .filter(p -> p.getPaymentDate() != null &&
                                p.getPaymentDate().toLocalDate().equals(date.toLocalDate()))
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0.0)
                    .sum();
            revenueByDay.put(dayLabel, dayRevenue);
        }
        model.addAttribute("revenueByDay", revenueByDay);

        // Payment methods distribution
        Map<String, Long> paymentMethodsCount = allPayments.stream()
                .filter(p -> p.getPaymentDate() != null &&
                            p.getPaymentDate().isAfter(LocalDateTime.now().minusMonths(1)))
                .collect(Collectors.groupingBy(
                    p -> p.getPaymentMethod() != null && !p.getPaymentMethod().isEmpty()
                            ? p.getPaymentMethod() : "UNKNOWN",
                    Collectors.counting()
                ));
        model.addAttribute("paymentMethods", paymentMethodsCount);

        // Unread notifications count
        if (userDetails != null) {
            long unreadNotifications = notificationService.countUnread(userDetails.getId());
            model.addAttribute("unreadNotifications", unreadNotifications);
        }

        return "admin/dashboard";
    }
}

