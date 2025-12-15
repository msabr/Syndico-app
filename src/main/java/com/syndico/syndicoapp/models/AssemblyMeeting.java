package com.syndico.syndicoapp.models;

import com.syndico.syndicoapp.models.enums.MeetingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assembly_meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssemblyMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre de la réunion est obligatoire")
    @Size(min = 3, max = 200, message = "Le titre doit contenir entre 3 et 200 caractères")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "La date de la réunion est obligatoire")
    @Future(message = "La date de la réunion doit être dans le futur")
    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Size(max = 500, message = "Le lieu ne peut pas dépasser 500 caractères")
    private String location;

    @Size(max = 5000, message = "L'ordre du jour ne peut pas dépasser 5000 caractères")
    @Column(columnDefinition = "TEXT")
    private String agenda;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.PLANIFIEE;

    @Column(name = "minutes_url", length = 500)
    private String minutesUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "assemblyMeeting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vote> votes = new ArrayList<>();
}
