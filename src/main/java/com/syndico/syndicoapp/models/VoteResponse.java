package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "vote_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vote_id", "resident_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le vote est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @NotNull(message = "Le résident est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @NotBlank(message = "L'option sélectionnée est obligatoire")
    @Column(name = "selected_option", nullable = false)
    private String selectedOption;

    @CreationTimestamp
    @Column(name = "voted_at", updatable = false)
    private LocalDateTime votedAt;
}
