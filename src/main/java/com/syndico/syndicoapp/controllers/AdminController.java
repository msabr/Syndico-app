package com.syndico.syndicoapp.controllers;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.repositories.PrestataireRepository;
import com.syndico.syndicoapp.repositories.UserRepository;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ResidentService residentService;
    private final BuildingService buildingService;
    private final ApartmentService apartmentService;
    private final ChargeService chargeService;
    private final PaymentService paymentService;
    private final ReclamationService reclamationService;
    private final CommentService commentService;
    private final ReservationService reservationService;
    private final WorkProjectService workProjectService;
    private final PrestataireService prestataireService;
    private final AssemblyMeetingService assemblyMeetingService;
    private final VoteService voteService;
    private final DocumentService documentService;
    private final MessageService messageService;
    private final ChatbotQAService chatbotQAService;
    private final NotificationService notificationService;
    private final PrestataireRepository prestataireRepository;
    private final UserRepository userRepository;

    // ========  Dashboard  =========
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Statistics Cards
        model.addAttribute("totalResidents", residentService.findAll().size());
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
        Map<String, Double> revenueByDay = new java.util.LinkedHashMap<>();
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

    // ========  Residents Management  =========
    @GetMapping("/residents")
    public String listResidents(Model model) {
        model.addAttribute("residents", residentService.findAll());
        model.addAttribute("activePage", "residents");
        return "admin/residents/list";
    }

    // Add New Resident
    @GetMapping("/residents/new")
    public String newResidentForm(Model model) {
        model.addAttribute("resident", new Resident());
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("activePage", "residents-new");
        return "admin/residents/form";
    }

    // Edit Resident
    @GetMapping("/residents/edit/{id}")
    public String editResidentForm(@PathVariable Long id, Model model) {
        model.addAttribute("resident", residentService.findById(id));
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("activePage", "residents");
        return "admin/residents/form";
    }

    // Save Resident
    @PostMapping("/residents/save")
    public String saveResident(@ModelAttribute Resident resident, RedirectAttributes redirectAttributes) {
        try {
            residentService.save(resident);
            redirectAttributes.addFlashAttribute("successMessage", "Resident saved successfully!");
            return "redirect:/admin/residents";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving resident: " + e.getMessage());
            return "redirect:/admin/residents/new";
        }
    }

    // Delete Resident
    @GetMapping("/residents/delete/{id}")
    public String deleteResident(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            residentService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Resident deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting resident: " + e.getMessage());
        }
        return "redirect:/admin/residents";
    }

    // ==========  Buildings Management  =========
    @GetMapping("/buildings")
    public String listBuildings(Model model) {
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("activePage", "buildings");
        return "admin/buildings/list";
    }

    // Buildings - NEW FORM
    @GetMapping("/buildings/new")
    public String newBuildingForm(Model model) {
        model.addAttribute("building", new Building());
        model.addAttribute("activePage", "buildings");
        return "admin/buildings/form";
    }

    // Buildings - EDIT
    @GetMapping("/buildings/edit/{id}")
    public String editBuildingForm(@PathVariable Long id, Model model) {
        model.addAttribute("building", buildingService.findById(id));
        model.addAttribute("activePage", "buildings");
        return "admin/buildings/form";
    }

    // Buildings - SAVE
    @PostMapping("/buildings/save")
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

    // Buildings - DELETE
    @GetMapping("/buildings/delete/{id}")
    public String deleteBuilding(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            buildingService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Building deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting building: " + e.getMessage());
        }
        return "redirect:/admin/buildings";
    }

    // =========  Apartments Management  =========
    @GetMapping("/apartments")
    public String listApartments(Model model) {
        model.addAttribute("apartments", apartmentService.findAll());
        model.addAttribute("activePage", "apartments");
        return "admin/apartments/list";
    }

    // Apartments - NEW FORM
    @GetMapping("/apartments/new")
    public String newApartmentForm(Model model) {
        model.addAttribute("apartment", new Apartment());
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("residents", residentService.findAll());
        model.addAttribute("activePage", "apartments");
        return "admin/apartments/form";
    }

    // Apartments - EDIT
    @GetMapping("/apartments/edit/{id}")
    public String editApartmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("apartment", apartmentService.findById(id));
        model.addAttribute("buildings", buildingService.findAll());
        model.addAttribute("residents", residentService.findAll());
        model.addAttribute("activePage", "apartments");
        return "admin/apartments/form";
    }

    // Apartments - SAVE
    @PostMapping("/apartments/save")
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

    // Apartments - DELETE
    @GetMapping("/apartments/delete/{id}")
    public String deleteApartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            apartmentService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Apartment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting apartment: " + e.getMessage());
        }
        return "redirect:/admin/apartments";
    }

    // ==========  Charges Management  =========
    @GetMapping("/charges")
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

    // Show create charge form
    @GetMapping("/charges/new")
    public String showCreateChargeForm(Model model) {
        model.addAttribute("charge", new Charge());
        model.addAttribute("residents", residentService.getAllResidents());
        model.addAttribute("chargeTypes", ChargeType.values());
        model.addAttribute("chargeStatuses", ChargeStatus.values());
        return "admin/charges/form";
    }

    // Create new charge
    @PostMapping("/charges/create")
    public String createCharge(@ModelAttribute Charge charge, RedirectAttributes redirectAttributes) {
        try {
            chargeService.createCharge(charge);
            redirectAttributes.addFlashAttribute("successMessage", "Charge created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating charge: " + e.getMessage());
        }
        return "redirect:/admin/charges";
    }

    // Show edit charge form
    @GetMapping("/charges/edit/{id}")
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

    // Update charge
    @PostMapping("/charges/update/{id}")
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

    // Delete charge
    @PostMapping("/charges/delete/{id}")
    public String deleteCharge(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            chargeService.deleteCharge(id);
            redirectAttributes.addFlashAttribute("successMessage", "Charge deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting charge: " + e.getMessage());
        }
        return "redirect:/admin/charges";
    }

    // Mark charge as paid
    @PostMapping("/charges/mark-paid/{id}")
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

    // Update overdue charges
    @PostMapping("/charges/update-overdue")
    public String updateOverdueCharges(RedirectAttributes redirectAttributes) {
        try {
            chargeService.updateOverdueCharges();
            redirectAttributes.addFlashAttribute("successMessage", "Overdue charges updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/charges";
    }

    // =========  Payments Management  =========
    @GetMapping("/payments")
    public String listPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String method,
            Model model) {

        List<Payment> payments;

        if (status != null) {
            payments = paymentService.getPaymentsByStatus(status);
        } else if (method != null && !method.isEmpty()) {
            payments = paymentService.getPaymentsByMethod(method);
        } else {
            payments = paymentService.getAllPayments();
        }

        // Get statistics
        PaymentService.PaymentStatistics stats = paymentService.getPaymentStatistics();

        model.addAttribute("payments", payments);
        model.addAttribute("statistics", stats);
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedMethod", method);

        return "admin/payments/list";
    }

    // View payment details (modal/page)
    @GetMapping("/payments/{id}")
    public String viewPaymentDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.getPaymentById(id)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            model.addAttribute("payment", payment);
            return "admin/payments/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment not found!");
            return "redirect:/admin/payments";
        }
    }

    // Download payment receipt PDF
    @GetMapping("/payments/{id}/receipt")
    public ResponseEntity<byte[]> downloadPaymentReceipt(@PathVariable Long id) {
        try {
            byte[] pdfContent = paymentService.generatePaymentReceipt(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "receipt-" + id + ".pdf");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update payment status
    @PostMapping("/payments/update-status/{id}")
    public String updatePaymentStatus(@PathVariable Long id,
                                      @RequestParam PaymentStatus status,
                                      RedirectAttributes redirectAttributes) {
        try {
            paymentService.updatePaymentStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Payment status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/payments";
    }

    // Delete payment
    @PostMapping("/payments/delete/{id}")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paymentService.deletePayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting payment: " + e.getMessage());
        }
        return "redirect:/admin/payments";
    }

    // =========  Financial Reports  =========
    @GetMapping("/financial-reports")
    public String financialReports(
            @RequestParam(required = false) String period,
            Model model) {

        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        // Determine date range based on period
        if (period == null || period.equals("month")) {
            startDate = endDate.minusMonths(1);
            period = "month";
        } else if (period.equals("quarter")) {
            startDate = endDate.minusMonths(3);
        } else if (period.equals("year")) {
            startDate = endDate.minusYears(1);
        } else if (period.equals("all")) {
            startDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        } else {
            startDate = endDate.minusMonths(1);
            period = "month";
        }

        // Get data for reports
        List<Payment> payments = paymentService.getPaymentsByDateRange(startDate, endDate);
        List<com.syndico.syndicoapp.models.Charge> charges = chargeService.getChargesByDateRange(
                startDate.toLocalDate(),
                endDate.toLocalDate()
        );

        PaymentService.PaymentStatistics paymentStats = paymentService.getPaymentStatistics();
        ChargeService.ChargeStatistics chargeStats = chargeService.getChargeStatistics();

        model.addAttribute("payments", payments);
        model.addAttribute("charges", charges);
        model.addAttribute("paymentStats", paymentStats);
        model.addAttribute("chargeStats", chargeStats);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/reports/financial";
    }

    // Download financial report PDF
    @GetMapping("/financial-reports/download")
    public ResponseEntity<byte[]> downloadFinancialReport(
            @RequestParam(required = false, defaultValue = "month") String period) {
        try {
            LocalDateTime startDate;
            LocalDateTime endDate = LocalDateTime.now();

            if (period.equals("quarter")) {
                startDate = endDate.minusMonths(3);
            } else if (period.equals("year")) {
                startDate = endDate.minusYears(1);
            } else {
                startDate = endDate.minusMonths(1);
            }

            byte[] pdfContent = generateFinancialReportPDF(startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "financial-report-" + period + ".pdf");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to generate financial report PDF
    private byte[] generateFinancialReportPDF(LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
        com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

        // Title
        document.add(new com.itextpdf.layout.element.Paragraph("FINANCIAL REPORT")
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setFontSize(24)
                .setBold());

        document.add(new com.itextpdf.layout.element.Paragraph("SYNDICO Management")
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setFontSize(14));

        document.add(new com.itextpdf.layout.element.Paragraph(
                "Period: " + startDate.toLocalDate() + " to " + endDate.toLocalDate())
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setFontSize(12));

        document.add(new com.itextpdf.layout.element.Paragraph("\n\n"));

        // Get statistics
        PaymentService.PaymentStatistics paymentStats = paymentService.getPaymentStatistics();
        ChargeService.ChargeStatistics chargeStats = chargeService.getChargeStatistics();

        // Summary table
        com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(
                com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{1, 1}));
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        table.addCell(new com.itextpdf.layout.element.Paragraph("CHARGES SUMMARY").setBold());
        table.addCell(new com.itextpdf.layout.element.Paragraph(""));

        table.addCell(new com.itextpdf.layout.element.Paragraph("Total Charges:"));
        table.addCell(new com.itextpdf.layout.element.Paragraph(String.format("%.2f DH", chargeStats.getTotalAmount())));

        table.addCell(new com.itextpdf.layout.element.Paragraph("Paid Charges:"));
        table.addCell(new com.itextpdf.layout.element.Paragraph(String.format("%.2f DH", chargeStats.getPaidAmount())));

        table.addCell(new com.itextpdf.layout.element.Paragraph("Unpaid Charges:"));
        table.addCell(new com.itextpdf.layout.element.Paragraph(String.format("%.2f DH", chargeStats.getUnpaidAmount())));

        table.addCell(new com.itextpdf.layout.element.Paragraph("Collection Rate:"));
        table.addCell(new com.itextpdf.layout.element.Paragraph(String.format("%.1f%%", chargeStats.getCollectionRate())));

        table.addCell(new com.itextpdf.layout.element.Paragraph(""));
        table.addCell(new com.itextpdf.layout.element.Paragraph(""));

        table.addCell(new com.itextpdf.layout.element.Paragraph("PAYMENTS SUMMARY").setBold());
        table.addCell(new com.itextpdf.layout.element.Paragraph(""));

        table.addCell(new com.itextpdf.layout.element.Paragraph("Total Payments:"));
        table.addCell(new com.itextpdf.layout.element.Paragraph(String.valueOf(paymentStats.getTotalPayments())));

        table.addCell(new com.itextpdf.layout.element.Paragraph("Successful:"));
        table.addCell(new com.itextpdf.layout.element.Paragraph(String.valueOf(paymentStats.getSuccessfulPayments())));

        table.addCell(new com.itextpdf.layout.element.Paragraph("Total Amount:"));
        table.addCell(new com.itextpdf.layout.element.Paragraph(String.format("%.2f DH", paymentStats.getTotalAmount())));

        document.add(table);

        document.add(new com.itextpdf.layout.element.Paragraph("\n\n"));
        document.add(new com.itextpdf.layout.element.Paragraph("Generated on: " + LocalDateTime.now())
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setFontSize(10));

        document.close();
        return baos.toByteArray();
    }

    // ==========  Complaints Management  =========
    @GetMapping("/complaints")
    public String listComplaints(
            @RequestParam(required = false) ReclamationStatus status,
            @RequestParam(required = false) ReclamationCategory category,
            @RequestParam(required = false) Priority priority,
            Model model) {

        List<Reclamation> reclamations;

        if (status != null) {
            reclamations = reclamationService.getReclamationsByStatus(status);
        } else if (category != null) {
            reclamations = reclamationService.getReclamationsByCategory(category);
        } else if (priority != null) {
            reclamations = reclamationService.getReclamationsByPriority(priority);
        } else {
            reclamations = reclamationService.getAllReclamations();
        }

        // Get statistics
        ReclamationService.ReclamationStatistics stats = reclamationService.getReclamationStatistics();

        model.addAttribute("reclamations", reclamations);
        model.addAttribute("statistics", stats);
        model.addAttribute("reclamationStatuses", ReclamationStatus.values());
        model.addAttribute("reclamationCategories", ReclamationCategory.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedPriority", priority);

        return "admin/complaints/list";
    }

    // View complaint details
    @GetMapping("/complaints/{id}")
    public String viewComplaintDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Reclamation reclamation = reclamationService.getReclamationById(id)
                    .orElseThrow(() -> new RuntimeException("Reclamation not found"));

            List<Comment> comments = commentService.getCommentsByReclamation(id);
            List<Prestataire> prestataires = prestataireRepository.findAll();

            model.addAttribute("reclamation", reclamation);
            model.addAttribute("comments", comments);
            model.addAttribute("prestataires", prestataires);
            model.addAttribute("reclamationStatuses", ReclamationStatus.values());

            return "admin/complaints/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Complaint not found!");
            return "redirect:/admin/complaints";
        }
    }

    // Update complaint status
    @PostMapping("/complaints/{id}/update-status")
    public String updateComplaintStatus(@PathVariable Long id,
                                        @RequestParam ReclamationStatus status,
                                        RedirectAttributes redirectAttributes) {
        try {
            reclamationService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    // Assign complaint to prestataire
    @PostMapping("/complaints/{id}/assign")
    public String assignComplaintToPrestataire(@PathVariable Long id,
                                               @RequestParam Long prestataireId,
                                               RedirectAttributes redirectAttributes) {
        try {
            reclamationService.assignToPrestataire(id, prestataireId);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    // Add comment to complaint
    @PostMapping("/complaints/{id}/add-comment")
    public String addCommentToComplaint(@PathVariable Long id,
                                        @RequestParam String content,
                                        @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                        RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            User currentUser = ((com.syndico.syndicoapp.security.CustomUserDetails) userDetails).getUser();

            // Add comment as admin
            commentService.addComment(id, currentUser.getId(), content, true);

            redirectAttributes.addFlashAttribute("successMessage", "Comment added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding comment: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    // Delete complaint
    @PostMapping("/complaints/delete/{id}")
    public String deleteComplaint(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.deleteReclamation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting complaint: " + e.getMessage());
        }
        return "redirect:/admin/complaints";
    }

    // Resolve complaint
    @PostMapping("/complaints/{id}/resolve")
    public String resolveComplaint(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.resolveReclamation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint resolved!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    // Close complaint
    @PostMapping("/complaints/{id}/close")
    public String closeComplaint(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reclamationService.closeReclamation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint closed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    // Delete comment
    @PostMapping("/complaints/comments/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam Long reclamationId,
                                RedirectAttributes redirectAttributes) {
        try {
            commentService.deleteComment(commentId);
            redirectAttributes.addFlashAttribute("successMessage", "Comment deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/complaints/" + reclamationId;
    }

    // =========  Reservations Management  =========
    @GetMapping("/reservations")
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

    // View reservation details
    @GetMapping("/reservations/{id}")
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

    // Approve reservation
    @PostMapping("/reservations/{id}/approve")
    public String approveReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.approveReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    // Reject reservation
    @PostMapping("/reservations/{id}/reject")
    public String rejectReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.rejectReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    // Cancel reservation
    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation cancelled!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    // Delete reservation
    @PostMapping("/reservations/delete/{id}")
    public String deleteReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.deleteReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting reservation: " + e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    // Check availability (AJAX endpoint)
    @GetMapping("/reservations/check-availability")
    @ResponseBody
    public boolean checkAvailability(
            @RequestParam SpaceType spaceType,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime,
            @RequestParam(required = false) Long excludeId) {

        try {
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDateTime);
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDateTime);
            return reservationService.isSpaceAvailable(spaceType, start, end, excludeId);
        } catch (Exception e) {
            return false;
        }
    }

    // ==========  Work Projects Management  =========
    @GetMapping("/work-projects")
    public String listWorkProjects(
            @RequestParam(required = false) WorkStatus status,
            Model model) {

        List<WorkProject> workProjects;

        if (status != null) {
            workProjects = workProjectService.getWorkProjectsByStatus(status);
        } else {
            workProjects = workProjectService.getAllWorkProjects();
        }

        // Get statistics
        WorkProjectService.WorkProjectStatistics stats = workProjectService.getWorkProjectStatistics();

        model.addAttribute("workProjects", workProjects);
        model.addAttribute("statistics", stats);
        model.addAttribute("workStatuses", WorkStatus.values());
        model.addAttribute("selectedStatus", status);

        return "admin/work-projects/list";
    }

    // Show create work project form
    @GetMapping("/work-projects/new")
    public String showCreateWorkProjectForm(Model model) {
        model.addAttribute("workProject", new WorkProject());
        model.addAttribute("prestataires", prestataireRepository.findAll());
        model.addAttribute("workStatuses", WorkStatus.values());
        return "admin/work-projects/form";
    }

    // Create work project
    @PostMapping("/work-projects/create")
    public String createWorkProject(@ModelAttribute WorkProject workProject, RedirectAttributes redirectAttributes) {
        try {
            WorkProject created = workProjectService.createWorkProject(workProject);
            redirectAttributes.addFlashAttribute("successMessage", "Work project created successfully!");
            return "redirect:/admin/work-projects/" + created.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/admin/work-projects/new";
        }
    }

    // View work project details with timeline
    @GetMapping("/work-projects/{id}")
    public String viewWorkProjectDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            WorkProject workProject = workProjectService.getWorkProjectById(id)
                    .orElseThrow(() -> new RuntimeException("Work project not found"));

            List<TimelineStage> timelineStages = workProjectService.getTimelineStagesByWorkProject(id);
            double progress = workProjectService.calculateProjectProgress(id);

            model.addAttribute("workProject", workProject);
            model.addAttribute("timelineStages", timelineStages);
            model.addAttribute("progress", progress);
            model.addAttribute("workStatuses", WorkStatus.values());
            model.addAttribute("newStage", new TimelineStage());

            return "admin/work-projects/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Work project not found!");
            return "redirect:/admin/work-projects";
        }
    }

    // Show edit work project form
    @GetMapping("/work-projects/edit/{id}")
    public String showEditWorkProjectForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            WorkProject workProject = workProjectService.getWorkProjectById(id)
                    .orElseThrow(() -> new RuntimeException("Work project not found"));

            model.addAttribute("workProject", workProject);
            model.addAttribute("prestataires", prestataireRepository.findAll());
            model.addAttribute("workStatuses", WorkStatus.values());

            return "admin/work-projects/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Work project not found!");
            return "redirect:/admin/work-projects";
        }
    }

    // Update work project
    @PostMapping("/work-projects/update/{id}")
    public String updateWorkProject(@PathVariable Long id, @ModelAttribute WorkProject workProject,
                                    RedirectAttributes redirectAttributes) {
        try {
            workProjectService.updateWorkProject(id, workProject);
            redirectAttributes.addFlashAttribute("successMessage", "Work project updated successfully!");
            return "redirect:/admin/work-projects/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/admin/work-projects/edit/" + id;
        }
    }

    // Delete work project
    @PostMapping("/work-projects/delete/{id}")
    public String deleteWorkProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            workProjectService.deleteWorkProject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Work project deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects";
    }

    // Update work project status
    @PostMapping("/work-projects/{id}/update-status")
    public String updateWorkProjectStatus(@PathVariable Long id, @RequestParam WorkStatus status,
                                          RedirectAttributes redirectAttributes) {
        try {
            workProjectService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects/" + id;
    }

    // Add timeline stage
    @PostMapping("/work-projects/{projectId}/stages/add")
    public String addTimelineStage(@PathVariable Long projectId, @ModelAttribute TimelineStage stage,
                                   RedirectAttributes redirectAttributes) {
        try {
            workProjectService.addTimelineStage(projectId, stage);
            redirectAttributes.addFlashAttribute("successMessage", "Timeline stage added!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects/" + projectId;
    }

    // Update timeline stage status
    @PostMapping("/work-projects/stages/{stageId}/update-status")
    public String updateTimelineStageStatus(@PathVariable Long stageId,
                                            @RequestParam String status,
                                            @RequestParam Long projectId,
                                            RedirectAttributes redirectAttributes) {
        try {
            workProjectService.updateTimelineStageStatus(stageId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Stage status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects/" + projectId;
    }

    // Delete timeline stage
    @PostMapping("/work-projects/stages/delete/{stageId}")
    public String deleteTimelineStage(@PathVariable Long stageId, @RequestParam Long projectId,
                                      RedirectAttributes redirectAttributes) {
        try {
            workProjectService.deleteTimelineStage(stageId);
            redirectAttributes.addFlashAttribute("successMessage", "Timeline stage deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects/" + projectId;
    }

    // ========  Service Providers Management  =========
    @GetMapping("/service-providers")
    public String listServiceProviders(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            Model model) {

        List<Prestataire> prestataires;

        if (search != null && !search.isEmpty()) {
            prestataires = prestataireService.searchByCompanyName(search);
        } else if (specialty != null && !specialty.isEmpty()) {
            prestataires = prestataireService.getPrestatairesBySpecialty(specialty);
        } else if (active != null && active) {
            prestataires = prestataireService.getActivePrestataires();
        } else {
            prestataires = prestataireService.getAllPrestataires();
        }

        // Get statistics
        PrestataireService.PrestataireStatistics stats = prestataireService.getPrestataireStatistics();

        // Get all specialties for filter dropdown
        List<String> specialties = prestataireService.getAllPrestataires().stream()
                .map(Prestataire::getSpecialty)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("prestataires", prestataires);
        model.addAttribute("statistics", stats);
        model.addAttribute("specialties", specialties);
        model.addAttribute("selectedSpecialty", specialty);
        model.addAttribute("searchQuery", search);
        model.addAttribute("activeFilter", active);

        return "admin/service-providers/list";
    }

    // Show create service provider form
    @GetMapping("/service-providers/new")
    public String showCreateServiceProviderForm(Model model) {
        model.addAttribute("prestataire", new Prestataire());
        return "admin/service-providers/form";
    }

    // Create service provider
    @PostMapping("/service-providers/create")
    public String createServiceProvider(@ModelAttribute Prestataire prestataire,
                                        RedirectAttributes redirectAttributes) {
        try {
            prestataireService.createPrestataire(prestataire);
            redirectAttributes.addFlashAttribute("successMessage", "Service provider created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/admin/service-providers/new";
        }
        return "redirect:/admin/service-providers";
    }

    // Show edit service provider form
    @GetMapping("/service-providers/edit/{id}")
    public String showEditServiceProviderForm(@PathVariable Long id, Model model,
                                              RedirectAttributes redirectAttributes) {
        try {
            Prestataire prestataire = prestataireService.getPrestataireById(id)
                    .orElseThrow(() -> new RuntimeException("Service provider not found"));

            model.addAttribute("prestataire", prestataire);
            return "admin/service-providers/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Service provider not found!");
            return "redirect:/admin/service-providers";
        }
    }

    // Update service provider
    @PostMapping("/service-providers/update/{id}")
    public String updateServiceProvider(@PathVariable Long id, @ModelAttribute Prestataire prestataire,
                                        RedirectAttributes redirectAttributes) {
        try {
            prestataireService.updatePrestataire(id, prestataire);
            redirectAttributes.addFlashAttribute("successMessage", "Service provider updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/service-providers";
    }

    // Delete service provider
    @PostMapping("/service-providers/delete/{id}")
    public String deleteServiceProvider(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            prestataireService.deletePrestataire(id);
            redirectAttributes.addFlashAttribute("successMessage", "Service provider deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/service-providers";
    }

    // Toggle active status
    @PostMapping("/service-providers/{id}/toggle-status")
    public String toggleServiceProviderStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            prestataireService.toggleActiveStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/service-providers";
    }

    // Update rating
    @PostMapping("/service-providers/{id}/update-rating")
    public String updateServiceProviderRating(@PathVariable Long id, @RequestParam Double rating,
                                              RedirectAttributes redirectAttributes) {
        try {
            prestataireService.updateRating(id, rating);
            redirectAttributes.addFlashAttribute("successMessage", "Rating updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/service-providers";
    }

    // ========== GENERAL ASSEMBLIES ==========
    @GetMapping("/general-assemblies")
    public String listAssemblies(@RequestParam(required = false) MeetingStatus status, Model model) {
        List<AssemblyMeeting> meetings;

        if (status != null) {
            meetings = assemblyMeetingService.getMeetingsByStatus(status);
        } else {
            meetings = assemblyMeetingService.getAllMeetings();
        }

        AssemblyMeetingService.MeetingStatistics stats = assemblyMeetingService.getMeetingStatistics();

        model.addAttribute("meetings", meetings);
        model.addAttribute("statistics", stats);
        model.addAttribute("meetingStatuses", MeetingStatus.values());
        model.addAttribute("selectedStatus", status);

        return "admin/general-assemblies/list";
    }

    @PostMapping("/general-assemblies/create")
    public String createAssembly(@ModelAttribute AssemblyMeeting meeting, RedirectAttributes redirectAttributes) {
        try {
            assemblyMeetingService.createAssemblyMeeting(meeting);
            redirectAttributes.addFlashAttribute("successMessage", "Assembly created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/general-assemblies";
    }

    @GetMapping("/general-assemblies/{id}")
    public String viewAssemblyDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            AssemblyMeeting meeting = assemblyMeetingService.getMeetingById(id)
                    .orElseThrow(() -> new RuntimeException("Meeting not found"));

            List<Vote> votes = voteService.getAllVotes().stream()
                    .filter(v -> v.getAssemblyMeeting() != null && v.getAssemblyMeeting().getId().equals(id))
                    .collect(java.util.stream.Collectors.toList());

            model.addAttribute("meeting", meeting);
            model.addAttribute("votes", votes);
            model.addAttribute("meetingStatuses", MeetingStatus.values());

            return "admin/general-assemblies/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Meeting not found!");
            return "redirect:/admin/general-assemblies";
        }
    }

    @PostMapping("/general-assemblies/update/{id}")
    public String updateAssembly(@PathVariable Long id, @ModelAttribute AssemblyMeeting meeting,
                                 RedirectAttributes redirectAttributes) {
        try {
            assemblyMeetingService.updateMeeting(id, meeting);
            redirectAttributes.addFlashAttribute("successMessage", "Assembly updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/general-assemblies/" + id;
    }

    @PostMapping("/general-assemblies/delete/{id}")
    public String deleteAssembly(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            assemblyMeetingService.deleteMeeting(id);
            redirectAttributes.addFlashAttribute("successMessage", "Assembly deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/general-assemblies";
    }

    @PostMapping("/general-assemblies/{id}/update-status")
    public String updateAssemblyStatus(@PathVariable Long id, @RequestParam MeetingStatus status,
                                       RedirectAttributes redirectAttributes) {
        try {
            assemblyMeetingService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/general-assemblies/" + id;
    }

    // ========== VOTES ==========
    @GetMapping("/votes")
    public String listVotes(@RequestParam(required = false) VoteStatus status, Model model) {
        List<Vote> votes;

        if (status != null) {
            votes = voteService.getVotesByStatus(status);
        } else {
            votes = voteService.getAllVotes();
        }

        VoteService.VoteStatistics stats = voteService.getVoteStatistics();

        model.addAttribute("votes", votes);
        model.addAttribute("statistics", stats);
        model.addAttribute("voteStatuses", VoteStatus.values());
        model.addAttribute("selectedStatus", status);

        return "admin/votes/list";
    }

    @PostMapping("/votes/create")
    public String createVote(@ModelAttribute Vote vote, RedirectAttributes redirectAttributes) {
        try {
            voteService.createVote(vote);
            redirectAttributes.addFlashAttribute("successMessage", "Vote created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/votes";
    }

    @GetMapping("/votes/{id}/results")
    public String viewVoteResults(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            VoteService.VoteResults results = voteService.getVoteResults(id);
            model.addAttribute("results", results);
            return "admin/votes/results";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vote not found!");
            return "redirect:/admin/votes";
        }
    }

    @PostMapping("/votes/{id}/close")
    public String closeVote(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voteService.closeVote(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vote closed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/votes";
    }

    @PostMapping("/votes/delete/{id}")
    public String deleteVote(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voteService.deleteVote(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vote deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/votes";
    }

    // ========== DOCUMENTS MANAGEMENT ==========
    @GetMapping("/documents")
    public String listDocuments(Model model,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String search) {
        List<Document> documents;

        if (search != null && !search.isEmpty()) {
            documents = documentService.searchDocuments(search);
        } else if (category != null && !category.isEmpty() && !category.equals("ALL")) {
            DocumentCategory docCategory = DocumentCategory.valueOf(category);
            documents = documentService.getDocumentsByCategory(docCategory);
        } else {
            documents = documentService.getAllDocuments();
        }

        model.addAttribute("documents", documents);
        model.addAttribute("categories", DocumentCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchTerm", search);

        return "admin/documents/list";
    }

    // Show document details
    @GetMapping("/documents/{id}")
    public String viewDocument(@PathVariable Long id, Model model) {
        Document document = documentService.getDocumentById(id);
        model.addAttribute("document", document);
        return "admin/documents/details";
    }

    // Show create document form
    @GetMapping("/documents/new")
    public String newDocumentForm(Model model) {
        model.addAttribute("document", new Document());
        model.addAttribute("categories", DocumentCategory.values());
        return "admin/documents/form";
    }

    // Create document
    @PostMapping("/documents/create")
    public String createDocument(@RequestParam("title") String title,
                                 @RequestParam("description") String description,
                                 @RequestParam("category") DocumentCategory category,
                                 @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/admin/documents/new";
            }

            Long userId = userDetails.getId();
            documentService.createDocument(title, description, category, isPublic, file, userId);

            redirectAttributes.addFlashAttribute("success", "Document uploaded successfully");
            return "redirect:/admin/documents";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload document: " + e.getMessage());
            return "redirect:/admin/documents/new";
        }
    }

    // Show edit document form
    @GetMapping("/documents/edit/{id}")
    public String editDocumentForm(@PathVariable Long id, Model model) {
        Document document = documentService.getDocumentById(id);
        model.addAttribute("document", document);
        model.addAttribute("categories", DocumentCategory.values());
        return "admin/documents/form";
    }

    // Update document
    @PostMapping("/documents/update/{id}")
    public String updateDocument(@PathVariable Long id,
                                 @RequestParam("title") String title,
                                 @RequestParam("description") String description,
                                 @RequestParam("category") DocumentCategory category,
                                 @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            documentService.updateDocument(id, title, description, category, isPublic, file);
            redirectAttributes.addFlashAttribute("success", "Document updated successfully");
            return "redirect:/admin/documents";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update document: " + e.getMessage());
            return "redirect:/admin/documents/edit/" + id;
        }
    }

    // Delete document
    @PostMapping("/documents/delete/{id}")
    public String deleteDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            documentService.deleteDocument(id);
            redirectAttributes.addFlashAttribute("success", "Document deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete document: " + e.getMessage());
        }
        return "redirect:/admin/documents";
    }

    // Download document
    @GetMapping("/documents/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            Document document = documentService.getDocumentById(id);
            String filePath = "uploads/documents/" + document.getFileUrl().substring(document.getFileUrl().lastIndexOf("/") + 1);

            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getTitle() + "." + document.getFileType() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read file: " + document.getTitle());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file: " + e.getMessage());
        }
    }

    // ========== MESSAGES MANAGEMENT ==========

    // List all messages (inbox for admin)
    @GetMapping("/messages")
    public String listMessages(Model model,
                               @RequestParam(required = false) String filter,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long adminId = userDetails.getId();
        List<Message> messages;

        if ("unread".equals(filter)) {
            messages = messageService.getInboxMessages(adminId).stream()
                    .filter(m -> !m.getIsRead())
                    .collect(Collectors.toList());
        } else if ("sent".equals(filter)) {
            messages = messageService.getSentMessages(adminId);
        } else {
            messages = messageService.getInboxMessages(adminId);
        }

        // Get sender/receiver information
        messages.forEach(message -> {
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            User receiver = userRepository.findById(message.getReceiverId()).orElse(null);
            model.addAttribute("sender_" + message.getId(), sender);
            model.addAttribute("receiver_" + message.getId(), receiver);
        });

        long unreadCount = messageService.getUnreadMessagesCount(adminId);

        model.addAttribute("messages", messages);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("filter", filter);

        return "admin/messages/list";
    }

    // View message details
    @GetMapping("/messages/{id}")
    public String viewMessage(@PathVariable Long id,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Message message = messageService.getMessageById(id);

        // Mark as read if current user is the receiver
        if (message.getReceiverId().equals(userDetails.getId()) && !message.getIsRead()) {
            messageService.markAsRead(id);
        }

        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        User receiver = userRepository.findById(message.getReceiverId()).orElse(null);

        model.addAttribute("message", message);
        model.addAttribute("sender", sender);
        model.addAttribute("receiver", receiver);

        return "admin/messages/details";
    }

    // Show compose message form
    @GetMapping("/messages/compose")
    public String composeMessageForm(Model model) {
        List<User> allResidents = userRepository.findByRole(
                com.syndico.syndicoapp.models.enums.UserRole.RESIDENT
        );

        model.addAttribute("residents", allResidents);
        return "admin/messages/compose";
    }

    // Send message
    @PostMapping("/messages/send")
    public String sendMessage(@RequestParam("recipientType") String recipientType,
                              @RequestParam(value = "recipientIds", required = false) String recipientIds,
                              @RequestParam("subject") String subject,
                              @RequestParam("content") String content,
                              RedirectAttributes redirectAttributes,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long senderId = userDetails.getId();

            if ("all".equals(recipientType)) {
                // Send to all residents
                messageService.sendMessageToAllResidents(senderId, subject, content);
                redirectAttributes.addFlashAttribute("success", "Message sent to all residents successfully");
            } else if ("individual".equals(recipientType) && recipientIds != null && !recipientIds.isEmpty()) {
                // Send to selected residents
                List<Long> receiverIds = Arrays.stream(recipientIds.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                messageService.sendBroadcastMessage(senderId, receiverIds, subject, content);
                redirectAttributes.addFlashAttribute("success",
                        "Message sent to " + receiverIds.size() + " resident(s) successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Please select at least one recipient");
                return "redirect:/admin/messages/compose";
            }

            return "redirect:/admin/messages?filter=sent";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send message: " + e.getMessage());
            return "redirect:/admin/messages/compose";
        }
    }

    // Reply to message
    @PostMapping("/messages/reply/{id}")
    public String replyToMessage(@PathVariable Long id,
                                 @RequestParam("content") String content,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Message originalMessage = messageService.getMessageById(id);
            Long senderId = userDetails.getId();
            Long receiverId = originalMessage.getSenderId(); // Reply to sender
            String subject = "Re: " + originalMessage.getSubject();

            messageService.sendMessage(senderId, receiverId, subject, content);

            redirectAttributes.addFlashAttribute("success", "Reply sent successfully");
            return "redirect:/admin/messages/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send reply: " + e.getMessage());
            return "redirect:/admin/messages/" + id;
        }
    }

    // Delete message
    @PostMapping("/messages/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            messageService.deleteMessage(id);
            redirectAttributes.addFlashAttribute("success", "Message deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete message: " + e.getMessage());
        }
        return "redirect:/admin/messages";
    }

    // Mark as read/unread
    @PostMapping("/messages/mark-read/{id}")
    public String markAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            messageService.markAsRead(id);
            redirectAttributes.addFlashAttribute("success", "Message marked as read");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update message");
        }
        return "redirect:/admin/messages";
    }

    @PostMapping("/messages/mark-unread/{id}")
    public String markAsUnread(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            messageService.markAsUnread(id);
            redirectAttributes.addFlashAttribute("success", "Message marked as unread");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update message");
        }
        return "redirect:/admin/messages";
    }

    // ========== CHATBOT CONFIG ==========
    @GetMapping("/chatbot-config")
    public String chatbotConfig(@RequestParam(required = false) String search, Model model) {
        List<ChatbotQA> qas;

        if (search != null && !search.isEmpty()) {
            qas = chatbotQAService.searchQAs(search);
        } else {
            qas = chatbotQAService.getAllQAs();
        }

        ChatbotQAService.ChatbotStatistics stats = chatbotQAService.getStatistics();

        model.addAttribute("qas", qas);
        model.addAttribute("statistics", stats);
        model.addAttribute("searchQuery", search);

        return "admin/chatbot/config";
    }

    @PostMapping("/chatbot-config/create")
    public String createChatbotQA(@ModelAttribute ChatbotQA chatbotQA, RedirectAttributes redirectAttributes) {
        try {
            chatbotQAService.createQA(chatbotQA);
            redirectAttributes.addFlashAttribute("successMessage", "Q&A created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/chatbot-config";
    }

    @PostMapping("/chatbot-config/update/{id}")
    public String updateChatbotQA(@PathVariable Long id, @ModelAttribute ChatbotQA chatbotQA,
                                  RedirectAttributes redirectAttributes) {
        try {
            chatbotQAService.updateQA(id, chatbotQA);
            redirectAttributes.addFlashAttribute("successMessage", "Q&A updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/chatbot-config";
    }

    @PostMapping("/chatbot-config/delete/{id}")
    public String deleteChatbotQA(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            chatbotQAService.deleteQA(id);
            redirectAttributes.addFlashAttribute("successMessage", "Q&A deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/chatbot-config";
    }

    @PostMapping("/chatbot-config/{id}/toggle")
    public String toggleChatbotQAStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            chatbotQAService.toggleActiveStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/chatbot-config";
    }

    // ========== NOTIFICATIONS ==========
    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getId();
        model.addAttribute("notifications", notificationService.findByUserId(userId));
        model.addAttribute("unreadCount", notificationService.countUnread(userId));
        model.addAttribute("activePage", "notifications");
        return "admin/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            notificationService.markAsRead(id, userDetails.getId());
            return ResponseEntity.ok(Map.of("status", "success", "message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/notifications/read-all")
    public String markAllNotificationsAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAllAsRead(userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "All notifications have been marked as read");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notifications";
    }

    @PostMapping("/notifications/{id}/delete")
    public String deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            notificationService.delete(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Notification deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/notifications";
    }

    // ========== SETTINGS ==========
    @GetMapping("/settings")
    public String settings(Model model) {
        // Add any settings data you need
        model.addAttribute("appVersion", "1.0.0");
        model.addAttribute("appName", "Syndico");

        return "admin/settings/index";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam Map<String, String> settings,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Save settings logic here
            redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}
