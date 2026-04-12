package com.syndico.syndicoapp.controllers.resident;

import com.syndico.syndicoapp.models.*;
import com.syndico.syndicoapp.models.enums.*;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.DocumentService;
import com.syndico.syndicoapp.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ResidentDocumentController {

    private final DocumentService documentService;
    private final ResidentService residentService;

    @GetMapping("/documents")
    public String documents(
            @RequestParam(required = false) DocumentCategory category,
            @RequestParam(required = false) String search,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Resident resident = residentService.getResidentByUserId(userDetails.getId());

            List<Document> documents;

            if (search != null && !search.isEmpty()) {
                // Search documents
                documents = documentService.searchDocuments(search);
            } else if (category != null) {
                // Filter by category
                documents = documentService.getDocumentsByCategory(category);
            } else {
                // Get all public documents
                documents = documentService.getPublicDocuments();
            }

            // Filter only public documents for residents
            documents = documents.stream()
                .filter(Document::getIsPublic)
                .collect(Collectors.toList());

            // Get statistics
            long totalDocuments = documents.size();
            long recentDocuments = documents.stream()
                .filter(d -> d.getUploadedAt().isAfter(LocalDateTime.now().minusDays(7)))
                .count();

            model.addAttribute("resident", resident);
            model.addAttribute("documents", documents);
            model.addAttribute("categories", DocumentCategory.values());
            model.addAttribute("selectedCategory", category);
            model.addAttribute("searchQuery", search);
            model.addAttribute("totalDocuments", totalDocuments);
            model.addAttribute("recentDocuments", recentDocuments);

            return "client/information/documents";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading documents: " + e.getMessage());
            model.addAttribute("documents", new ArrayList<>());
            return "client/information/documents";
        }
    }
}

