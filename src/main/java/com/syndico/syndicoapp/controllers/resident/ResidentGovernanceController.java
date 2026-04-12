package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.AssemblyMeetingService;
import com.syndico.syndicoapp.services.ResidentService;
import com.syndico.syndicoapp.services.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client/governance")
@RequiredArgsConstructor
public class ResidentGovernanceController {

    private final VoteService voteService;
    private final AssemblyMeetingService assemblyMeetingService;
    private final ResidentService residentService;

    @GetMapping("/active-votes")
    public String activeVotes(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            // Get active votes
            List<Vote> activeVotes = voteService.getActiveVotes();

            // Get votes where resident has already voted
            List<Vote> votedVotes = voteService.getVotesWhereResidentVoted(resident.getId());

            // Get closed votes
            List<Vote> closedVotes = voteService.getVotesByStatus(VoteStatus.FERME);

            model.addAttribute("resident", resident);
            model.addAttribute("activeVotes", activeVotes);
            model.addAttribute("votedVotes", votedVotes);
            model.addAttribute("closedVotes", closedVotes);

            return "client/governance/active-votes";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading votes: " + e.getMessage());
            model.addAttribute("activeVotes", new ArrayList<>());
            model.addAttribute("votedVotes", new ArrayList<>());
            model.addAttribute("closedVotes", new ArrayList<>());
            return "client/governance/active-votes";
        }
    }

    @PostMapping("/votes/{voteId}/submit")
    public String submitVote(
            @PathVariable Long voteId,
            @RequestParam String choice,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            voteService.castVote(voteId, resident.getId(), choice);
            redirectAttributes.addFlashAttribute("successMessage", "Your vote has been recorded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting vote: " + e.getMessage());
        }
        return "redirect:/client/governance/active-votes";
    }

    @GetMapping("/general-assemblies")
    public String generalAssemblies(
            @RequestParam(required = false) MeetingStatus status,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            // Get assemblies
            List<AssemblyMeeting> assemblies;
            if (status != null) {
                assemblies = assemblyMeetingService.getMeetingsByStatus(status);
            } else {
                assemblies = assemblyMeetingService.getAllMeetings();
            }

            // Sort by date (upcoming first)
            assemblies = assemblies.stream()
                .sorted((a1, a2) -> a1.getScheduledDate().compareTo(a2.getScheduledDate()))
                .collect(Collectors.toList());

            // Get statistics
            long upcomingCount = assemblyMeetingService.getMeetingsByStatus(MeetingStatus.PLANIFIEE).size();
            long completedCount = assemblyMeetingService.getMeetingsByStatus(MeetingStatus.TERMINEE).size();

            model.addAttribute("resident", resident);
            model.addAttribute("assemblies", assemblies);
            model.addAttribute("upcomingCount", upcomingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("selectedStatus", status);

            return "client/governance/general-assemblies";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading assemblies: " + e.getMessage());
            model.addAttribute("assemblies", new ArrayList<>());
            model.addAttribute("upcomingCount", 0L);
            model.addAttribute("completedCount", 0L);
            return "client/governance/general-assemblies";
        }
    }

    @GetMapping("/general-assemblies/{id}")
    public String viewAssembly(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            AssemblyMeeting assembly = assemblyMeetingService.getMeetingById(id)
                .orElseThrow(() -> new RuntimeException("Assembly not found"));

            model.addAttribute("resident", resident);
            model.addAttribute("assembly", assembly);

            return "client/governance/assembly-details";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Assembly not found");
            return "redirect:/client/governance/general-assemblies";
        }
    }
}

