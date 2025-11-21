package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.ChatbotQA;
import com.syndico.syndicoapp.repositories.ChatbotQARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatbotService {

    @Autowired
    private ChatbotQARepository chatbotQARepository;

    public String getAnswer(String question) {

        List<ChatbotQA> allQA = chatbotQARepository.findByIsActiveTrue();

        for (ChatbotQA qa : allQA){
            String answer = qa.findAnswer(question);
            if (answer != null)
                return answer;
        }

        return "I couldn't find an answer. Please contact support@syndico.ma";
    }
}
