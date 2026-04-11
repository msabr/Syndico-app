package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.TimelineStage;
import com.syndico.syndicoapp.models.WorkProject;
import com.syndico.syndicoapp.models.enums.WorkStatus;
import com.syndico.syndicoapp.repositories.PrestataireRepository;
import com.syndico.syndicoapp.services.WorkProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/work-projects")
@RequiredArgsConstructor
public class AdminWorkProjectController {

    private final WorkProjectService workProjectService;
    private final PrestataireRepository prestataireRepository;

    @GetMapping
    public String listWorkProjects(
            @RequestParam(required = false) WorkStatus status,
            Model model) {

        List<WorkProject> workProjects;

        if (status != null) {
            workProjects = workProjectService.getWorkProjectsByStatus(status);
        } else {
            workProjects = workProjectService.getAllWorkProjects();
        }

        // Get statistics
        WorkProjectService.WorkProjectStatistics stats = workProjectService.getWorkProjectStatistics();

        model.addAttribute("workProjects", workProjects);
        model.addAttribute("statistics", stats);
        model.addAttribute("workStatuses", WorkStatus.values());
        model.addAttribute("selectedStatus", status);

        return "admin/work-projects/list";
    }

    @GetMapping("/new")
    public String showCreateWorkProjectForm(Model model) {
        model.addAttribute("workProject", new WorkProject());
        model.addAttribute("prestataires", prestataireRepository.findAll());
        model.addAttribute("workStatuses", WorkStatus.values());
        return "admin/work-projects/form";
    }

    @PostMapping("/create")
    public String createWorkProject(@ModelAttribute WorkProject workProject, RedirectAttributes redirectAttributes) {
        try {
            WorkProject created = workProjectService.createWorkProject(workProject);
            redirectAttributes.addFlashAttribute("successMessage", "Work project created successfully!");
            return "redirect:/admin/work-projects/" + created.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/admin/work-projects/new";
        }
    }

    @GetMapping("/{id}")
    public String viewWorkProjectDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            WorkProject workProject = workProjectService.getWorkProjectById(id)
                    .orElseThrow(() -> new RuntimeException("Work project not found"));

            List<TimelineStage> timelineStages = workProjectService.getTimelineStagesByWorkProject(id);
            double progress = workProjectService.calculateProjectProgress(id);

            model.addAttribute("workProject", workProject);
            model.addAttribute("timelineStages", timelineStages);
            model.addAttribute("progress", progress);
            model.addAttribute("workStatuses", WorkStatus.values());
            model.addAttribute("newStage", new TimelineStage());

            return "admin/work-projects/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Work project not found!");
            return "redirect:/admin/work-projects";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditWorkProjectForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            WorkProject workProject = workProjectService.getWorkProjectById(id)
                    .orElseThrow(() -> new RuntimeException("Work project not found"));

            model.addAttribute("workProject", workProject);
            model.addAttribute("prestataires", prestataireRepository.findAll());
            model.addAttribute("workStatuses", WorkStatus.values());

            return "admin/work-projects/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Work project not found!");
            return "redirect:/admin/work-projects";
        }
    }

    @PostMapping("/update/{id}")
    public String updateWorkProject(@PathVariable Long id, @ModelAttribute WorkProject workProject,
                                    RedirectAttributes redirectAttributes) {
        try {
            workProjectService.updateWorkProject(id, workProject);
            redirectAttributes.addFlashAttribute("successMessage", "Work project updated successfully!");
            return "redirect:/admin/work-projects/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/admin/work-projects/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteWorkProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            workProjectService.deleteWorkProject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Work project deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects";
    }

    @PostMapping("/{id}/update-status")
    public String updateWorkProjectStatus(@PathVariable Long id, @RequestParam WorkStatus status,
                                          RedirectAttributes redirectAttributes) {
        try {
            workProjectService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects/" + id;
    }

    @PostMapping("/{projectId}/stages/add")
    public String addTimelineStage(@PathVariable Long projectId, @ModelAttribute TimelineStage stage,
                                   RedirectAttributes redirectAttributes) {
        try {
            workProjectService.addTimelineStage(projectId, stage);
            redirectAttributes.addFlashAttribute("successMessage", "Timeline stage added!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects/" + projectId;
    }

    @PostMapping("/stages/{stageId}/update-status")
    public String updateTimelineStageStatus(@PathVariable Long stageId,
                                            @RequestParam String status,
                                            @RequestParam Long projectId,
                                            RedirectAttributes redirectAttributes) {
        try {
            workProjectService.updateTimelineStageStatus(stageId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Stage status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects/" + projectId;
    }

    @PostMapping("/stages/delete/{stageId}")
    public String deleteTimelineStage(@PathVariable Long stageId, @RequestParam Long projectId,
                                      RedirectAttributes redirectAttributes) {
        try {
            workProjectService.deleteTimelineStage(stageId);
            redirectAttributes.addFlashAttribute("successMessage", "Timeline stage deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/work-projects/" + projectId;
    }
}

