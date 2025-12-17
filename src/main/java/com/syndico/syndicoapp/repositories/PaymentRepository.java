package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Payment;
import com.syndico.syndicoapp.models.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByChargeId(Long chargeId);
    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByPaymentMethod(String paymentMethod);

    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> findByChargeResidentIdAndPaymentDateBetween(Long residentId, LocalDateTime startDate, LocalDateTime endDate);

}
