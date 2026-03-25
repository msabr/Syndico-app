package com.syndico.syndicoapp.models;

import com.syndico.syndicoapp.models.enums.WorkStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "work_projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre du projet est obligatoire")
    @Size(min = 3, max = 200, message = "Le titre doit contenir entre 3 et 200 caractères")
    @Column(nullable = false)
    private String title;

    @Size(max = 5000, message = "La description ne peut pas dépasser 5000 caractères")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WorkStatus status = WorkStatus.PLANIFIE;

    @DecimalMin(value = "0.0", message = "Le budget ne peut pas être négatif")
    @DecimalMax(value = "100000000.0", message = "Le budget ne peut pas dépasser 100 000 000")
    private Double budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id")
    private Prestataire prestataire;

    @OneToMany(mappedBy = "workProject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimelineStage> timelineStages = new ArrayList<>();
}
