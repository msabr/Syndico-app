package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.AssemblyMeetingService;
import com.syndico.syndicoapp.services.ResidentService;
import com.syndico.syndicoapp.services.WorkProjectService;
import com.syndico.syndicoapp.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client/community")
@RequiredArgsConstructor
public class ResidentWorkProjectController {

    private final WorkProjectService workProjectService;
    private final AssemblyMeetingService assemblyMeetingService;
    private final ReservationService reservationService;
    private final ResidentService residentService;

    @GetMapping("/calendar")
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
            model.addAttribute("meetings", new ArrayList<>());
            model.addAttribute("workProjects", new ArrayList<>());
            model.addAttribute("reservations", new ArrayList<>());
            return "client/community/calendar";
        }
    }

    @GetMapping("/ongoing-works")
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
            model.addAttribute("workProjects", new ArrayList<>());
            model.addAttribute("inProgressCount", 0L);
            model.addAttribute("planningCount", 0L);
            model.addAttribute("completedCount", 0L);
            return "client/community/ongoing-works";
        }
    }
}

