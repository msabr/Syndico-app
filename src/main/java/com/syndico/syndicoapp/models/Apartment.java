package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "apartments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"building_id", "apartment_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro d'appartement est obligatoire")
    @Size(max = 50, message = "Le numéro d'appartement ne peut pas dépasser 50 caractères")
    @Column(name = "apartment_number", nullable = false, length = 50)
    private String apartmentNumber;

    @Min(value = -5, message = "L'étage ne peut pas être inférieur à -5")
    @Max(value = 200, message = "L'étage ne peut pas dépasser 200")
    private Integer floor;

    @DecimalMin(value = "1.0", message = "La surface doit être au minimum 1 m²")
    @DecimalMax(value = "10000.0", message = "La surface ne peut pas dépasser 10000 m²")
    private Double surface;

    @Min(value = 1, message = "Le nombre de pièces doit être au minimum 1")
    @Max(value = 50, message = "Le nombre de pièces ne peut pas dépasser 50")
    @Column(name = "number_of_rooms")
    private Integer numberOfRooms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @OneToOne
    @JoinColumn(name = "resident_id")
    private Resident resident;
}
