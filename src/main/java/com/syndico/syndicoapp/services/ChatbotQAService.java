package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.ChatbotQA;
import com.syndico.syndicoapp.repositories.ChatbotQARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatbotQAService {

    @Autowired
    private ChatbotQARepository chatbotQARepository;

    // Create Q&A
    public ChatbotQA createQA(ChatbotQA chatbotQA) {
        if (chatbotQA.getCreatedAt() == null) {
            chatbotQA.setCreatedAt(LocalDateTime.now());
        }
        if (chatbotQA.getIsActive() == null) {
            chatbotQA.setIsActive(true);
        }
        return chatbotQARepository.save(chatbotQA);
    }

    // Get all Q&As
    public List<ChatbotQA> getAllQAs() {
        return chatbotQARepository.findAll();
    }

    // Get active Q&As
    public List<ChatbotQA> getActiveQAs() {
        return chatbotQARepository.findByIsActiveTrue();
    }

    // Get Q&A by ID
    public Optional<ChatbotQA> getQAById(Long id) {
        return chatbotQARepository.findById(id);
    }

    // Update Q&A
    public ChatbotQA updateQA(Long id, ChatbotQA qaDetails) {
        ChatbotQA chatbotQA = chatbotQARepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Q&A not found with id: " + id));

        chatbotQA.setQuestion(qaDetails.getQuestion());
        chatbotQA.setAnswer(qaDetails.getAnswer());
        chatbotQA.setCategory(qaDetails.getCategory());
        chatbotQA.setIsActive(qaDetails.getIsActive());

        return chatbotQARepository.save(chatbotQA);
    }

    // Delete Q&A
    public void deleteQA(Long id) {
        ChatbotQA chatbotQA = chatbotQARepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Q&A not found with id: " + id));
        chatbotQARepository.delete(chatbotQA);
    }

    // Toggle active status
    public ChatbotQA toggleActiveStatus(Long id) {
        ChatbotQA chatbotQA = chatbotQARepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Q&A not found with id: " + id));

        chatbotQA.setIsActive(!chatbotQA.getIsActive());
        return chatbotQARepository.save(chatbotQA);
    }

    // Search Q&A
    public List<ChatbotQA> searchQAs(String keyword) {
        return chatbotQARepository.findAll().stream()
                .filter(qa -> qa.getQuestion().toLowerCase().contains(keyword.toLowerCase()) ||
                        qa.getAnswer().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Find answer by question (for chatbot usage)
    public String findAnswer(String userQuery) {
        String normalizedQuery = userQuery.toLowerCase().trim();

        List<ChatbotQA> activeQAs = chatbotQARepository.findByIsActiveTrue();

        // Try exact match first
        for (ChatbotQA qa : activeQAs) {
            if (qa.getQuestion().toLowerCase().equals(normalizedQuery)) {
                return qa.getAnswer();
            }
        }

        // Try partial match
        for (ChatbotQA qa : activeQAs) {
            if (qa.getQuestion().toLowerCase().contains(normalizedQuery) ||
                    normalizedQuery.contains(qa.getQuestion().toLowerCase())) {
                return qa.getAnswer();
            }
        }

        return "Je suis désolé, je n'ai pas trouvé de réponse à votre question. Veuillez contacter l'administration.";
    }

    // Get statistics
    public ChatbotStatistics getStatistics() {
        List<ChatbotQA> allQAs = chatbotQARepository.findAll();

        long totalQAs = allQAs.size();
        long activeQAs = allQAs.stream()
                .filter(ChatbotQA::getIsActive)
                .count();
        long inactiveQAs = totalQAs - activeQAs;

        // Group by category
        Map<String, Long> byCategory = allQAs.stream()
                .filter(qa -> qa.getCategory() != null && !qa.getCategory().isEmpty())
                .collect(Collectors.groupingBy(ChatbotQA::getCategory, Collectors.counting()));

        return new ChatbotStatistics(totalQAs, activeQAs, inactiveQAs, byCategory);
    }

    // Inner class for statistics
    public static class ChatbotStatistics {
        private long totalQAs;
        private long activeQAs;
        private long inactiveQAs;
        private Map<String, Long> byCategory;

        public ChatbotStatistics(long totalQAs, long activeQAs, long inactiveQAs, Map<String, Long> byCategory) {
            this.totalQAs = totalQAs;
            this.activeQAs = activeQAs;
            this.inactiveQAs = inactiveQAs;
            this.byCategory = byCategory;
        }

        public long getTotalQAs() { return totalQAs; }
        public long getActiveQAs() { return activeQAs; }
        public long getInactiveQAs() { return inactiveQAs; }
        public Map<String, Long> getByCategory() { return byCategory; }
    }
}
