package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.services.ChargeService;
import com.syndico.syndicoapp.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/financial-reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final PaymentService paymentService;
    private final ChargeService chargeService;

    @GetMapping
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
        var payments = paymentService.getPaymentsByDateRange(startDate, endDate);
        var charges = chargeService.getChargesByDateRange(
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

    @GetMapping("/download")
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
}

