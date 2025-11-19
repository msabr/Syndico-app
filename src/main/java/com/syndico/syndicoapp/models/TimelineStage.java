package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_project_id", nullable = false)
    private WorkProject workProject;

    @Column(name = "stage_name", nullable = false)
    private String stageName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 50)
    @Builder.Default
    private String status = "Pending";

    @Column(columnDefinition = "TEXT")
    private String notes;
}
