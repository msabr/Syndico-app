package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.AssemblyMeeting;
import com.syndico.syndicoapp.models.enums.MeetingStatus;
import com.syndico.syndicoapp.repositories.AssemblyMeetingRepository;
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
public class AssemblyMeetingService {

    @Autowired
    private AssemblyMeetingRepository assemblyMeetingRepository;

    // Create assembly meeting
    public AssemblyMeeting createAssemblyMeeting(AssemblyMeeting meeting) {
        if (meeting.getCreatedAt() == null) {
            meeting.setCreatedAt(LocalDateTime.now());
        }
        if (meeting.getStatus() == null) {
            meeting.setStatus(MeetingStatus.PLANIFIEE);
        }
        return assemblyMeetingRepository.save(meeting);
    }

    // Get all meetings
    public List<AssemblyMeeting> getAllMeetings() {
        return assemblyMeetingRepository.findAll();
    }

    // Get meeting by ID
    public Optional<AssemblyMeeting> getMeetingById(Long id) {
        return assemblyMeetingRepository.findById(id);
    }

    // Update meeting
    public AssemblyMeeting updateMeeting(Long id, AssemblyMeeting meetingDetails) {
        AssemblyMeeting meeting = assemblyMeetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + id));

        meeting.setTitle(meetingDetails.getTitle());
        meeting.setScheduledDate(meetingDetails.getScheduledDate());
        meeting.setLocation(meetingDetails.getLocation());
        meeting.setAgenda(meetingDetails.getAgenda());
        meeting.setStatus(meetingDetails.getStatus());

        if (meetingDetails.getMinutesUrl() != null) {
            meeting.setMinutesUrl(meetingDetails.getMinutesUrl());
        }

        return assemblyMeetingRepository.save(meeting);
    }

    // Delete meeting
    public void deleteMeeting(Long id) {
        AssemblyMeeting meeting = assemblyMeetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + id));
        assemblyMeetingRepository.delete(meeting);
    }

    // Get meetings by status
    public List<AssemblyMeeting> getMeetingsByStatus(MeetingStatus status) {
        return assemblyMeetingRepository.findByStatus(status);
    }

    // Get upcoming meetings
    public List<AssemblyMeeting> getUpcomingMeetings() {
        LocalDateTime now = LocalDateTime.now();
        return assemblyMeetingRepository.findAll().stream()
                .filter(m -> m.getScheduledDate().isAfter(now))
                .filter(m -> m.getStatus() == MeetingStatus.PLANIFIEE)
                .sorted((m1, m2) -> m1.getScheduledDate().compareTo(m2.getScheduledDate()))
                .collect(Collectors.toList());
    }

    // Get past meetings
    public List<AssemblyMeeting> getPastMeetings() {
        LocalDateTime now = LocalDateTime.now();
        return assemblyMeetingRepository.findAll().stream()
                .filter(m -> m.getScheduledDate().isBefore(now))
                .filter(m -> m.getStatus() == MeetingStatus.TERMINEE)
                .sorted((m1, m2) -> m2.getScheduledDate().compareTo(m1.getScheduledDate()))
                .collect(Collectors.toList());
    }

    // Update meeting status
    public AssemblyMeeting updateStatus(Long meetingId, MeetingStatus newStatus) {
        AssemblyMeeting meeting = assemblyMeetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + meetingId));

        meeting.setStatus(newStatus);
        return assemblyMeetingRepository.save(meeting);
    }

    // Upload meeting minutes
    public AssemblyMeeting uploadMinutes(Long meetingId, String minutesUrl) {
        AssemblyMeeting meeting = assemblyMeetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + meetingId));

        meeting.setMinutesUrl(minutesUrl);
        meeting.setStatus(MeetingStatus.TERMINEE);
        return assemblyMeetingRepository.save(meeting);
    }

    // Get meeting statistics
    public MeetingStatistics getMeetingStatistics() {
        List<AssemblyMeeting> allMeetings = assemblyMeetingRepository.findAll();

        long totalMeetings = allMeetings.size();

        long plannedMeetings = allMeetings.stream()
                .filter(m -> m.getStatus() == MeetingStatus.PLANIFIEE)
                .count();

        long inProgressMeetings = allMeetings.stream()
                .filter(m -> m.getStatus() == MeetingStatus.EN_COURS)
                .count();

        long completedMeetings = allMeetings.stream()
                .filter(m -> m.getStatus() == MeetingStatus.TERMINEE)
                .count();

        long cancelledMeetings = allMeetings.stream()
                .filter(m -> m.getStatus() == MeetingStatus.ANNULEE)
                .count();

        // Upcoming meetings count
        LocalDateTime now = LocalDateTime.now();
        long upcomingCount = allMeetings.stream()
                .filter(m -> m.getScheduledDate().isAfter(now))
                .filter(m -> m.getStatus() == MeetingStatus.PLANIFIEE)
                .count();

        // Group by status
        Map<MeetingStatus, Long> byStatus = allMeetings.stream()
                .collect(Collectors.groupingBy(AssemblyMeeting::getStatus, Collectors.counting()));

        return new MeetingStatistics(
                totalMeetings,
                plannedMeetings,
                inProgressMeetings,
                completedMeetings,
                cancelledMeetings,
                upcomingCount,
                byStatus
        );
    }

    // Inner class for statistics
    public static class MeetingStatistics {
        private long totalMeetings;
        private long plannedMeetings;
        private long inProgressMeetings;
        private long completedMeetings;
        private long cancelledMeetings;
        private long upcomingCount;
        private Map<MeetingStatus, Long> byStatus;

        public MeetingStatistics(long totalMeetings, long plannedMeetings,
                                 long inProgressMeetings, long completedMeetings,
                                 long cancelledMeetings, long upcomingCount,
                                 Map<MeetingStatus, Long> byStatus) {
            this.totalMeetings = totalMeetings;
            this.plannedMeetings = plannedMeetings;
            this.inProgressMeetings = inProgressMeetings;
            this.completedMeetings = completedMeetings;
            this.cancelledMeetings = cancelledMeetings;
            this.upcomingCount = upcomingCount;
            this.byStatus = byStatus;
        }

        // Getters
        public long getTotalMeetings() { return totalMeetings; }
        public long getPlannedMeetings() { return plannedMeetings; }
        public long getInProgressMeetings() { return inProgressMeetings; }
        public long getCompletedMeetings() { return completedMeetings; }
        public long getCancelledMeetings() { return cancelledMeetings; }
        public long getUpcomingCount() { return upcomingCount; }
        public Map<MeetingStatus, Long> getByStatus() { return byStatus; }
    }
}
