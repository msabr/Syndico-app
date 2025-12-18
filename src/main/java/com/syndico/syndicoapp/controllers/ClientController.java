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
    private final DocumentService documentService;
    private final MessageService messageService;
    private final NotificationService notificationService;

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

    // ========== FACILITIES - MY RESERVATIONS ==========
    @GetMapping("/my-reservations")
    public String myReservations(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            // Get all reservations for the resident
            List<Reservation> allReservations = reservationService.getReservationsByResident(resident.getId());

            // Filter by status
            List<Reservation> upcomingReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE
                          && r.getStartDateTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

            List<Reservation> confirmedReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .collect(Collectors.toList());

            List<Reservation> pendingReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.EN_ATTENTE)
                .collect(Collectors.toList());

            List<Reservation> cancelledReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.ANNULEE)
                .collect(Collectors.toList());

            model.addAttribute("resident", resident);
            model.addAttribute("reservations", allReservations);
            model.addAttribute("upcomingCount", upcomingReservations.size());
            model.addAttribute("confirmedCount", confirmedReservations.size());
            model.addAttribute("pendingCount", pendingReservations.size());
            model.addAttribute("cancelledCount", cancelledReservations.size());

            return "client/facilities/my-reservations";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading reservations: " + e.getMessage());
            model.addAttribute("reservations", new java.util.ArrayList<>());
            return "client/facilities/my-reservations";
        }
    }

    // ========== FACILITIES - BOOK SPACE ==========
    @GetMapping("/book-space")
    public String bookSpace(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            // Get all space types
            SpaceType[] spaceTypes = SpaceType.values();

            model.addAttribute("resident", resident);
            model.addAttribute("spaceTypes", spaceTypes);
            model.addAttribute("reservation", new Reservation());

            return "client/facilities/book-space";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading booking page: " + e.getMessage());
            return "client/facilities/book-space";
        }
    }

    // Submit new reservation
    @PostMapping("/book-space")
    public String submitReservation(
            @RequestParam SpaceType spaceType,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            Reservation reservation = Reservation.builder()
                .resident(resident)
                .spaceType(spaceType)
                .startDateTime(LocalDateTime.parse(startDateTime))
                .endDateTime(LocalDateTime.parse(endDateTime))
                .notes(notes)
                .status(ReservationStatus.EN_ATTENTE)
                .build();

            reservationService.createReservation(reservation);

            redirectAttributes.addFlashAttribute("successMessage", "Reservation submitted successfully!");
            return "redirect:/client/my-reservations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating reservation: " + e.getMessage());
            return "redirect:/client/book-space";
        }
    }

    // Cancel reservation
    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling reservation: " + e.getMessage());
        }
        return "redirect:/client/my-reservations";
    }

    // ========== INFORMATION - DOCUMENTS ==========
    @GetMapping("/documents")
    public String documents(
            @RequestParam(required = false) DocumentCategory category,
            @RequestParam(required = false) String search,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            List<Document> documents;

            if (search != null && !search.isEmpty()) {
                // Search documents
                documents = documentService.searchDocuments(search);
            } else if (category != null) {
                // Filter by category
                documents = documentService.getDocumentsByCategory(category);
            } else {
                // Get all public documents
                documents = documentService.getPublicDocuments();
            }

            // Filter only public documents for residents
            documents = documents.stream()
                .filter(Document::getIsPublic)
                .collect(Collectors.toList());

            // Get statistics
            long totalDocuments = documents.size();
            long recentDocuments = documents.stream()
                .filter(d -> d.getUploadedAt().isAfter(LocalDateTime.now().minusDays(7)))
                .count();

            model.addAttribute("resident", resident);
            model.addAttribute("documents", documents);
            model.addAttribute("categories", DocumentCategory.values());
            model.addAttribute("selectedCategory", category);
            model.addAttribute("searchQuery", search);
            model.addAttribute("totalDocuments", totalDocuments);
            model.addAttribute("recentDocuments", recentDocuments);

            return "client/information/documents";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading documents: " + e.getMessage());
            model.addAttribute("documents", new java.util.ArrayList<>());
            return "client/information/documents";
        }
    }

    // ========== INFORMATION - MESSAGES ==========
    @GetMapping("/messages")
    public String messages(
            @RequestParam(required = false) String search,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            User user = userDetails.getUser();

            List<Message> receivedMessages;
            if (search != null && !search.isEmpty()) {
                receivedMessages = messageService.searchMessages(user.getId(), search);
            } else {
                receivedMessages = messageService.getReceivedMessages(user.getId());
            }

            List<Message> sentMessages = messageService.getSentMessages(user.getId());
            long unreadCount = messageService.getUnreadCount(user.getId());

            model.addAttribute("resident", resident);
            model.addAttribute("receivedMessages", receivedMessages);
            model.addAttribute("sentMessages", sentMessages);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("searchQuery", search);

            return "client/information/messages";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading messages: " + e.getMessage());
            model.addAttribute("receivedMessages", new java.util.ArrayList<>());
            model.addAttribute("sentMessages", new java.util.ArrayList<>());
            return "client/information/messages";
        }
    }

    // Send new message
    @PostMapping("/messages/send")
    public String sendMessage(
            @RequestParam Long receiverId,
            @RequestParam String subject,
            @RequestParam String content,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User sender = userDetails.getUser();
            messageService.sendMessage(sender.getId(), receiverId, subject, content);
            redirectAttributes.addFlashAttribute("successMessage", "Message sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error sending message: " + e.getMessage());
        }
        return "redirect:/client/messages";
    }

    // Mark message as read
    @PostMapping("/messages/{id}/read")
    public String markMessageAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            messageService.markAsRead(id);
            redirectAttributes.addFlashAttribute("successMessage", "Message marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error marking message: " + e.getMessage());
        }
        return "redirect:/client/messages";
    }

    // ========== INFORMATION - NOTIFICATIONS ==========
    @GetMapping("/notifications")
    public String notifications(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Boolean unreadOnly,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            Resident resident = residentService.getResidentByUserId(user.getId());

            List<Notification> notifications;

            if (Boolean.TRUE.equals(unreadOnly)) {
                notifications = notificationService.getUnreadNotifications(user.getId());
            } else {
                notifications = notificationService.getNotificationsByUser(user.getId());
            }

            // Filter by type if specified
            if (type != null) {
                notifications = notifications.stream()
                    .filter(n -> n.getType() == type)
                    .collect(Collectors.toList());
            }

            // Get statistics
            long totalNotifications = notifications.size();
            long unreadCount = notificationService.getUnreadCount(user.getId());
            long todayCount = notifications.stream()
                .filter(n -> n.getSentAt().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();

            model.addAttribute("resident", resident);
            model.addAttribute("notifications", notifications);
            model.addAttribute("types", NotificationType.values());
            model.addAttribute("selectedType", type);
            model.addAttribute("unreadOnly", unreadOnly);
            model.addAttribute("totalNotifications", totalNotifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("todayCount", todayCount);

            return "client/information/notifications";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading notifications: " + e.getMessage());
            model.addAttribute("notifications", new java.util.ArrayList<>());
            return "client/information/notifications";
        }
    }

    // Mark notification as read
    @PostMapping("/notifications/{id}/read")
    public String markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notification marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error marking notification: " + e.getMessage());
        }
        return "redirect:/client/notifications";
    }

    // Mark all notifications as read
    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userDetails.getUser();
            notificationService.markAllAsRead(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error marking notifications: " + e.getMessage());
        }
        return "redirect:/client/notifications";
    }

    // ========== PROFILE PAGES ==========

    // My Information Page
    @GetMapping("/profile/my-information")
    public String myInformation(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            User user = userDetails.getUser();

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "client/profile/myInformation";
            }

            // Create a view object with all necessary information
            java.util.Map<String, Object> residentView = new java.util.HashMap<>();
            residentView.put("fullName", user.getFirstName() + " " + user.getLastName());
            residentView.put("email", user.getEmail());
            residentView.put("phone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not provided");
            residentView.put("dateOfBirth", null); // Can be added to User model if needed
            residentView.put("idNumber", "N/A"); // Can be added to Resident model if needed
            residentView.put("emergencyContactName", "N/A"); // Can be added to Resident model
            residentView.put("emergencyContactPhone", resident.getEmergencyContact());
            residentView.put("emergencyContactRelationship", "N/A"); // Can be added to Resident model
            residentView.put("memberSince", resident.getMoveInDate());
            residentView.put("lastPasswordChange", "N/A"); // Can be tracked in User model

            model.addAttribute("residentView", residentView);
            model.addAttribute("resident", resident);
            model.addAttribute("user", user);

            return "client/profile/myInformation";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading profile information: " + e.getMessage());
            return "client/profile/myInformation";
        }
    }

    // Edit Information Page
    @GetMapping("/profile/edit")
    public String editInformation(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            User user = userDetails.getUser();

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "redirect:/client/profile/my-information";
            }

            // Create a DTO for the form
            java.util.Map<String, Object> residentForm = new java.util.HashMap<>();
            residentForm.put("firstName", user.getFirstName());
            residentForm.put("lastName", user.getLastName());
            residentForm.put("email", user.getEmail());
            residentForm.put("phoneNumber", user.getPhoneNumber());
            residentForm.put("emergencyContact", resident.getEmergencyContact());
            residentForm.put("apartmentNumber", resident.getApartmentNumber());

            model.addAttribute("residentForm", residentForm);
            model.addAttribute("resident", resident);
            model.addAttribute("user", user);

            return "client/profile/edit-information";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading profile: " + e.getMessage());
            return "redirect:/client/profile/my-information";
        }
    }

    // Update Profile Information
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String emergencyContact,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            User user = userDetails.getUser();

            if (resident == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Resident profile not found.");
                return "redirect:/client/profile/my-information";
            }

            // Create ResidentDTO with updated values
            com.syndico.syndicoapp.dto.ResidentDTO residentDTO = com.syndico.syndicoapp.dto.ResidentDTO.builder()
                .id(resident.getId())
                .userId(user.getId())
                .firstName(firstName != null && !firstName.trim().isEmpty() ? firstName.trim() : user.getFirstName())
                .lastName(lastName != null && !lastName.trim().isEmpty() ? lastName.trim() : user.getLastName())
                .email(user.getEmail())
                .phoneNumber(phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber.trim() : user.getPhoneNumber())
                .emergencyContact(emergencyContact != null && !emergencyContact.trim().isEmpty() ? emergencyContact.trim() : resident.getEmergencyContact())
                .buildingId(resident.getBuilding() != null ? resident.getBuilding().getId() : null)
                .apartmentNumber(resident.getApartmentNumber())
                .moveInDate(resident.getMoveInDate())
                .isOwner(resident.getIsOwner())
                .preferredLanguage(user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "FR")
                .build();

            // Save updates using the service
            residentService.updateResident(resident.getId(), residentDTO);

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/client/profile/my-information";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
            return "redirect:/client/profile/edit";
        }
    }

    // My Apartment Page
    @GetMapping("/profile/my-apartment")
    public String myApartment(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null || resident.getBuilding() == null) {
                model.addAttribute("errorMessage", "Apartment information not found.");
                return "client/profile/myApartment";
            }

            Building building = resident.getBuilding();

            // Create apartment view object
            java.util.Map<String, Object> apartmentView = new java.util.HashMap<>();
            apartmentView.put("building", building.getName());
            apartmentView.put("number", resident.getApartmentNumber());
            apartmentView.put("residenceName", building.getName());
            apartmentView.put("city", building.getAddress() != null ? building.getAddress() : "N/A");
            apartmentView.put("area", "N/A"); // Can be added to Apartment/Resident model
            apartmentView.put("bedrooms", "N/A"); // Can be added to model
            apartmentView.put("bathrooms", "N/A"); // Can be added to model
            apartmentView.put("floor", "N/A"); // Can be added to model
            apartmentView.put("type", resident.getIsOwner() ? "Owner" : "Tenant");
            apartmentView.put("moveInDate", resident.getMoveInDate());
            apartmentView.put("buildingFloors", building.getNumberOfFloors());
            apartmentView.put("buildingApartments", building.getNumberOfApartments());
            apartmentView.put("yearBuilt", building.getYearBuilt());

            model.addAttribute("apartment", apartmentView);
            model.addAttribute("resident", resident);
            model.addAttribute("building", building);

            return "client/profile/myApartment";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading apartment information: " + e.getMessage());
            return "client/profile/myApartment";
        }
    }

    // ========== FINANCE PAGES ==========

    // My Charges Page
    @GetMapping("/my-charges")
    public String myCharges(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "client/finance/myCharges";
            }

            // Get all charges for the resident
            List<Charge> allCharges = chargeService.getChargesByResident(resident.getId());

            // Calculate statistics
            double pendingAmount = allCharges.stream()
                .filter(c -> c.getStatus() == ChargeStatus.EN_ATTENTE)
                .mapToDouble(c -> c.getAmount().doubleValue())
                .sum();

            double paidThisYear = allCharges.stream()
                .filter(c -> c.getStatus() == ChargeStatus.PAYEE &&
                            c.getDueDate().getYear() == java.time.LocalDate.now().getYear())
                .mapToDouble(c -> c.getAmount().doubleValue())
                .sum();

            double overdueAmount = allCharges.stream()
                .filter(c -> c.getStatus() == ChargeStatus.EN_RETARD)
                .mapToDouble(c -> c.getAmount().doubleValue())
                .sum();

            double totalThisYear = allCharges.stream()
                .filter(c -> c.getDueDate().getYear() == java.time.LocalDate.now().getYear())
                .mapToDouble(c -> c.getAmount().doubleValue())
                .sum();

            // Create summary
            java.util.Map<String, Object> summary = new java.util.HashMap<>();
            summary.put("pending", pendingAmount);
            summary.put("paidThisYear", paidThisYear);
            summary.put("overdue", overdueAmount);
            summary.put("totalThisYear", totalThisYear);

            model.addAttribute("charges", allCharges);
            model.addAttribute("summary", summary);
            model.addAttribute("resident", resident);

            return "client/finance/myCharges";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading charges: " + e.getMessage());
            model.addAttribute("charges", new java.util.ArrayList<>());
            return "client/finance/myCharges";
        }
    }

    // Payment History Page
    @GetMapping("/payment-history")
    public String paymentHistory(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "client/finance/paymentHistory";
            }

            // Get all payments for the resident (filter by resident's charges)
            List<Payment> payments = paymentService.getAllPayments().stream()
                .filter(p -> p.getCharge() != null &&
                            p.getCharge().getResident() != null &&
                            p.getCharge().getResident().getId().equals(resident.getId()))
                .collect(java.util.stream.Collectors.toList());

            // Calculate statistics
            double totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum();

            long paymentsThisYear = payments.stream()
                .filter(p -> p.getPaymentDate() != null &&
                            p.getPaymentDate().getYear() == java.time.LocalDateTime.now().getYear())
                .count();

            double averagePayment = payments.isEmpty() ? 0 : totalPaid / payments.size();

            Payment lastPayment = payments.stream()
                .filter(p -> p.getPaymentDate() != null)
                .max(java.util.Comparator.comparing(Payment::getPaymentDate))
                .orElse(null);

            // Create statistics map
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("totalPaid", totalPaid);
            statistics.put("paymentsThisYear", paymentsThisYear);
            statistics.put("averagePayment", averagePayment);
            statistics.put("lastPaymentDate", lastPayment != null ? lastPayment.getPaymentDate() : null);

            model.addAttribute("payments", payments);
            model.addAttribute("statistics", statistics);
            model.addAttribute("resident", resident);

            return "client/finance/paymentHistory";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading payment history: " + e.getMessage());
            model.addAttribute("payments", new java.util.ArrayList<>());
            return "client/finance/paymentHistory";
        }
    }

    // ========== SERVICES PAGES ==========

    // My Complaints Page
    @GetMapping("/my-complaints")
    public String myComplaints(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "client/services/my-complaints";
            }

            // Get all complaints for the resident
            List<Reclamation> complaints = reclamationService.getReclamationsByResident(resident.getId());

            // Calculate statistics
            long totalComplaints = complaints.size();
            long pendingComplaints = complaints.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.NOUVELLE)
                .count();
            long inProgressComplaints = complaints.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.EN_COURS)
                .count();
            long resolvedComplaints = complaints.stream()
                .filter(r -> r.getStatus() == ReclamationStatus.RESOLUE)
                .count();

            // Create statistics map
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("total", totalComplaints);
            statistics.put("pending", pendingComplaints);
            statistics.put("inProgress", inProgressComplaints);
            statistics.put("resolved", resolvedComplaints);

            model.addAttribute("complaints", complaints);
            model.addAttribute("statistics", statistics);
            model.addAttribute("resident", resident);

            return "client/services/my-complaints";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading complaints: " + e.getMessage());
            model.addAttribute("complaints", new java.util.ArrayList<>());
            return "client/services/my-complaints";
        }
    }

    // New Complaint Page
    @GetMapping("/new-complaint")
    public String newComplaintPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "redirect:/client/my-complaints";
            }

            model.addAttribute("resident", resident);
            model.addAttribute("categories", ReclamationCategory.values());

            return "client/services/new-complaint";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/client/my-complaints";
        }
    }

    // Submit New Complaint
    @PostMapping("/new-complaint")
    public String submitComplaint(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam(required = false) String priority,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Resident profile not found.");
                return "redirect:/client/my-complaints";
            }

            // Create new complaint
            Reclamation complaint = new Reclamation();
            complaint.setResident(resident);
            complaint.setTitle(title);
            complaint.setDescription(description);

            // Set category
            try {
                complaint.setCategory(ReclamationCategory.valueOf(category));
            } catch (IllegalArgumentException e) {
                complaint.setCategory(ReclamationCategory.AUTRE);
            }

            // Set priority
            if (priority != null && !priority.isEmpty()) {
                try {
                    complaint.setPriority(Priority.valueOf(priority));
                } catch (IllegalArgumentException e) {
                    complaint.setPriority(Priority.MOYENNE);
                }
            } else {
                complaint.setPriority(Priority.MOYENNE);
            }

            complaint.setStatus(ReclamationStatus.NOUVELLE);

            // Save complaint
            reclamationService.createReclamation(complaint);

            redirectAttributes.addFlashAttribute("successMessage", "Complaint submitted successfully!");
            return "redirect:/client/my-complaints";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting complaint: " + e.getMessage());
            return "redirect:/client/new-complaint";
        }
    }

    //  ========== SUPPORT PAGES ==========

    // Help & FAQ Page
    @GetMapping("/help")
    public String helpPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            model.addAttribute("resident", resident);
            return "client/support/help-faq";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading help page: " + e.getMessage());
            return "client/support/help-faq";
        }
    }
}
