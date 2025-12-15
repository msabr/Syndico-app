package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Reclamation;
import com.syndico.syndicoapp.models.Prestataire;
import com.syndico.syndicoapp.models.Resident;
import com.syndico.syndicoapp.models.enums.ReclamationStatus;
import com.syndico.syndicoapp.models.enums.ReclamationCategory;
import com.syndico.syndicoapp.models.enums.Priority;
import com.syndico.syndicoapp.repositories.ReclamationRepository;
import com.syndico.syndicoapp.repositories.PrestataireRepository;
import com.syndico.syndicoapp.repositories.ResidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReclamationService {

    @Autowired
    private ReclamationRepository reclamationRepository;

    @Autowired
    private PrestataireRepository prestataireRepository;

    @Autowired
    private ResidentRepository residentRepository;

    // Create reclamation
    public Reclamation createReclamation(Reclamation reclamation) {
        if (reclamation.getCreatedAt() == null) {
            reclamation.setCreatedAt(LocalDateTime.now());
        }
        if (reclamation.getStatus() == null) {
            reclamation.setStatus(ReclamationStatus.NOUVELLE);
        }
        reclamation.setUpdatedAt(LocalDateTime.now());
        return reclamationRepository.save(reclamation);
    }

    // Get all reclamations
    public List<Reclamation> getAllReclamations() {
        return reclamationRepository.findAll();
    }

    // Get reclamation by ID
    public Optional<Reclamation> getReclamationById(Long id) {
        return reclamationRepository.findById(id);
    }

    // Update reclamation
    public Reclamation updateReclamation(Long id, Reclamation reclamationDetails) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamation not found with id: " + id));

        reclamation.setTitle(reclamationDetails.getTitle());
        reclamation.setDescription(reclamationDetails.getDescription());
        reclamation.setCategory(reclamationDetails.getCategory());
        reclamation.setPriority(reclamationDetails.getPriority());
        reclamation.setStatus(reclamationDetails.getStatus());
        reclamation.setUpdatedAt(LocalDateTime.now());

        if (reclamationDetails.getPrestataire() != null) {
            reclamation.setPrestataire(reclamationDetails.getPrestataire());
        }

        return reclamationRepository.save(reclamation);
    }

    // Delete reclamation
    public void deleteReclamation(Long id) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamation not found with id: " + id));
        reclamationRepository.delete(reclamation);
    }

    // Get reclamations by resident
    public List<Reclamation> getReclamationsByResident(Long residentId) {
        return reclamationRepository.findByResidentId(residentId);
    }

    // Get reclamations by status
    public List<Reclamation> getReclamationsByStatus(ReclamationStatus status) {
        return reclamationRepository.findByStatus(status);
    }

    // Get reclamations by category
    public List<Reclamation> getReclamationsByCategory(ReclamationCategory category) {
        return reclamationRepository.findByCategory(category);
    }

    // Get reclamations by priority
    public List<Reclamation> getReclamationsByPriority(Priority priority) {
        return reclamationRepository.findByPriority(priority);
    }

    // Get reclamations by prestataire
    public List<Reclamation> getReclamationsByPrestataire(Long prestataireId) {
        return reclamationRepository.findByPrestataireId(prestataireId);
    }

    // Get pending reclamations ordered by priority
    public List<Reclamation> getPendingReclamationsOrderedByPriority() {
        return reclamationRepository.findPendingReclamationsOrderedByPriority();
    }

    // Get reclamations by building
    public List<Reclamation> getReclamationsByBuilding(Long buildingId) {
        return reclamationRepository.findByBuildingId(buildingId);
    }

    // Assign reclamation to prestataire
    public Reclamation assignToPrestataire(Long reclamationId, Long prestataireId) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new RuntimeException("Reclamation not found with id: " + reclamationId));

        Prestataire prestataire = prestataireRepository.findById(prestataireId)
                .orElseThrow(() -> new RuntimeException("Prestataire not found with id: " + prestataireId));

        reclamation.setPrestataire(prestataire);
        reclamation.setStatus(ReclamationStatus.ASSIGNEE);
        reclamation.setUpdatedAt(LocalDateTime.now());

        return reclamationRepository.save(reclamation);
    }

    // Update reclamation status
    public Reclamation updateStatus(Long reclamationId, ReclamationStatus newStatus) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new RuntimeException("Reclamation not found with id: " + reclamationId));

        reclamation.setStatus(newStatus);
        reclamation.setUpdatedAt(LocalDateTime.now());

        // Set resolved date if status is RESOLUE or FERMEE
        if (newStatus == ReclamationStatus.RESOLUE || newStatus == ReclamationStatus.FERMEE) {
            if (reclamation.getResolvedAt() == null) {
                reclamation.setResolvedAt(LocalDateTime.now());
            }
        }

        return reclamationRepository.save(reclamation);
    }

    // Close reclamation
    public Reclamation closeReclamation(Long reclamationId) {
        return updateStatus(reclamationId, ReclamationStatus.FERMEE);
    }

    // Resolve reclamation
    public Reclamation resolveReclamation(Long reclamationId) {
        return updateStatus(reclamationId, ReclamationStatus.RESOLUE);
    }

    // Get reclamation statistics
    public ReclamationStatistics getReclamationStatistics() {
        List<Reclamation> allReclamations = reclamationRepository.findAll();

        long totalReclamations = allReclamations.size();

        long newReclamations = allReclamations.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.NOUVELLE)
                .count();

        long inProgressReclamations = allReclamations.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.EN_COURS || r.getStatus() == ReclamationStatus.ASSIGNEE)
                .count();

        long resolvedReclamations = allReclamations.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.RESOLUE || r.getStatus() == ReclamationStatus.FERMEE)
                .count();

        long urgentReclamations = allReclamations.stream()
                .filter(r -> r.getPriority() == Priority.URGENTE &&
                        (r.getStatus() == ReclamationStatus.NOUVELLE || r.getStatus() == ReclamationStatus.EN_COURS))
                .count();

        // Group by category
        Map<ReclamationCategory, Long> byCategory = allReclamations.stream()
                .collect(Collectors.groupingBy(Reclamation::getCategory, Collectors.counting()));

        // Group by priority
        Map<Priority, Long> byPriority = allReclamations.stream()
                .collect(Collectors.groupingBy(Reclamation::getPriority, Collectors.counting()));

        // Average resolution time (in hours)
        double avgResolutionTime = allReclamations.stream()
                .filter(r -> r.getResolvedAt() != null && r.getCreatedAt() != null)
                .mapToLong(r -> java.time.Duration.between(r.getCreatedAt(), r.getResolvedAt()).toHours())
                .average()
                .orElse(0.0);

        return new ReclamationStatistics(
                totalReclamations,
                newReclamations,
                inProgressReclamations,
                resolvedReclamations,
                urgentReclamations,
                byCategory,
                byPriority,
                avgResolutionTime
        );
    }

    // Count reclamations by resident and status
    public Long countByResidentAndStatus(Long residentId, ReclamationStatus status) {
        return reclamationRepository.countByResidentAndStatus(residentId, status);
    }

    // Inner class for statistics
    public static class ReclamationStatistics {
        private long totalReclamations;
        private long newReclamations;
        private long inProgressReclamations;
        private long resolvedReclamations;
        private long urgentReclamations;
        private Map<ReclamationCategory, Long> byCategory;
        private Map<Priority, Long> byPriority;
        private double avgResolutionTime;

        public ReclamationStatistics(long totalReclamations, long newReclamations,
                                     long inProgressReclamations, long resolvedReclamations,
                                     long urgentReclamations, Map<ReclamationCategory, Long> byCategory,
                                     Map<Priority, Long> byPriority, double avgResolutionTime) {
            this.totalReclamations = totalReclamations;
            this.newReclamations = newReclamations;
            this.inProgressReclamations = inProgressReclamations;
            this.resolvedReclamations = resolvedReclamations;
            this.urgentReclamations = urgentReclamations;
            this.byCategory = byCategory;
            this.byPriority = byPriority;
            this.avgResolutionTime = avgResolutionTime;
        }

        // Getters
        public long getTotalReclamations() { return totalReclamations; }
        public long getNewReclamations() { return newReclamations; }
        public long getInProgressReclamations() { return inProgressReclamations; }
        public long getResolvedReclamations() { return resolvedReclamations; }
        public long getUrgentReclamations() { return urgentReclamations; }
        public Map<ReclamationCategory, Long> getByCategory() { return byCategory; }
        public Map<Priority, Long> getByPriority() { return byPriority; }
        public double getAvgResolutionTime() { return avgResolutionTime; }
        public double getResolutionRate() {
            return totalReclamations > 0 ? ((double) resolvedReclamations / totalReclamations) * 100 : 0;
        }
    }
}
