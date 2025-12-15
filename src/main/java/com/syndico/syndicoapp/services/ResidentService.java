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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final PasswordEncoder passwordEncoder;

    // Find all with filters
    public Page<Resident> findAll(String search, Long buildingId, String status, String type, Pageable pageable) {
        Specification<Resident> spec = (root, query, cb) -> cb.conjunction();

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("user").get("firstName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("user").get("lastName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("user").get("email")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("apartmentNumber")), "%" + search.toLowerCase() + "%")
            ));
        }

        if (buildingId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("building").get("id"), buildingId));
        }

        if ("active".equals(status)) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("user").get("isEmailVerified")));
        } else if ("inactive".equals(status)) {
            spec = spec.and((root, query, cb) -> cb.isFalse(root.get("user").get("isEmailVerified")));
        }

        if ("owner".equals(type)) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isOwner")));
        } else if ("tenant".equals(type)) {
            spec = spec.and((root, query, cb) -> cb.isFalse(root.get("isOwner")));
        }

        return residentRepository.findAll(spec, pageable);
    }

    public List<Resident> findAll() {
        return residentRepository.findAll();
    }

    public Optional<Resident> findById(Long id) {
        return residentRepository.findById(id);
    }

    public long count() {
        return residentRepository.count();
    }

    public long countByActive(boolean isActive) {
        return residentRepository.countByUserIsEmailVerified(isActive);
    }

    public long countByIsOwner(boolean isOwner) {
        return residentRepository.countByIsOwner(isOwner);
    }

    @Transactional
    public Resident create(ResidentDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        // Create User
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .role(UserRole.RESIDENT)
                .isEmailVerified(true) // Auto-activate for admin creation
                .preferredLanguage(dto.getPreferredLanguage() != null ? dto.getPreferredLanguage() : "EN")
                .build();

        user = userRepository.save(user);

        // Get building
        Building building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found"));

        // Create Resident
        Resident resident = Resident.builder()
                .user(user)
                .building(building)
                .apartmentNumber(dto.getApartmentNumber())
                .moveInDate(dto.getMoveInDate())
                .isOwner(dto.getIsOwner())
                .emergencyContact(dto.getEmergencyContact())
                .build();

        return residentRepository.save(resident);
    }

    @Transactional
    public Resident update(Long id, ResidentDTO dto) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        User user = resident.getUser();

        // Update user info
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPreferredLanguage(dto.getPreferredLanguage());

        // Only update password if provided
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Update email if changed
        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email already exists!");
            }
            user.setEmail(dto.getEmail());
        }

        userRepository.save(user);

        // Update resident info
        Building building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found"));

        resident.setBuilding(building);
        resident.setApartmentNumber(dto.getApartmentNumber());
        resident.setMoveInDate(dto.getMoveInDate());
        resident.setIsOwner(dto.getIsOwner());
        resident.setEmergencyContact(dto.getEmergencyContact());

        return residentRepository.save(resident);
    }

    @Transactional
    public void delete(Long id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        User user = resident.getUser();

        residentRepository.delete(resident);
        userRepository.delete(user);
    }

    @Transactional
    public Resident save(Resident resident) {
        return residentRepository.save(resident);
    }

    @Transactional
    public void deleteById(Long id) {
        residentRepository.deleteById(id);
    }

    // Convert to DTO
    public ResidentDTO toDTO(Resident resident) {
        return ResidentDTO.builder()
                .id(resident.getId())
                .firstName(resident.getUser().getFirstName())
                .lastName(resident.getUser().getLastName())
                .email(resident.getUser().getEmail())
                .phoneNumber(resident.getUser().getPhoneNumber())
                .buildingId(resident.getBuilding().getId())
                .apartmentNumber(resident.getApartmentNumber())
                .moveInDate(resident.getMoveInDate())
                .isOwner(resident.getIsOwner())
                .emergencyContact(resident.getEmergencyContact())
                .preferredLanguage(resident.getUser().getPreferredLanguage())
                .build();
    }

    // Export to Excel
    public void exportToExcel(List<Resident> residents, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Residents");

        // Header Row
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Name", "Email", "Phone", "Building", "Apartment", "Type", "Move In Date", "Status"};

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data Rows
        int rowNum = 1;
        for (Resident resident : residents) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(resident.getId());
            row.createCell(1).setCellValue(resident.getUser().getFullName());
            row.createCell(2).setCellValue(resident.getUser().getEmail());
            row.createCell(3).setCellValue(resident.getUser().getPhoneNumber());
            row.createCell(4).setCellValue(resident.getBuilding().getName());
            row.createCell(5).setCellValue(resident.getApartmentNumber());
            row.createCell(6).setCellValue(resident.getIsOwner() ? "Owner" : "Tenant");
            row.createCell(7).setCellValue(resident.getMoveInDate() != null ? resident.getMoveInDate().toString() : "");
            row.createCell(8).setCellValue(resident.getUser().getIsEmailVerified() ? "Active" : "Inactive");
        }

        // Auto-size columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=residents_" + System.currentTimeMillis() + ".xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // Get All Resident
    public List<Resident> getAllResidents() {
        return residentRepository.findAll();
    }
}
