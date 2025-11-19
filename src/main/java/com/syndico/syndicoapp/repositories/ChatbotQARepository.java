package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.ChatbotQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatbotQARepository extends JpaRepository<ChatbotQA, Long> {
    List<ChatbotQA> findByIsActiveTrue();
}
