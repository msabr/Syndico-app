package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.AssemblyMeeting;
import com.syndico.syndicoapp.models.Vote;
import com.syndico.syndicoapp.models.enums.MeetingStatus;
import com.syndico.syndicoapp.services.AssemblyMeetingService;
import com.syndico.syndicoapp.services.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/general-assemblies")
@RequiredArgsConstructor
public class AdminAssemblyController {

    private final AssemblyMeetingService assemblyMeetingService;
    private final VoteService voteService;

    @GetMapping
    public String listAssemblies(@RequestParam(required = false) MeetingStatus status, Model model) {
        List<AssemblyMeeting> meetings;

        if (status != null) {
            meetings = assemblyMeetingService.getMeetingsByStatus(status);
        } else {
            meetings = assemblyMeetingService.getAllMeetings();
        }

        AssemblyMeetingService.MeetingStatistics stats = assemblyMeetingService.getMeetingStatistics();

        model.addAttribute("meetings", meetings);
        model.addAttribute("statistics", stats);
        model.addAttribute("meetingStatuses", MeetingStatus.values());
        model.addAttribute("selectedStatus", status);

        return "admin/general-assemblies/list";
    }

    @PostMapping("/create")
    public String createAssembly(@ModelAttribute AssemblyMeeting meeting, RedirectAttributes redirectAttributes) {
        try {
            assemblyMeetingService.createAssemblyMeeting(meeting);
            redirectAttributes.addFlashAttribute("successMessage", "Assembly created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/general-assemblies";
    }

    @GetMapping("/{id}")
    public String viewAssemblyDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            AssemblyMeeting meeting = assemblyMeetingService.getMeetingById(id)
                    .orElseThrow(() -> new RuntimeException("Meeting not found"));

            List<Vote> votes = voteService.getAllVotes().stream()
                    .filter(v -> v.getAssemblyMeeting() != null && v.getAssemblyMeeting().getId().equals(id))
                    .collect(Collectors.toList());

            model.addAttribute("meeting", meeting);
            model.addAttribute("votes", votes);
            model.addAttribute("meetingStatuses", MeetingStatus.values());

            return "admin/general-assemblies/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Meeting not found!");
            return "redirect:/admin/general-assemblies";
        }
    }

    @PostMapping("/update/{id}")
    public String updateAssembly(@PathVariable Long id, @ModelAttribute AssemblyMeeting meeting,
                                 RedirectAttributes redirectAttributes) {
        try {
            assemblyMeetingService.updateMeeting(id, meeting);
            redirectAttributes.addFlashAttribute("successMessage", "Assembly updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/general-assemblies/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteAssembly(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            assemblyMeetingService.deleteMeeting(id);
            redirectAttributes.addFlashAttribute("successMessage", "Assembly deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/general-assemblies";
    }

    @PostMapping("/{id}/update-status")
    public String updateAssemblyStatus(@PathVariable Long id, @RequestParam MeetingStatus status,
                                       RedirectAttributes redirectAttributes) {
        try {
            assemblyMeetingService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/general-assemblies/" + id;
    }
}

