package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Reclamation;
import com.syndico.syndicoapp.models.enums.Priority;
import com.syndico.syndicoapp.models.enums.ReclamationCategory;
import com.syndico.syndicoapp.models.enums.ReclamationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    List<Reclamation> findByResidentId(Long residentId);
    List<Reclamation> findByStatus(ReclamationStatus status);
    List<Reclamation> findByCategory(ReclamationCategory category);
    List<Reclamation> findByPriority(Priority priority);
    List<Reclamation> findByPrestataireId(Long prestataireId);

    List<Reclamation> findByResidentIdAndStatus(Long residentId, ReclamationStatus status);

    @Query("SELECT r FROM Reclamation r WHERE r.status IN ('NOUVELLE', 'EN_COURS') ORDER BY r.priority DESC, r.createdAt ASC")
    List<Reclamation> findPendingReclamationsOrderedByPriority();

    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.resident.id = :residentId AND r.status = :status")
    Long countByResidentAndStatus(@Param("residentId") Long residentId, @Param("status") ReclamationStatus status);

    @Query("SELECT r FROM Reclamation r WHERE r.resident.building.id = :buildingId")
    List<Reclamation> findByBuildingId(@Param("buildingId") Long buildingId);
}
