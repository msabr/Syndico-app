package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "residents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'utilisateur est obligatoire")
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 50, message = "Le numéro d'appartement ne peut pas dépasser 50 caractères")
    @Column(name = "apartment_number", length = 50)
    private String apartmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @PastOrPresent(message = "La date d'emménagement ne peut pas être dans le futur")
    @Column(name = "move_in_date")
    private LocalDate moveInDate;

    @Column(name = "is_owner")
    @Builder.Default
    private Boolean isOwner = true;

    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Le contact d'urgence doit être un numéro de téléphone valide")
    @Column(name = "emergency_contact")
    private String emergencyContact;

    // Relations
    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Charge> charges = new ArrayList<>();

    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reclamation> reclamations = new ArrayList<>();

    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    // Méthodes utilitaires
    public String getFullAddress() {
        if (building != null && apartmentNumber != null) {
            return building.getName() + " - Apt " + apartmentNumber;
        }
        return "Adresse non définie";
    }
}
