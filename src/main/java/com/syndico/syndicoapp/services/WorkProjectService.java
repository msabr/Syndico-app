package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.WorkProject;
import com.syndico.syndicoapp.models.TimelineStage;
import com.syndico.syndicoapp.models.Prestataire;
import com.syndico.syndicoapp.models.enums.WorkStatus;
import com.syndico.syndicoapp.repositories.WorkProjectRepository;
import com.syndico.syndicoapp.repositories.TimelineStageRepository;
import com.syndico.syndicoapp.repositories.PrestataireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkProjectService {

    @Autowired
    private WorkProjectRepository workProjectRepository;

    @Autowired
    private TimelineStageRepository timelineStageRepository;

    @Autowired
    private PrestataireRepository prestataireRepository;

    // Create work project
    public WorkProject createWorkProject(WorkProject workProject) {
        if (workProject.getStatus() == null) {
            workProject.setStatus(WorkStatus.PLANIFIE);
        }
        return workProjectRepository.save(workProject);
    }

    // Get all work projects
    public List<WorkProject> getAllWorkProjects() {
        return workProjectRepository.findAll();
    }

    // Alias for getAllWorkProjects
    public List<WorkProject> findAll() {
        return getAllWorkProjects();
    }

    // Get work project by ID
    public Optional<WorkProject> getWorkProjectById(Long id) {
        return workProjectRepository.findById(id);
    }

    // Alias for getWorkProjectById that throws exception if not found
    public WorkProject findById(Long id) {
        return getWorkProjectById(id).orElseThrow(() -> new RuntimeException("Work project not found with id: " + id));
    }

    // Update work project
    public WorkProject updateWorkProject(Long id, WorkProject workProjectDetails) {
        WorkProject workProject = workProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work project not found with id: " + id));

        workProject.setTitle(workProjectDetails.getTitle());
        workProject.setDescription(workProjectDetails.getDescription());
        workProject.setStartDate(workProjectDetails.getStartDate());
        workProject.setEndDate(workProjectDetails.getEndDate());
        workProject.setStatus(workProjectDetails.getStatus());
        workProject.setBudget(workProjectDetails.getBudget());

        if (workProjectDetails.getPrestataire() != null) {
            workProject.setPrestataire(workProjectDetails.getPrestataire());
        }

        return workProjectRepository.save(workProject);
    }

    // Delete work project
    public void deleteWorkProject(Long id) {
        WorkProject workProject = workProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work project not found with id: " + id));
        workProjectRepository.delete(workProject);
    }

    // Get work projects by status
    public List<WorkProject> getWorkProjectsByStatus(WorkStatus status) {
        return workProjectRepository.findByStatus(status);
    }

    // Alias for getWorkProjectsByStatus
    public List<WorkProject> findByStatus(WorkStatus status) {
        return getWorkProjectsByStatus(status);
    }

    // Update work project status
    public WorkProject updateStatus(Long workProjectId, WorkStatus newStatus) {
        WorkProject workProject = workProjectRepository.findById(workProjectId)
                .orElseThrow(() -> new RuntimeException("Work project not found with id: " + workProjectId));

        workProject.setStatus(newStatus);
        return workProjectRepository.save(workProject);
    }

    // Add timeline stage to work project
    public TimelineStage addTimelineStage(Long workProjectId, TimelineStage stage) {
        WorkProject workProject = workProjectRepository.findById(workProjectId)
                .orElseThrow(() -> new RuntimeException("Work project not found with id: " + workProjectId));

        stage.setWorkProject(workProject);
        if (stage.getStatus() == null || stage.getStatus().isEmpty()) {
            stage.setStatus("Pending");
        }
        return timelineStageRepository.save(stage);
    }

    // Get timeline stages for work project
    public List<TimelineStage> getTimelineStagesByWorkProject(Long workProjectId) {
        return timelineStageRepository.findByWorkProjectIdOrderByStartDateAsc(workProjectId);
    }

    // Update timeline stage
    public TimelineStage updateTimelineStage(Long stageId, TimelineStage stageDetails) {
        TimelineStage stage = timelineStageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Timeline stage not found with id: " + stageId));

        stage.setStageName(stageDetails.getStageName());
        stage.setStartDate(stageDetails.getStartDate());
        stage.setEndDate(stageDetails.getEndDate());
        stage.setStatus(stageDetails.getStatus());
        stage.setNotes(stageDetails.getNotes());

        return timelineStageRepository.save(stage);
    }

    // Delete timeline stage
    public void deleteTimelineStage(Long stageId) {
        TimelineStage stage = timelineStageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Timeline stage not found with id: " + stageId));
        timelineStageRepository.delete(stage);
    }

    // Update timeline stage status
    public TimelineStage updateTimelineStageStatus(Long stageId, String newStatus) {
        TimelineStage stage = timelineStageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Timeline stage not found with id: " + stageId));

        stage.setStatus(newStatus);
        return timelineStageRepository.save(stage);
    }

    // Calculate work project progress
    public double calculateProjectProgress(Long workProjectId) {
        List<TimelineStage> stages = getTimelineStagesByWorkProject(workProjectId);

        if (stages.isEmpty()) {
            return 0.0;
        }

        long completedStages = stages.stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .count();

        return ((double) completedStages / stages.size()) * 100;
    }

    // Get work project statistics
    public WorkProjectStatistics getWorkProjectStatistics() {
        List<WorkProject> allProjects = workProjectRepository.findAll();

        long totalProjects = allProjects.size();

        long plannedProjects = allProjects.stream()
                .filter(p -> p.getStatus() == WorkStatus.PLANIFIE)
                .count();

        long inProgressProjects = allProjects.stream()
                .filter(p -> p.getStatus() == WorkStatus.EN_COURS)
                .count();

        long completedProjects = allProjects.stream()
                .filter(p -> p.getStatus() == WorkStatus.TERMINE)
                .count();

        long suspendedProjects = allProjects.stream()
                .filter(p -> p.getStatus() == WorkStatus.SUSPENDU)
                .count();

        double totalBudget = allProjects.stream()
                .mapToDouble(p -> p.getBudget() != null ? p.getBudget() : 0.0)
                .sum();

        // Group by status
        Map<WorkStatus, Long> byStatus = allProjects.stream()
                .collect(Collectors.groupingBy(WorkProject::getStatus, Collectors.counting()));

        // Average project duration (in days)
        double avgDuration = allProjects.stream()
                .filter(p -> p.getStartDate() != null && p.getEndDate() != null)
                .mapToLong(p -> java.time.temporal.ChronoUnit.DAYS.between(p.getStartDate(), p.getEndDate()))
                .average()
                .orElse(0.0);

        return new WorkProjectStatistics(
                totalProjects,
                plannedProjects,
                inProgressProjects,
                completedProjects,
                suspendedProjects,
                totalBudget,
                byStatus,
                avgDuration
        );
    }

    // Inner class for statistics
    public static class WorkProjectStatistics {
        private long totalProjects;
        private long plannedProjects;
        private long inProgressProjects;
        private long completedProjects;
        private long suspendedProjects;
        private double totalBudget;
        private Map<WorkStatus, Long> byStatus;
        private double avgDuration;

        public WorkProjectStatistics(long totalProjects, long plannedProjects,
                                     long inProgressProjects, long completedProjects,
                                     long suspendedProjects, double totalBudget,
                                     Map<WorkStatus, Long> byStatus, double avgDuration) {
            this.totalProjects = totalProjects;
            this.plannedProjects = plannedProjects;
            this.inProgressProjects = inProgressProjects;
            this.completedProjects = completedProjects;
            this.suspendedProjects = suspendedProjects;
            this.totalBudget = totalBudget;
            this.byStatus = byStatus;
            this.avgDuration = avgDuration;
        }

        // Getters
        public long getTotalProjects() { return totalProjects; }
        public long getPlannedProjects() { return plannedProjects; }
        public long getInProgressProjects() { return inProgressProjects; }
        public long getCompletedProjects() { return completedProjects; }
        public long getSuspendedProjects() { return suspendedProjects; }
        public double getTotalBudget() { return totalBudget; }
        public Map<WorkStatus, Long> getByStatus() { return byStatus; }
        public double getAvgDuration() { return avgDuration; }
        public double getCompletionRate() {
            return totalProjects > 0 ? ((double) completedProjects / totalProjects) * 100 : 0;
        }
    }
}
