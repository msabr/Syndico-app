package com.syndico.syndicoapp.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.syndico.syndicoapp.models.Document;
import com.syndico.syndicoapp.models.enums.DocumentCategory;
import com.syndico.syndicoapp.repositories.DocumentRepository;
import com.syndico.syndicoapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/documents/";
    private static final String QR_DIR = "uploads/qrcodes/";

    public DocumentService() {
        // Create directories if they don't exist
        createDirectoryIfNotExists(UPLOAD_DIR);
        createDirectoryIfNotExists(QR_DIR);
    }

    private void createDirectoryIfNotExists(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // Get all documents
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    // Get documents by category
    public List<Document> getDocumentsByCategory(DocumentCategory category) {
        return documentRepository.findByCategory(category);
    }

    // Get public documents only
    public List<Document> getPublicDocuments() {
        return documentRepository.findByIsPublicTrue();
    }

    // Get document by ID
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

    // Create new document
    public Document createDocument(String title, String description,
                                   DocumentCategory category, boolean isPublic,
                                   MultipartFile file, Long uploadedBy) throws IOException {

        // Save file
        String fileName = saveFile(file);
        String fileUrl = "/uploads/documents/" + fileName;

        // Récupérer l'utilisateur depuis la base de données
        var user = userRepository.findById(uploadedBy)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + uploadedBy));

        // Create document entity
        Document document = new Document();
        document.setTitle(title);
        document.setDescription(description);
        document.setFileUrl(fileUrl);
        document.setFileType(getFileExtension(file.getOriginalFilename()));
        document.setCategory(category);
        document.setIsPublic(isPublic);
        document.setUploadedBy(user);  // utiliser l'objet User
        document.setUploadedAt(LocalDateTime.now());

        // Save to database and generate QR code
        Document savedDocument = documentRepository.save(document);
        String qrCodeUrl = generateQRCode(savedDocument.getId(), fileUrl);
        savedDocument.setQrCodeUrl(qrCodeUrl);

        return documentRepository.save(savedDocument);
    }

    // Update document
    public Document updateDocument(Long id, String title, String description,
                                   DocumentCategory category, boolean isPublic,
                                   MultipartFile file) throws IOException {

        Document document = getDocumentById(id);

        document.setTitle(title);
        document.setDescription(description);
        document.setCategory(category);
        document.setIsPublic(isPublic);

        // Update file if new file is provided
        if (file != null && !file.isEmpty()) {
            // Delete old file
            deleteFile(document.getFileUrl());

            // Save new file
            String fileName = saveFile(file);
            String fileUrl = "/uploads/documents/" + fileName;
            document.setFileUrl(fileUrl);
            document.setFileType(getFileExtension(file.getOriginalFilename()));

            // Regenerate QR Code
            String qrCodeUrl = generateQRCode(id, fileUrl);
            document.setQrCodeUrl(qrCodeUrl);
        }

        return documentRepository.save(document);
    }

    // Delete document
    public void deleteDocument(Long id) {
        Document document = getDocumentById(id);

        // Delete physical files
        deleteFile(document.getFileUrl());
        deleteFile(document.getQrCodeUrl());

        // Delete from database
        documentRepository.deleteById(id);
    }

    // Save uploaded file
    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + extension;

        Path targetLocation = Paths.get(UPLOAD_DIR + fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    // Delete file from filesystem
    private void deleteFile(String fileUrl) {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            try {
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Error deleting file: " + e.getMessage());
            }
        }
    }

    // Generate QR Code
    private String generateQRCode(Long documentId, String fileUrl) {
        try {
            String qrContent = "http://localhost:8080/documents/view/" + documentId;
            String qrFileName = "qr_" + documentId + ".png";
            String qrFilePath = QR_DIR + qrFileName;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);

            Path path = Paths.get(qrFilePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            return "/uploads/qrcodes/" + qrFileName;
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            return null;
        }
    }

    // Get file extension
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toUpperCase();
    }

    // Search documents
    public List<Document> searchDocuments(String keyword) {
        return documentRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }
}
