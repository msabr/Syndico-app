package com.syndico.syndicoapp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentDTO {

    private Long id;
    private Long userId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phoneNumber;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Building is required")
    private Long buildingId;

    @NotBlank(message = "Apartment number is required")
    private String apartmentNumber;

    private LocalDate moveInDate;

    private String buildingName;

    private LocalDateTime createdAt;

    @NotNull(message = "Please specify if owner or tenant")
    private Boolean isOwner;

    private String emergencyContact;

    private String preferredLanguage;

    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
