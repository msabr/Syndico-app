package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Document;
import com.syndico.syndicoapp.models.enums.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByCategory(DocumentCategory category);
    List<Document> findByIsPublicTrue();
}
