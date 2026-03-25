package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReclamationId(Long reclamationId);

    long countByReclamationId(Long reclamationId);
    List<Comment> findByReclamationIdOrderByCreatedAtDesc(Long reclamationId);
}
