package com.syndico.syndicoapp.controllers;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ResidentService residentService;
    private final ChargeService chargeService;
    private final ReclamationService reclamationService;
    private final ReservationService reservationService;
    private final WorkProjectService workProjectService;
    private final AssemblyMeetingService assemblyMeetingService;
    private final VoteService voteService;
    private final PaymentService paymentService;

    // ========== DASHBOARD ==========
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // Get resident information
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found. Please contact administration.");
                return "client/dashboard";
            }

            // Get statistics
            List<Charge> pendingCharges = chargeService.getChargesByResidentAndStatus(
                resident.getId(), ChargeStatus.EN_ATTENTE
            );

            List<Payment> paymentsThisYear = paymentService.getPaymentsByResidentAndYear(
                resident.getId(), LocalDateTime.now().getYear()
            );

            List<Reclamation> activeComplaints = reclamationService.findByResidentIdAndStatus(
                resident.getId(), ReclamationStatus.EN_COURS
            );

            List<Reservation> upcomingReservations = reservationService.getUpcomingReservationsByResident(
                resident.getId()
            );

            // Add to model
            model.addAttribute("resident", resident);
            model.addAttribute("pendingChargesCount", pendingCharges.size());
            model.addAttribute("paymentsThisYearCount", paymentsThisYear.size());
            model.addAttribute("activeComplaintsCount", activeComplaints.size());
            model.addAttribute("upcomingReservationsCount", upcomingReservations.size());

            // Get recent payments (last 3)
            List<Payment> recentPayments = paymentsThisYear.stream()
                .sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()))
                .limit(3)
                .collect(Collectors.toList());
            model.addAttribute("recentPayments", recentPayments);

            // Get upcoming events
            List<AssemblyMeeting> upcomingMeetings = assemblyMeetingService.getUpcomingMeetings();
            model.addAttribute("upcomingMeetings", upcomingMeetings);

            return "client/dashboard";
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            model.addAttribute("errorMessage", "Error loading dashboard: " + e.getMessage());
            // Set default empty values to prevent errors
            model.addAttribute("pendingChargesCount", 0);
            model.addAttribute("paymentsThisYearCount", 0);
            model.addAttribute("activeComplaintsCount", 0);
            model.addAttribute("upcomingReservationsCount", 0);
            model.addAttribute("recentPayments", new java.util.ArrayList<>());
            model.addAttribute("upcomingMeetings", new java.util.ArrayList<>());
            return "client/dashboard";
        }
    }

    // ========== COMMUNITY - CALENDAR ==========
    @GetMapping("/community/calendar")
    public String calendar(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            // Get all events for calendar
            List<AssemblyMeeting> meetings = assemblyMeetingService.getAllMeetings();
            List<WorkProject> workProjects = workProjectService.getAllWorkProjects();
            List<Reservation> reservations = reservationService.getReservationsByResident(resident.getId());

            model.addAttribute("resident", resident);
            model.addAttribute("meetings", meetings);
            model.addAttribute("workProjects", workProjects);
            model.addAttribute("reservations", reservations);

            return "client/community/calendar";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading calendar: " + e.getMessage());
            model.addAttribute("meetings", new java.util.ArrayList<>());
            model.addAttribute("workProjects", new java.util.ArrayList<>());
            model.addAttribute("reservations", new java.util.ArrayList<>());
            return "client/community/calendar";
        }
    }

    // ========== COMMUNITY - ONGOING WORKS ==========
    @GetMapping("/community/ongoing-works")
    public String ongoingWorks(
            @RequestParam(required = false) WorkStatus status,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            // Get work projects
            List<WorkProject> workProjects;
            if (status != null) {
                workProjects = workProjectService.getWorkProjectsByStatus(status);
            } else {
                workProjects = workProjectService.getAllWorkProjects();
            }

            // Get statistics
            long inProgressCount = workProjectService.getWorkProjectsByStatus(WorkStatus.EN_COURS).size();
            long planningCount = workProjectService.getWorkProjectsByStatus(WorkStatus.PLANIFIE).size();
            long completedCount = workProjectService.getWorkProjectsByStatus(WorkStatus.TERMINE).size();

            model.addAttribute("resident", resident);
            model.addAttribute("workProjects", workProjects);
            model.addAttribute("inProgressCount", inProgressCount);
            model.addAttribute("planningCount", planningCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("selectedStatus", status);

            return "client/community/ongoing-works";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading work projects: " + e.getMessage());
            model.addAttribute("workProjects", new java.util.ArrayList<>());
            model.addAttribute("inProgressCount", 0L);
            model.addAttribute("planningCount", 0L);
            model.addAttribute("completedCount", 0L);
            return "client/community/ongoing-works";
        }
    }

    // ========== GOVERNANCE - ACTIVE VOTES ==========
    @GetMapping("/governance/active-votes")
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
            model.addAttribute("activeVotes", new java.util.ArrayList<>());
            model.addAttribute("votedVotes", new java.util.ArrayList<>());
            model.addAttribute("closedVotes", new java.util.ArrayList<>());
            return "client/governance/active-votes";
        }
    }

    // Submit vote
    @PostMapping("/governance/votes/{voteId}/submit")
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

    // ========== GOVERNANCE - GENERAL ASSEMBLIES ==========
    @GetMapping("/governance/general-assemblies")
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
            model.addAttribute("assemblies", new java.util.ArrayList<>());
            model.addAttribute("upcomingCount", 0L);
            model.addAttribute("completedCount", 0L);
            return "client/governance/general-assemblies";
        }
    }

    // View assembly details
    @GetMapping("/governance/general-assemblies/{id}")
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
