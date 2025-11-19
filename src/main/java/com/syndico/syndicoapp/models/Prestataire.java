package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prestataires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestataire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String specialty;

    private String address;

    @Column
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "prestataire")
    @Builder.Default
    private List<Reclamation> reclamations = new ArrayList<>();

    @OneToMany(mappedBy = "prestataire")
    @Builder.Default
    private List<WorkProject> workProjects = new ArrayList<>();
}
