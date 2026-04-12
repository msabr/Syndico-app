package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.Payment;
import com.syndico.syndicoapp.models.enums.PaymentStatus;
import com.syndico.syndicoapp.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
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

    @GetMapping("/{id}")
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

    @GetMapping("/{id}/receipt")
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

    @PostMapping("/update-status/{id}")
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

    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paymentService.deletePayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting payment: " + e.getMessage());
        }
        return "redirect:/admin/payments";
    }
}

