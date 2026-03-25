package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Comment;
import com.syndico.syndicoapp.models.Reclamation;
import com.syndico.syndicoapp.models.User;
import com.syndico.syndicoapp.repositories.CommentRepository;
import com.syndico.syndicoapp.repositories.ReclamationRepository;
import com.syndico.syndicoapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReclamationRepository reclamationRepository;

    @Autowired
    private UserRepository userRepository;

    // Add comment to reclamation
    public Comment addComment(Long reclamationId, Long userId, String content, boolean isAdminComment) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new RuntimeException("Reclamation not found with id: " + reclamationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setReclamation(reclamation);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setIsAdminComment(isAdminComment);

        return commentRepository.save(comment);
    }

    // Get all comments for a reclamation
    public List<Comment> getCommentsByReclamation(Long reclamationId) {
        return commentRepository.findByReclamationIdOrderByCreatedAtDesc(reclamationId);
    }

    // Get comment by ID
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    // Delete comment
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        commentRepository.delete(comment);
    }

    // Update comment
    public Comment updateComment(Long id, String newContent) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    // Count comments by reclamation
    public long countCommentsByReclamation(Long reclamationId) {
        return commentRepository.countByReclamationId(reclamationId);
    }
}
