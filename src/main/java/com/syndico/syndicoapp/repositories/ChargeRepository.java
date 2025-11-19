package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Charge;
import com.syndico.syndicoapp.models.enums.ChargeStatus;
import com.syndico.syndicoapp.models.enums.ChargeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {
    List<Charge> findByResidentId(Long residentId);
    List<Charge> findByStatus(ChargeStatus status);
    List<Charge> findByType(ChargeType type);

    List<Charge> findByResidentIdAndStatus(Long residentId, ChargeStatus status);

    Optional<Charge> findByInvoiceNumber(String invoiceNumber);

    List<Charge> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT c FROM Charge c WHERE c.resident.id = :residentId AND c.status = :status ORDER BY c.dueDate DESC")
    List<Charge> findByResidentAndStatusOrderByDueDateDesc(@Param("residentId") Long residentId,
                                                           @Param("status") ChargeStatus status);

    @Query("SELECT c FROM Charge c WHERE c.dueDate < :currentDate AND c.status = 'EN_ATTENTE'")
    List<Charge> findOverdueCharges(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT SUM(c.amount) FROM Charge c WHERE c.resident.id = :residentId AND c.status = 'PAYEE'")
    Double getTotalPaidByResident(@Param("residentId") Long residentId);

    @Query("SELECT SUM(c.amount) FROM Charge c WHERE c.resident.id = :residentId AND c.status = 'EN_ATTENTE'")
    Double getTotalUnpaidByResident(@Param("residentId") Long residentId);
}
