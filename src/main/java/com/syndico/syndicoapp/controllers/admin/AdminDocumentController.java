package com.syndico.syndicoapp.controllers.admin;

import com.syndico.syndicoapp.models.Document;
import com.syndico.syndicoapp.models.enums.DocumentCategory;
import com.syndico.syndicoapp.security.CustomUserDetails;
import com.syndico.syndicoapp.services.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final DocumentService documentService;

    @GetMapping
    public String listDocuments(Model model,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String search) {
        List<Document> documents;

        if (search != null && !search.isEmpty()) {
            documents = documentService.searchDocuments(search);
        } else if (category != null && !category.isEmpty() && !category.equals("ALL")) {
            DocumentCategory docCategory = DocumentCategory.valueOf(category);
            documents = documentService.getDocumentsByCategory(docCategory);
        } else {
            documents = documentService.getAllDocuments();
        }

        model.addAttribute("documents", documents);
        model.addAttribute("categories", DocumentCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchTerm", search);

        return "admin/documents/list";
    }

    @GetMapping("/{id}")
    public String viewDocument(@PathVariable Long id, Model model) {
        Document document = documentService.getDocumentById(id);
        model.addAttribute("document", document);
        return "admin/documents/details";
    }

    @GetMapping("/new")
    public String newDocumentForm(Model model) {
        model.addAttribute("document", new Document());
        model.addAttribute("categories", DocumentCategory.values());
        return "admin/documents/form";
    }

    @PostMapping("/create")
    public String createDocument(@RequestParam("title") String title,
                                 @RequestParam("description") String description,
                                 @RequestParam("category") DocumentCategory category,
                                 @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/admin/documents/new";
            }

            Long userId = userDetails.getId();
            documentService.createDocument(title, description, category, isPublic, file, userId);

            redirectAttributes.addFlashAttribute("success", "Document uploaded successfully");
            return "redirect:/admin/documents";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload document: " + e.getMessage());
            return "redirect:/admin/documents/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String editDocumentForm(@PathVariable Long id, Model model) {
        Document document = documentService.getDocumentById(id);
        model.addAttribute("document", document);
        model.addAttribute("categories", DocumentCategory.values());
        return "admin/documents/form";
    }

    @PostMapping("/update/{id}")
    public String updateDocument(@PathVariable Long id,
                                 @RequestParam("title") String title,
                                 @RequestParam("description") String description,
                                 @RequestParam("category") DocumentCategory category,
                                 @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            documentService.updateDocument(id, title, description, category, isPublic, file);
            redirectAttributes.addFlashAttribute("success", "Document updated successfully");
            return "redirect:/admin/documents";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update document: " + e.getMessage());
            return "redirect:/admin/documents/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            documentService.deleteDocument(id);
            redirectAttributes.addFlashAttribute("success", "Document deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete document: " + e.getMessage());
        }
        return "redirect:/admin/documents";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            Document document = documentService.getDocumentById(id);
            String filePath = "uploads/documents/" + document.getFileUrl().substring(document.getFileUrl().lastIndexOf("/") + 1);

            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getTitle() + "." + document.getFileType() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read file: " + document.getTitle());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file: " + e.getMessage());
        }
    }
}

