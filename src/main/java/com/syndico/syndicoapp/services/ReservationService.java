package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Reservation;
import com.syndico.syndicoapp.models.enums.ReservationStatus;
import com.syndico.syndicoapp.models.enums.SpaceType;
import com.syndico.syndicoapp.repositories.ReservationRepository;
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
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ResidentRepository residentRepository;

    // Create reservation
    public Reservation createReservation(Reservation reservation) {
        if (reservation.getStatus() == null) {
            reservation.setStatus(ReservationStatus.EN_ATTENTE);
        }
        return reservationRepository.save(reservation);
    }

    // Get all reservations
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Get reservation by ID
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    // Update reservation
    public Reservation updateReservation(Long id, Reservation reservationDetails) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

        reservation.setSpaceType(reservationDetails.getSpaceType());
        reservation.setStartDateTime(reservationDetails.getStartDateTime());
        reservation.setEndDateTime(reservationDetails.getEndDateTime());
        reservation.setStatus(reservationDetails.getStatus());
        reservation.setNotes(reservationDetails.getNotes());

        return reservationRepository.save(reservation);
    }

    // Delete reservation
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));
        reservationRepository.delete(reservation);
    }

    // Get reservations by resident
    public List<Reservation> getReservationsByResident(Long residentId) {
        return reservationRepository.findByResidentId(residentId);
    }

    // Get reservations by space type
    public List<Reservation> getReservationsBySpaceType(SpaceType spaceType) {
        return reservationRepository.findBySpaceType(spaceType);
    }

    // Get reservations by status
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }

    // Get pending reservations (awaiting approval)
    public List<Reservation> getPendingReservations() {
        return getReservationsByStatus(ReservationStatus.EN_ATTENTE);
    }

    // Get upcoming reservations (confirmed and in future)
    public List<Reservation> getUpcomingReservations() {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .filter(r -> r.getStartDateTime().isAfter(now))
                .sorted((r1, r2) -> r1.getStartDateTime().compareTo(r2.getStartDateTime()))
                .collect(Collectors.toList());
    }

    // Check if space is available for given time slot
    public boolean isSpaceAvailable(SpaceType spaceType, LocalDateTime startDateTime, LocalDateTime endDateTime, Long excludeReservationId) {
        List<Reservation> existingReservations = reservationRepository.findBySpaceType(spaceType).stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE || r.getStatus() == ReservationStatus.EN_ATTENTE)
                .filter(r -> excludeReservationId == null || !r.getId().equals(excludeReservationId))
                .collect(Collectors.toList());

        for (Reservation existing : existingReservations) {
            // Check for overlap
            if (startDateTime.isBefore(existing.getEndDateTime()) && endDateTime.isAfter(existing.getStartDateTime())) {
                return false; // Overlap detected
            }
        }
        return true;
    }

    // Approve reservation
    public Reservation approveReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        // Check if space is still available
        if (!isSpaceAvailable(reservation.getSpaceType(), reservation.getStartDateTime(),
                reservation.getEndDateTime(), reservationId)) {
            throw new RuntimeException("Space is no longer available for this time slot");
        }

        reservation.setStatus(ReservationStatus.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    // Reject reservation
    public Reservation rejectReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        reservation.setStatus(ReservationStatus.REFUSEE);
        return reservationRepository.save(reservation);
    }

    // Cancel reservation
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        reservation.setStatus(ReservationStatus.ANNULEE);
        return reservationRepository.save(reservation);
    }

    // Get reservations for a specific date
    public List<Reservation> getReservationsByDate(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);

        return reservationRepository.findAll().stream()
                .filter(r -> !r.getStartDateTime().isAfter(endOfDay) && !r.getEndDateTime().isBefore(startOfDay))
                .collect(Collectors.toList());
    }

    // Get reservation statistics
    public ReservationStatistics getReservationStatistics() {
        List<Reservation> allReservations = reservationRepository.findAll();

        long totalReservations = allReservations.size();

        long pendingReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.EN_ATTENTE)
                .count();

        long confirmedReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .count();

        long rejectedReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.REFUSEE)
                .count();

        long cancelledReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.ANNULEE)
                .count();

        // Upcoming reservations (confirmed and in future)
        LocalDateTime now = LocalDateTime.now();
        long upcomingReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .filter(r -> r.getStartDateTime().isAfter(now))
                .count();

        // Group by space type
        Map<SpaceType, Long> bySpaceType = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .collect(Collectors.groupingBy(Reservation::getSpaceType, Collectors.counting()));

        // Approval rate
        long totalProcessed = confirmedReservations + rejectedReservations;
        double approvalRate = totalProcessed > 0 ? ((double) confirmedReservations / totalProcessed) * 100 : 0;

        return new ReservationStatistics(
                totalReservations,
                pendingReservations,
                confirmedReservations,
                rejectedReservations,
                cancelledReservations,
                upcomingReservations,
                bySpaceType,
                approvalRate
        );
    }

    // Inner class for statistics
    public static class ReservationStatistics {
        private long totalReservations;
        private long pendingReservations;
        private long confirmedReservations;
        private long rejectedReservations;
        private long cancelledReservations;
        private long upcomingReservations;
        private Map<SpaceType, Long> bySpaceType;
        private double approvalRate;

        public ReservationStatistics(long totalReservations, long pendingReservations,
                                     long confirmedReservations, long rejectedReservations,
                                     long cancelledReservations, long upcomingReservations,
                                     Map<SpaceType, Long> bySpaceType, double approvalRate) {
            this.totalReservations = totalReservations;
            this.pendingReservations = pendingReservations;
            this.confirmedReservations = confirmedReservations;
            this.rejectedReservations = rejectedReservations;
            this.cancelledReservations = cancelledReservations;
            this.upcomingReservations = upcomingReservations;
            this.bySpaceType = bySpaceType;
            this.approvalRate = approvalRate;
        }

        // Getters
        public long getTotalReservations() { return totalReservations; }
        public long getPendingReservations() { return pendingReservations; }
        public long getConfirmedReservations() { return confirmedReservations; }
        public long getRejectedReservations() { return rejectedReservations; }
        public long getCancelledReservations() { return cancelledReservations; }
        public long getUpcomingReservations() { return upcomingReservations; }
        public Map<SpaceType, Long> getBySpaceType() { return bySpaceType; }
        public double getApprovalRate() { return approvalRate; }
    }
}
