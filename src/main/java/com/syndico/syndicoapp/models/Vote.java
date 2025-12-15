package com.syndico.syndicoapp.models;

import com.syndico.syndicoapp.models.enums.VoteStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "votes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La réunion d'assemblée est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assembly_meeting_id", nullable = false)
    private AssemblyMeeting assemblyMeeting;

    @NotBlank(message = "La question est obligatoire")
    @Size(min = 5, max = 1000, message = "La question doit contenir entre 5 et 1000 caractères")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @NotEmpty(message = "Les options de vote sont obligatoires")
    @Size(min = 2, message = "Il doit y avoir au minimum 2 options")
    @ElementCollection
    @CollectionTable(name = "vote_options", joinColumns = @JoinColumn(name = "vote_id"))
    @Column(name = "option_text")
    private List<String> options;

    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VoteStatus status = VoteStatus.OUVERT;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VoteResponse> responses = new ArrayList<>();
}
