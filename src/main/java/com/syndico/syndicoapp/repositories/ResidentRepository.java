package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long>, JpaSpecificationExecutor<Resident> {
    Optional<Resident> findByUser_Id(Long userId);
    List<Resident> findByBuilding_Id(Long buildingId);
    long countByUser_IsEmailVerified(boolean isEmailVerified);
    long countByIsOwner(boolean isOwner);

    // Find residents by apartment number
    List<Resident> findByApartmentNumberContainingIgnoreCase(String apartmentNumber);

    // Find owners only
    List<Resident> findByIsOwnerTrue();

    // Find tenants only (non-owners)
    List<Resident> findByIsOwnerFalse();

    // Check if apartment is occupied
    boolean existsByBuilding_IdAndApartmentNumber(Long buildingId, String apartmentNumber);
}
