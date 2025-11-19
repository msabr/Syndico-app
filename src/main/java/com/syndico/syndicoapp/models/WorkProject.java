package com.syndico.syndicoapp.models;

import com.syndico.syndicoapp.models.enums.WorkStatus;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WorkStatus status = WorkStatus.PLANIFIE;

    private Double budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id")
    private Prestataire prestataire;

    @OneToMany(mappedBy = "workProject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimelineStage> timelineStages = new ArrayList<>();
}
