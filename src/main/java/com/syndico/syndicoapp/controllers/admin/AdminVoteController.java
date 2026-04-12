package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.Vote;
import com.syndico.syndicoapp.models.enums.VoteStatus;
import com.syndico.syndicoapp.services.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/votes")
@RequiredArgsConstructor
public class AdminVoteController {

    private final VoteService voteService;

    @GetMapping
    public String listVotes(@RequestParam(required = false) VoteStatus status, Model model) {
        List<Vote> votes;

        if (status != null) {
            votes = voteService.getVotesByStatus(status);
        } else {
            votes = voteService.getAllVotes();
        }

        VoteService.VoteStatistics stats = voteService.getVoteStatistics();

        model.addAttribute("votes", votes);
        model.addAttribute("statistics", stats);
        model.addAttribute("voteStatuses", VoteStatus.values());
        model.addAttribute("selectedStatus", status);

        return "admin/votes/list";
    }

    @PostMapping("/create")
    public String createVote(@ModelAttribute Vote vote, RedirectAttributes redirectAttributes) {
        try {
            voteService.createVote(vote);
            redirectAttributes.addFlashAttribute("successMessage", "Vote created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/votes";
    }

    @GetMapping("/{id}/results")
    public String viewVoteResults(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            VoteService.VoteResults results = voteService.getVoteResults(id);
            model.addAttribute("results", results);
            return "admin/votes/results";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vote not found!");
            return "redirect:/admin/votes";
        }
    }

    @PostMapping("/{id}/close")
    public String closeVote(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voteService.closeVote(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vote closed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/votes";
    }

    @PostMapping("/delete/{id}")
    public String deleteVote(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voteService.deleteVote(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vote deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/votes";
    }
}

