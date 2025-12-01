package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long>, JpaSpecificationExecutor<Resident> {
    Optional<Resident> findByUserId(Long userId);
    List<Resident> findByBuildingId(Long buildingId);
    List<Resident> findByApartmentNumber(String apartmentNumber);
    long countByUserIsEmailVerified(boolean isEmailVerified);
    long countByIsOwner(boolean isOwner);
    List<Resident> findByIsOwner(Boolean isOwner);
}
