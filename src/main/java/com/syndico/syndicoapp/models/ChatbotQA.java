package com.syndico.syndicoapp.models;

import jakarta.persistence.*;
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

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
