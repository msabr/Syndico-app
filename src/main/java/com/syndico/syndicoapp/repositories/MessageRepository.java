package com.syndico.syndicoapp.repositories;

import com.syndico.syndicoapp.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Recherche avec JPQL
    @Query("""
        SELECT m FROM Message m
        WHERE m.receiver.id = :receiverId
        AND (
            LOWER(m.subject) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(m.content) LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    List<Message> searchByReceiverAndQuery(
            @Param("receiverId") Long receiverId,
            @Param("q") String q
    );

    // Conversation entre deux utilisateurs
    @Query("""
        SELECT m FROM Message m
        WHERE 
          (m.sender.id = :user1Id AND m.receiver.id = :user2Id)
          OR
          (m.sender.id = :user2Id AND m.receiver.id = :user1Id)
        ORDER BY m.sentAt ASC
    """)
    List<Message> findConversationBetweenUsers(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id
    );

    // Messages envoyés
    List<Message> findBySender_IdOrderBySentAtDesc(Long userId);

    // Messages non lus
    long countByReceiver_IdAndIsReadFalse(Long userId);

    // Messages reçus
    List<Message> findByReceiver_IdOrderBySentAtDesc(Long userId);

    // Recherche simple
    List<Message> findByReceiver_IdAndSubjectContainingIgnoreCaseOrReceiver_IdAndContentContainingIgnoreCase(
            Long userId1, String keyword1,
            Long userId2, String keyword2
    );
}
