package com.syndico.syndicoapp.services;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.syndico.syndicoapp.models.Payment;
import com.syndico.syndicoapp.models.Charge;
import com.syndico.syndicoapp.models.enums.PaymentStatus;
import com.syndico.syndicoapp.repositories.PaymentRepository;
import com.syndico.syndicoapp.repositories.ChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ChargeRepository chargeRepository;

    // Create payment
    public Payment createPayment(Payment payment) {
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING);
        }
        return paymentRepository.save(payment);
    }

    // Get all payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Get payment by ID
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    // Get payment by charge ID
    public Optional<Payment> getPaymentByChargeId(Long chargeId) {
        return paymentRepository.findByChargeId(chargeId);
    }

    // Get payment by transaction ID
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    // Get payments by status
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    // Get payments by payment method
    public List<Payment> getPaymentsByMethod(String method) {
        return paymentRepository.findByPaymentMethod(method);
    }

    // Get payments by date range
    public List<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate);
    }

    // Update payment status
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    // Delete payment
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        paymentRepository.delete(payment);
    }

    // Calculate payment statistics
    public PaymentStatistics getPaymentStatistics() {
        List<Payment> allPayments = paymentRepository.findAll();

        long totalPayments = allPayments.size();

        double totalAmount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum();

        long successfulPayments = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .count();

        long failedPayments = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .count();

        long pendingPayments = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();

        // Group by payment method
        Map<String, Long> paymentsByMethod = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        p -> p.getPaymentMethod() != null ? p.getPaymentMethod() : "Unknown",
                        Collectors.counting()
                ));

        return new PaymentStatistics(
                totalPayments,
                totalAmount,
                successfulPayments,
                failedPayments,
                pendingPayments,
                paymentsByMethod
        );
    }

    // Generate payment receipt PDF
    public byte[] generatePaymentReceipt(Long paymentId) throws IOException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Charge charge = payment.getCharge();
        if (charge == null) {
            throw new RuntimeException("No charge associated with this payment");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        Paragraph title = new Paragraph("PAYMENT RECEIPT")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold();
        document.add(title);

        document.add(new Paragraph("\n"));

        // Company info
        document.add(new Paragraph("SYNDICO Management")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12));
        document.add(new Paragraph("Property Management Services")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        document.add(new Paragraph("\n\n"));

        // Payment details table
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        addTableRow(table, "Receipt Number:", "REC-" + payment.getId());
        addTableRow(table, "Payment Date:", payment.getPaymentDate().format(formatter));
        addTableRow(table, "Transaction ID:", payment.getTransactionId() != null ? payment.getTransactionId() : "N/A");
        addTableRow(table, "Payment Method:", payment.getPaymentMethod());
        addTableRow(table, "Status:", payment.getStatus().toString());

        table.addCell(new Paragraph("\n").setBold());
        table.addCell(new Paragraph("\n"));

        addTableRow(table, "Invoice Number:", charge.getInvoiceNumber());
        addTableRow(table, "Charge Type:", charge.getType().toString());
        addTableRow(table, "Description:", charge.getDescription() != null ? charge.getDescription() : "N/A");

        if (charge.getResident() != null && charge.getResident().getUser() != null) {
            addTableRow(table, "Resident:",
                    charge.getResident().getUser().getFirstName() + " " +
                            charge.getResident().getUser().getLastName());
            addTableRow(table, "Apartment:", charge.getResident().getApartmentNumber());
        }

        table.addCell(new Paragraph("\n").setBold());
        table.addCell(new Paragraph("\n"));

        addTableRow(table, "Amount Paid:", String.format("%.2f DH", payment.getAmount()));

        document.add(table);

        document.add(new Paragraph("\n\n"));
        document.add(new Paragraph("Thank you for your payment!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setItalic());

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(formatter))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8));

        document.close();
        return baos.toByteArray();
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Paragraph(label).setBold());
        table.addCell(new Paragraph(value));
    }

    // Inner class for statistics
    public static class PaymentStatistics {
        private long totalPayments;
        private double totalAmount;
        private long successfulPayments;
        private long failedPayments;
        private long pendingPayments;
        private Map<String, Long> paymentsByMethod;

        public PaymentStatistics(long totalPayments, double totalAmount, long successfulPayments,
                                 long failedPayments, long pendingPayments, Map<String, Long> paymentsByMethod) {
            this.totalPayments = totalPayments;
            this.totalAmount = totalAmount;
            this.successfulPayments = successfulPayments;
            this.failedPayments = failedPayments;
            this.pendingPayments = pendingPayments;
            this.paymentsByMethod = paymentsByMethod;
        }

        // Getters
        public long getTotalPayments() { return totalPayments; }
        public double getTotalAmount() { return totalAmount; }
        public long getSuccessfulPayments() { return successfulPayments; }
        public long getFailedPayments() { return failedPayments; }
        public long getPendingPayments() { return pendingPayments; }
        public Map<String, Long> getPaymentsByMethod() { return paymentsByMethod; }
        public double getSuccessRate() {
            return totalPayments > 0 ? ((double) successfulPayments / totalPayments) * 100 : 0;
        }
    }

}
