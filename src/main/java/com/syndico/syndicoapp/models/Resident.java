package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
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
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "apartment_number", length = 50)
    private String apartmentNumber;

    //@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @Column(name = "move_in_date")
    private LocalDate moveInDate;

    @Column(name = "is_owner")
    private Boolean isOwner = true;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    // Relations
    //@OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    //@Builder.Default
    //private List<Charge> charges = new ArrayList<>();

    //@OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    //@Builder.Default
    //private List<Reclamation> reclamations = new ArrayList<>();

    //@OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    //@Builder.Default
    //private List<Reservation> reservations = new ArrayList<>();

    // Méthodes utilitaires
    public String getFullAddress() {
        if (building != null && apartmentNumber != null) {
            return building.getName() + " - Apt " + apartmentNumber;
        }
        return "Adresse non définie";
    }
}
