package com.syndico.syndicoapp.models;

import com.syndico.syndicoapp.models.enums.Priority;
import com.syndico.syndicoapp.models.enums.ReclamationCategory;
import com.syndico.syndicoapp.models.enums.ReclamationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reclamations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre de la réclamation est obligatoire")
    @Size(min = 3, max = 200, message = "Le titre doit contenir entre 3 et 200 caractères")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 5000, message = "La description doit contenir entre 10 et 5000 caractères")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReclamationCategory category;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.MOYENNE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReclamationStatus status = ReclamationStatus.NOUVELLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestataire_id")
    private Prestataire prestataire;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ElementCollection
    @CollectionTable(name = "reclamation_attachments",
            joinColumns = @JoinColumn(name = "reclamation_id"))
    @Column(name = "attachment_url")
    @Builder.Default
    private List<String> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // Méthodes métier
    public void assignToProvider(Prestataire provider) {
        this.prestataire = provider;
        this.status = ReclamationStatus.ASSIGNEE;
    }

    public void updateStatus(ReclamationStatus newStatus) {
        this.status = newStatus;
        if (newStatus == ReclamationStatus.RESOLUE || newStatus == ReclamationStatus.FERMEE) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
}
