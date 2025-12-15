package com.syndico.syndicoapp.models;

import com.syndico.syndicoapp.models.enums.ChargeStatus;
import com.syndico.syndicoapp.models.enums.ChargeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "charges", indexes = {
        @Index(name = "idx_resident", columnList = "resident_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Charge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le type de charge est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeType type;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    @DecimalMax(value = "1000000.00", message = "Le montant ne peut pas dépasser 1 000 000")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "La date d'échéance est obligatoire")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull(message = "La date d'émission est obligatoire")
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChargeStatus status = ChargeStatus.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "invoice_number", unique = true, length = 100)
    private String invoiceNumber;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @OneToOne(mappedBy = "charge", cascade = CascadeType.ALL)
    private Payment payment;

    // Méthodes métier
    public boolean isOverdue() {
        return status == ChargeStatus.EN_ATTENTE
                && LocalDate.now().isAfter(dueDate);
    }

    public void markAsPaid(String method) {
        this.status = ChargeStatus.PAYEE;
        this.paidAt = LocalDateTime.now();
        this.paymentMethod = method;
    }

    @PrePersist
    public void generateInvoiceNumber() {
        if (invoiceNumber == null) {
            this.invoiceNumber = "INV-" + System.currentTimeMillis();
        }
    }
}
