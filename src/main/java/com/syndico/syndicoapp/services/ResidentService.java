package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.dto.ResidentDTO;
import com.syndico.syndicoapp.models.Building;
import com.syndico.syndicoapp.models.Resident;
import com.syndico.syndicoapp.models.User;
import com.syndico.syndicoapp.models.enums.UserRole;
import com.syndico.syndicoapp.repositories.BuildingRepository;
import com.syndico.syndicoapp.repositories.ResidentRepository;
import com.syndico.syndicoapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidentService {

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get all residents
    public List<Resident> getAllResidents() {
        return residentRepository.findAll();
    }

    // Get resident by ID
    public Resident getResidentById(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resident not found with id: " + id));
    }

    // Get resident by user ID
    public Resident getResidentByUserId(Long userId) {
        return residentRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Resident not found for user id: " + userId));
    }

    // Get residents by building
    public List<Resident> getResidentsByBuilding(Long buildingId) {
        return residentRepository.findByBuilding_Id(buildingId);
    }

    // Search residents
    public List<Resident> searchResidents(String keyword) {
        List<User> users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword, keyword, keyword
        );

        return users.stream()
                .filter(user -> user.getRole() == UserRole.RESIDENT)
                .map(user -> residentRepository.findByUser_Id(user.getId()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    // Create new resident with user account
    @Transactional
    public Resident createResident(ResidentDTO residentDTO) {
        // Check if email already exists
        if (userRepository.findByEmail(residentDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + residentDTO.getEmail());
        }

        // Verify building exists
        Building building = buildingRepository.findById(residentDTO.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found"));

        // Create User entity
        User user = new User();
        user.setEmail(residentDTO.getEmail());
        user.setPassword(passwordEncoder.encode(residentDTO.getPassword()));
        user.setFirstName(residentDTO.getFirstName());
        user.setLastName(residentDTO.getLastName());
        user.setPhoneNumber(residentDTO.getPhoneNumber());
        user.setRole(UserRole.RESIDENT);
        user.setIsEmailVerified(true); // Auto-verify for admin-created accounts
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setPreferredLanguage("FR");

        User savedUser = userRepository.save(user);

        // Create Resident entity
        Resident resident = new Resident();
        resident.setUserId(savedUser.getId());
        resident.setApartmentNumber(residentDTO.getApartmentNumber());
        resident.setBuildingId(residentDTO.getBuildingId());
        resident.setMoveInDate(residentDTO.getMoveInDate() != null ?
                residentDTO.getMoveInDate() : LocalDate.now());
        resident.setIsOwner(residentDTO.getIsOwner() != null ?
                residentDTO.getIsOwner() : false);
        resident.setEmergencyContact(residentDTO.getEmergencyContact());

        return residentRepository.save(resident);
    }

    // Update resident
    @Transactional
    public Resident updateResident(Long id, ResidentDTO residentDTO) {
        Resident resident = getResidentById(id);
        User user = userRepository.findById(resident.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(residentDTO.getEmail())) {
            if (userRepository.findByEmail(residentDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists: " + residentDTO.getEmail());
            }
            user.setEmail(residentDTO.getEmail());
        }

        // Update User entity
        user.setFirstName(residentDTO.getFirstName());
        user.setLastName(residentDTO.getLastName());
        user.setPhoneNumber(residentDTO.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());

        // Update password only if provided
        if (residentDTO.getPassword() != null && !residentDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(residentDTO.getPassword()));
        }

        userRepository.save(user);

        // Update Resident entity
        if (residentDTO.getBuildingId() != null) {
            buildingRepository.findById(residentDTO.getBuildingId())
                    .orElseThrow(() -> new RuntimeException("Building not found"));
            resident.setBuildingId(residentDTO.getBuildingId());
        }

        resident.setApartmentNumber(residentDTO.getApartmentNumber());
        resident.setMoveInDate(residentDTO.getMoveInDate());
        resident.setIsOwner(residentDTO.getIsOwner());
        resident.setEmergencyContact(residentDTO.getEmergencyContact());

        return residentRepository.save(resident);
    }

    // Delete resident
    @Transactional
    public void deleteResident(Long id) {
        Resident resident = getResidentById(id);
        Long userId = resident.getUserId();

        // Delete resident first
        residentRepository.deleteById(id);

        // Then delete user account
        userRepository.deleteById(userId);
    }

    // Get resident with user details (for display)
    public ResidentDTO getResidentDTOById(Long id) {
        Resident resident = getResidentById(id);
        User user = userRepository.findById(resident.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDTO(resident, user);
    }

    // Get all residents with user details
    public List<ResidentDTO> getAllResidentsDTO() {
        List<Resident> residents = getAllResidents();
        return residents.stream()
                .map(resident -> {
                    User user = userRepository.findById(resident.getUserId()).orElse(null);
                    return user != null ? mapToDTO(resident, user) : null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    // Map Resident and User to ResidentDTO
    private ResidentDTO mapToDTO(Resident resident, User user) {
        ResidentDTO dto = new ResidentDTO();
        dto.setId(resident.getId());
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setApartmentNumber(resident.getApartmentNumber());
        dto.setBuildingId(resident.getBuildingId());
        dto.setMoveInDate(resident.getMoveInDate());
        dto.setIsOwner(resident.getIsOwner());
        dto.setEmergencyContact(resident.getEmergencyContact());
        dto.setCreatedAt(user.getCreatedAt());

        // Get building name
        if (resident.getBuildingId() != null) {
            buildingRepository.findById(resident.getBuildingId())
                    .ifPresent(building -> dto.setBuildingName(building.getName()));
        }

        return dto;
    }

    // Count total residents
    public long countResidents() {
        return residentRepository.count();
    }

    // Get owners only
    public List<Resident> getOwners() {
        return residentRepository.findByIsOwnerTrue();
    }

    // Get tenants only
    public List<Resident> getTenants() {
        return residentRepository.findByIsOwnerFalse();
    }

    public Object findAll() {
        return residentRepository.findAll();
    }
}
