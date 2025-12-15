package com.syndico.syndicoapp.models;

import com.syndico.syndicoapp.models.enums.DocumentCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre du document est obligatoire")
    @Size(min = 2, max = 200, message = "Le titre doit contenir entre 2 et 200 caractères")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "L'URL du fichier est obligatoire")
    @Size(max = 500, message = "L'URL du fichier ne peut pas dépasser 500 caractères")
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Size(max = 50, message = "Le type de fichier ne peut pas dépasser 50 caractères")
    @Column(name = "file_type", length = 50)
    private String fileType;

    @NotNull(message = "La catégorie du document est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentCategory category;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    public void generateQRCode() {
        // Logique de génération QR Code
        this.qrCodeUrl = "/qr/" + this.id + ".png";

    }
}
