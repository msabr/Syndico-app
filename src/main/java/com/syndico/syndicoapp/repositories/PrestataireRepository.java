package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Prestataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrestataireRepository extends JpaRepository<Prestataire, Long> {
    Optional<Prestataire> findByEmail(String email);

    List<Prestataire> findByIsActiveTrue();
    List<Prestataire> findBySpecialty(String specialty);

    List<Prestataire> findByCompanyNameContainingIgnoreCase(String companyName);

    @Query("SELECT p FROM Prestataire p WHERE p.isActive = true ORDER BY p.rating DESC")
    List<Prestataire> findActivePrestatairesOrderedByRating();

    @Query("SELECT p FROM Prestataire p WHERE p.rating >= :minRating AND p.isActive = true")
    List<Prestataire> findByMinRating(Double minRating);
}
