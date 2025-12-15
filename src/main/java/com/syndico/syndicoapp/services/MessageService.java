package com.syndico.syndicoapp.services;

import com.syndico.syndicoapp.models.Message;
import com.syndico.syndicoapp.models.User;
import com.syndico.syndicoapp.repositories.MessageRepository;
import com.syndico.syndicoapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all messages
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public List<Message> getInboxMessages(Long userId) {
        return messageRepository.findByReceiver_IdOrderBySentAtDesc(userId);
    }

    public List<Message> getSentMessages(Long userId) {
        return messageRepository.findBySender_IdOrderBySentAtDesc(userId);
    }

    public long getUnreadMessagesCount(Long userId) {
        return messageRepository.countByReceiver_IdAndIsReadFalse(userId);
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
    }

    public Message sendMessage(Long senderId, Long receiverId, String subject, String content) {

        // Validation
        userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSubject(subject);
        message.setContent(content);
        message.setIsRead(false);

        return messageRepository.save(message);
    }

    // Broadcast
    public List<Message> sendBroadcastMessage(
            Long senderId,
            List<Long> receiverIds,
            String subject,
            String content
    ) {
        return receiverIds.stream()
                .map(id -> sendMessage(senderId, id, subject, content))
                .toList();
    }

    // Message to all residents
    public List<Message> sendMessageToAllResidents(
            Long senderId,
            String subject,
            String content
    ) {
        List<User> residents = userRepository.findByRole(
                com.syndico.syndicoapp.models.enums.UserRole.RESIDENT
        );

        return sendBroadcastMessage(
                senderId,
                residents.stream().map(User::getId).toList(),
                subject,
                content
        );
    }

    // Mark as read
    public Message markAsRead(Long messageId) {
        Message message = getMessageById(messageId);
        message.setIsRead(true);
        return messageRepository.save(message);
    }

    // Mark as unread
    public Message markAsUnread(Long messageId) {
        Message message = getMessageById(messageId);
        message.setIsRead(false);
        return messageRepository.save(message);
    }

    // Delete
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    // Conversation
    public List<Message> getConversation(Long user1Id, Long user2Id) {
        return messageRepository.findConversationBetweenUsers(user1Id, user2Id);
    }

    // Search inbox
    public List<Message> searchMessages(Long userId, String keyword) {
        return messageRepository
                .findByReceiver_IdAndSubjectContainingIgnoreCaseOrReceiver_IdAndContentContainingIgnoreCase(
                        userId, keyword,
                        userId, keyword
                );
    }
}
