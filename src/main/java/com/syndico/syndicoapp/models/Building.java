package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du bâtiment est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(min = 5, max = 500, message = "L'adresse doit contenir entre 5 et 500 caractères")
    @Column(nullable = false, length = 500)
    private String address;

    @Min(value = 1, message = "Le nombre d'étages doit être au minimum 1")
    @Max(value = 200, message = "Le nombre d'étages ne peut pas dépasser 200")
    @Column(name = "number_of_floors")
    private Integer numberOfFloors;

    @Min(value = 1, message = "Le nombre d'appartements doit être au minimum 1")
    @Max(value = 1000, message = "Le nombre d'appartements ne peut pas dépasser 1000")
    @Column(name = "number_of_apartments")
    private Integer numberOfApartments;

    @Min(value = 1800, message = "L'année de construction ne peut pas être antérieure à 1800")
    @Max(value = 2100, message = "L'année de construction ne peut pas dépasser 2100")
    @Column(name = "year_built")
    private Integer yearBuilt;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relations
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Resident> residents = new ArrayList<>();

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Apartment> apartments = new ArrayList<>();
}
