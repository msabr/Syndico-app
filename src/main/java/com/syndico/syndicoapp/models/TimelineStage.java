package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "timeline_stages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le projet de travaux est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_project_id", nullable = false)
    private WorkProject workProject;

    @NotBlank(message = "Le nom de l'étape est obligatoire")
    @Size(min = 2, max = 200, message = "Le nom de l'étape doit contenir entre 2 et 200 caractères")
    @Column(name = "stage_name", nullable = false)
    private String stageName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Size(max = 50, message = "Le statut ne peut pas dépasser 50 caractères")
    @Column(length = 50)
    @Builder.Default
    private String status = "Pending";

    @Size(max = 2000, message = "Les notes ne peuvent pas dépasser 2000 caractères")
    @Column(columnDefinition = "TEXT")
    private String notes;
}
