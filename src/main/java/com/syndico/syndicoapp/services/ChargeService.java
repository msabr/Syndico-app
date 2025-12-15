package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Charge;
import com.syndico.syndicoapp.models.enums.ChargeStatus;
import com.syndico.syndicoapp.models.enums.ChargeType;
import com.syndico.syndicoapp.repositories.ChargeRepository;
import com.syndico.syndicoapp.repositories.ResidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ChargeService {

    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private ResidentRepository residentRepository;

    // Create a new charge
    public Charge createCharge(Charge charge) {
        // Generate unique invoice number if not provided
        if (charge.getInvoiceNumber() == null || charge.getInvoiceNumber().isEmpty()) {
            charge.setInvoiceNumber(generateInvoiceNumber());
        }

        // Set issue date to today if not provided
        if (charge.getIssueDate() == null) {
            charge.setIssueDate(LocalDate.now());
        }

        // Set default status if not provided
        if (charge.getStatus() == null) {
            charge.setStatus(ChargeStatus.EN_ATTENTE);
        }

        return chargeRepository.save(charge);
    }

    // Get all charges
    public List<Charge> getAllCharges() {
        return chargeRepository.findAll();
    }

    // Get charge by ID
    public Optional<Charge> getChargeById(Long id) {
        return chargeRepository.findById(id);
    }

    // Update charge
    public Charge updateCharge(Long id, Charge chargeDetails) {
        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge not found with id: " + id));

        charge.setType(chargeDetails.getType());
        charge.setAmount(chargeDetails.getAmount());
        charge.setDueDate(chargeDetails.getDueDate());
        charge.setDescription(chargeDetails.getDescription());
        charge.setStatus(chargeDetails.getStatus());

        // Only update resident if provided
        if (chargeDetails.getResident() != null) {
            charge.setResident(chargeDetails.getResident());
        }

        return chargeRepository.save(charge);
    }

    // Delete charge
    public void deleteCharge(Long id) {
        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge not found with id: " + id));
        chargeRepository.delete(charge);
    }

    // Get charges by resident
    public List<Charge> getChargesByResident(Long residentId) {
        return chargeRepository.findByResidentId(residentId);
    }

    // Get charges by status
    public List<Charge> getChargesByStatus(ChargeStatus status) {
        return chargeRepository.findByStatus(status);
    }

    // Get charges by type
    public List<Charge> getChargesByType(ChargeType type) {
        return chargeRepository.findByType(type);
    }

    // Get overdue charges
    public List<Charge> getOverdueCharges() {
        return chargeRepository.findOverdueCharges(LocalDate.now());
    }

    // Mark charge as paid
    public Charge markAsPaid(Long chargeId, String paymentMethod) {
        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new RuntimeException("Charge not found with id: " + chargeId));

        charge.markAsPaid(paymentMethod);
        return chargeRepository.save(charge);
    }

    // Check if charge is overdue
    public boolean isChargeOverdue(Long chargeId) {
        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new RuntimeException("Charge not found with id: " + chargeId));
        return charge.isOverdue();
    }

    // Update overdue charges status
    public void updateOverdueCharges() {
        List<Charge> overdueCharges = chargeRepository.findOverdueCharges(LocalDate.now());
        for (Charge charge : overdueCharges) {
            charge.setStatus(ChargeStatus.EN_RETARD);
        }
        chargeRepository.saveAll(overdueCharges);
    }

    // Get total paid by resident
    public Double getTotalPaidByResident(Long residentId) {
        Double total = chargeRepository.getTotalPaidByResident(residentId);
        return total != null ? total : 0.0;
    }

    // Get total unpaid by resident
    public Double getTotalUnpaidByResident(Long residentId) {
        Double total = chargeRepository.getTotalUnpaidByResident(residentId);
        return total != null ? total : 0.0;
    }

    // Get charges by date range
    public List<Charge> getChargesByDateRange(LocalDate startDate, LocalDate endDate) {
        return chargeRepository.findByDueDateBetween(startDate, endDate);
    }

    // Generate unique invoice number
    private String generateInvoiceNumber() {
        String prefix = "INV";
        String year = String.valueOf(LocalDate.now().getYear());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "-" + year + "-" + uuid;
    }

    // Calculate statistics
    public ChargeStatistics getChargeStatistics() {
        List<Charge> allCharges = chargeRepository.findAll();

        double totalAmount = allCharges.stream()
                .map(Charge::getAmount)
                .filter(amount -> amount != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double paidAmount = allCharges.stream()
                .filter(c -> c.getStatus() == ChargeStatus.PAYEE)
                .map(Charge::getAmount)
                .filter(amount -> amount != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double unpaidAmount = allCharges.stream()
                .filter(c -> c.getStatus() == ChargeStatus.EN_ATTENTE || c.getStatus() == ChargeStatus.EN_RETARD)
                .map(Charge::getAmount)
                .filter(amount -> amount != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        long overdueCount = allCharges.stream()
                .filter(c -> c.getStatus() == ChargeStatus.EN_RETARD)
                .count();

        return new ChargeStatistics(totalAmount, paidAmount, unpaidAmount, overdueCount);
    }

    // Inner class for statistics
    public static class ChargeStatistics {
        private double totalAmount;
        private double paidAmount;
        private double unpaidAmount;
        private long overdueCount;

        public ChargeStatistics(double totalAmount, double paidAmount, double unpaidAmount, long overdueCount) {
            this.totalAmount = totalAmount;
            this.paidAmount = paidAmount;
            this.unpaidAmount = unpaidAmount;
            this.overdueCount = overdueCount;
        }

        // Getters
        public double getTotalAmount() { return totalAmount; }
        public double getPaidAmount() { return paidAmount; }
        public double getUnpaidAmount() { return unpaidAmount; }
        public long getOverdueCount() { return overdueCount; }
        public double getCollectionRate() {
            return totalAmount > 0 ? (paidAmount / totalAmount) * 100 : 0;
        }
    }
}
