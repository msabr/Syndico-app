package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ResidentPaymentController {

    private final ResidentService residentService;
    private final PaymentService paymentService;
    private final ChargeService chargeService;

    // MY CHARGES
    // =========================
    @GetMapping("/my-charges")
    public String myCharges(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "client/finance/myCharges";
            }

            List<Charge> allCharges = chargeService.getChargesByResident(resident.getId());

            double pendingAmount = allCharges.stream()
                    .filter(c -> c.getStatus() == ChargeStatus.EN_ATTENTE)
                    .mapToDouble(c -> c.getAmount().doubleValue())
                    .sum();

            double paidThisYear = allCharges.stream()
                    .filter(c -> c.getStatus() == ChargeStatus.PAYEE &&
                            c.getDueDate().getYear() == java.time.LocalDate.now().getYear())
                    .mapToDouble(c -> c.getAmount().doubleValue())
                    .sum();

            double overdueAmount = allCharges.stream()
                    .filter(c -> c.getStatus() == ChargeStatus.EN_RETARD)
                    .mapToDouble(c -> c.getAmount().doubleValue())
                    .sum();

            double totalThisYear = allCharges.stream()
                    .filter(c -> c.getDueDate().getYear() == java.time.LocalDate.now().getYear())
                    .mapToDouble(c -> c.getAmount().doubleValue())
                    .sum();

            Map<String, Object> summary = new HashMap<>();
            summary.put("pending", pendingAmount);
            summary.put("paidThisYear", paidThisYear);
            summary.put("overdue", overdueAmount);
            summary.put("totalThisYear", totalThisYear);

            model.addAttribute("charges", allCharges);
            model.addAttribute("summary", summary);
            model.addAttribute("resident", resident);

            return "client/finance/myCharges";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading charges: " + e.getMessage());
            model.addAttribute("charges", new ArrayList<>());
            return "client/finance/myCharges";
        }
    }

    // PAYMENT HISTORY
    // =========================
    @GetMapping("/payment-history")
    public String paymentHistory(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "client/finance/paymentHistory";
            }

            List<Payment> payments = paymentService.getAllPayments().stream()
                    .filter(p -> p.getCharge() != null &&
                            p.getCharge().getResident() != null &&
                            p.getCharge().getResident().getId().equals(resident.getId()))
                    .collect(Collectors.toList());

            double totalPaid = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                    .mapToDouble(p -> p.getAmount().doubleValue())
                    .sum();

            long paymentsThisYear = payments.stream()
                    .filter(p -> p.getPaymentDate() != null &&
                            p.getPaymentDate().getYear() == LocalDateTime.now().getYear())
                    .count();

            double averagePayment = payments.isEmpty() ? 0 : totalPaid / payments.size();

            Payment lastPayment = payments.stream()
                    .filter(p -> p.getPaymentDate() != null)
                    .max(Comparator.comparing(Payment::getPaymentDate))
                    .orElse(null);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalPaid", totalPaid);
            statistics.put("paymentsThisYear", paymentsThisYear);
            statistics.put("averagePayment", averagePayment);
            statistics.put("lastPaymentDate", lastPayment != null ? lastPayment.getPaymentDate() : null);

            model.addAttribute("payments", payments);
            model.addAttribute("statistics", statistics);
            model.addAttribute("resident", resident);

            return "client/finance/paymentHistory";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading payment history: " + e.getMessage());
            model.addAttribute("payments", new ArrayList<>());
            return "client/finance/paymentHistory";
        }
    }
}

