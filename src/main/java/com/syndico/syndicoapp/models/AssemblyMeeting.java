package com.syndico.syndicoapp.models;

import com.syndico.syndicoapp.models.enums.MeetingStatus;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String title;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    private String location;

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
