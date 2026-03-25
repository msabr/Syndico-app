package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Prestataire;
import com.syndico.syndicoapp.repositories.PrestataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrestataireService {

    @Autowired
    private PrestataireRepository prestataireRepository;

    // Create prestataire
    public Prestataire createPrestataire(Prestataire prestataire) {
        if (prestataire.getIsActive() == null) {
            prestataire.setIsActive(true);
        }
        if (prestataire.getRating() == null) {
            prestataire.setRating(0.0);
        }
        return prestataireRepository.save(prestataire);
    }

    // Get all prestataires
    public List<Prestataire> getAllPrestataires() {
        return prestataireRepository.findAll();
    }

    // Get prestataire by ID
    public Optional<Prestataire> getPrestataireById(Long id) {
        return prestataireRepository.findById(id);
    }

    // Update prestataire
    public Prestataire updatePrestataire(Long id, Prestataire prestataireDetails) {
        Prestataire prestataire = prestataireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestataire not found with id: " + id));

        prestataire.setCompanyName(prestataireDetails.getCompanyName());
        prestataire.setContactPerson(prestataireDetails.getContactPerson());
        prestataire.setPhoneNumber(prestataireDetails.getPhoneNumber());
        prestataire.setEmail(prestataireDetails.getEmail());
        prestataire.setSpecialty(prestataireDetails.getSpecialty());
        prestataire.setAddress(prestataireDetails.getAddress());
        prestataire.setIsActive(prestataireDetails.getIsActive());

        return prestataireRepository.save(prestataire);
    }

    // Delete prestataire
    public void deletePrestataire(Long id) {
        Prestataire prestataire = prestataireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestataire not found with id: " + id));
        prestataireRepository.delete(prestataire);
    }

    // Get active prestataires
    public List<Prestataire> getActivePrestataires() {
        return prestataireRepository.findByIsActiveTrue();
    }

    // Get prestataires by specialty
    public List<Prestataire> getPrestatairesBySpecialty(String specialty) {
        return prestataireRepository.findBySpecialty(specialty);
    }

    // Search prestataires by company name
    public List<Prestataire> searchByCompanyName(String companyName) {
        return prestataireRepository.findByCompanyNameContainingIgnoreCase(companyName);
    }

    // Get prestataires ordered by rating
    public List<Prestataire> getPrestatairesByRating() {
        return prestataireRepository.findActivePrestatairesOrderedByRating();
    }

    // Get prestataires by minimum rating
    public List<Prestataire> getPrestatairesByMinRating(Double minRating) {
        return prestataireRepository.findByMinRating(minRating);
    }

    // Update rating
    public Prestataire updateRating(Long prestataireId, Double newRating) {
        Prestataire prestataire = prestataireRepository.findById(prestataireId)
                .orElseThrow(() -> new RuntimeException("Prestataire not found with id: " + prestataireId));

        if (newRating < 0.0 || newRating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }

        prestataire.setRating(newRating);
        return prestataireRepository.save(prestataire);
    }

    // Toggle active status
    public Prestataire toggleActiveStatus(Long prestataireId) {
        Prestataire prestataire = prestataireRepository.findById(prestataireId)
                .orElseThrow(() -> new RuntimeException("Prestataire not found with id: " + prestataireId));

        prestataire.setIsActive(!prestataire.getIsActive());
        return prestataireRepository.save(prestataire);
    }

    // Get prestataire statistics
    public PrestataireStatistics getPrestataireStatistics() {
        List<Prestataire> allPrestataires = prestataireRepository.findAll();

        long totalPrestataires = allPrestataires.size();
        long activePrestataires = allPrestataires.stream()
                .filter(Prestataire::getIsActive)
                .count();
        long inactivePrestataires = totalPrestataires - activePrestataires;

        double averageRating = allPrestataires.stream()
                .filter(p -> p.getRating() != null)
                .mapToDouble(Prestataire::getRating)
                .average()
                .orElse(0.0);

        // Group by specialty
        Map<String, Long> bySpecialty = allPrestataires.stream()
                .filter(p -> p.getSpecialty() != null && !p.getSpecialty().isEmpty())
                .collect(Collectors.groupingBy(Prestataire::getSpecialty, Collectors.counting()));

        // Top rated prestataires (rating >= 4.0)
        long topRatedCount = allPrestataires.stream()
                .filter(p -> p.getRating() != null && p.getRating() >= 4.0)
                .count();

        return new PrestataireStatistics(
                totalPrestataires,
                activePrestataires,
                inactivePrestataires,
                averageRating,
                bySpecialty,
                topRatedCount
        );
    }

    // Inner class for statistics
    public static class PrestataireStatistics {
        private long totalPrestataires;
        private long activePrestataires;
        private long inactivePrestataires;
        private double averageRating;
        private Map<String, Long> bySpecialty;
        private long topRatedCount;

        public PrestataireStatistics(long totalPrestataires, long activePrestataires,
                                     long inactivePrestataires, double averageRating,
                                     Map<String, Long> bySpecialty, long topRatedCount) {
            this.totalPrestataires = totalPrestataires;
            this.activePrestataires = activePrestataires;
            this.inactivePrestataires = inactivePrestataires;
            this.averageRating = averageRating;
            this.bySpecialty = bySpecialty;
            this.topRatedCount = topRatedCount;
        }

        // Getters
        public long getTotalPrestataires() { return totalPrestataires; }
        public long getActivePrestataires() { return activePrestataires; }
        public long getInactivePrestataires() { return inactivePrestataires; }
        public double getAverageRating() { return averageRating; }
        public Map<String, Long> getBySpecialty() { return bySpecialty; }
        public long getTopRatedCount() { return topRatedCount; }
    }
}
