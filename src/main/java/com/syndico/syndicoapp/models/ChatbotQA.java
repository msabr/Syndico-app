package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_qa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotQA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La question est obligatoire")
    @Size(min = 5, max = 1000, message = "La question doit contenir entre 5 et 1000 caractères")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @NotBlank(message = "La réponse est obligatoire")
    @Size(min = 5, max = 5000, message = "La réponse doit contenir entre 5 et 5000 caractères")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Size(max = 100, message = "La catégorie ne peut pas dépasser 100 caractères")
    private String category;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public String findAnswer(String userQuery) {
        if (this.question.toLowerCase().contains(userQuery.toLowerCase())) {
            return this.answer;
        }
        return null;
    }
}
