package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/client/profile")
@RequiredArgsConstructor
public class ResidentProfileController {

    private final ResidentService residentService;

    @GetMapping("/my-information")
    public String myInformation(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            User user = userDetails.getUser();

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "client/profile/myInformation";
            }

            // Create a view object with all necessary information
            Map<String, Object> residentView = new HashMap<>();
            residentView.put("fullName", user.getFirstName() + " " + user.getLastName());
            residentView.put("email", user.getEmail());
            residentView.put("phone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not provided");
            residentView.put("dateOfBirth", null);
            residentView.put("idNumber", "N/A");
            residentView.put("emergencyContactName", "N/A");
            residentView.put("emergencyContactPhone", resident.getEmergencyContact());
            residentView.put("emergencyContactRelationship", "N/A");
            residentView.put("memberSince", resident.getMoveInDate());
            residentView.put("lastPasswordChange", "N/A");

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

    @GetMapping("/edit")
    public String editInformation(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());
            User user = userDetails.getUser();

            if (resident == null) {
                model.addAttribute("errorMessage", "Resident profile not found.");
                return "redirect:/client/profile/my-information";
            }

            // Create a DTO for the form
            Map<String, Object> residentForm = new HashMap<>();
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

    @PostMapping("/update")
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

    @GetMapping("/my-apartment")
    public String myApartment(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            if (resident == null || resident.getBuilding() == null) {
                model.addAttribute("errorMessage", "Apartment information not found.");
                return "client/profile/myApartment";
            }

            Building building = resident.getBuilding();

            // Create apartment view object
            Map<String, Object> apartmentView = new HashMap<>();
            apartmentView.put("building", building.getName());
            apartmentView.put("number", resident.getApartmentNumber());
            apartmentView.put("residenceName", building.getName());
            apartmentView.put("city", building.getAddress() != null ? building.getAddress() : "N/A");
            apartmentView.put("area", "N/A");
            apartmentView.put("bedrooms", "N/A");
            apartmentView.put("bathrooms", "N/A");
            apartmentView.put("floor", "N/A");
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
}

