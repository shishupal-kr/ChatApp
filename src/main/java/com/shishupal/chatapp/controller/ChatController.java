package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.dto.ChatMessage;
import com.shishupal.chatapp.dto.TypingDTO;
import com.shishupal.chatapp.entity.ChatMessageEntity;
import com.shishupal.chatapp.repository.ChatMessageRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    // Manual constructor injection (No Lombok needed)
    public ChatController(SimpMessagingTemplate messagingTemplate,
                          ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
    }

    @MessageMapping("/private-message")
    @Transactional
    public void sendPrivateMessage(ChatMessage message,
                                   org.springframework.messaging.simp.SimpMessageHeaderAccessor accessor) {

        System.out.println("PRIVATE MESSAGE METHOD CALLED");

        if (accessor.getUser() == null) {
            System.out.println("No authenticated user found in headers.");
            return;
        }

        String sender = accessor.getUser().getName();

        String receiver = message.getReceiver();

        if (receiver == null || receiver.isBlank()) {
            System.out.println("Receiver is empty.");
            return;
        }

        if (message.getContent() == null || message.getContent().isBlank()) {
            System.out.println("Message content is empty. Not saving.");
            return;
        }

        System.out.println("Sender: " + sender);
        System.out.println("Receiver: " + receiver);

        message.setSender(sender);

        ChatMessageEntity entity = ChatMessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .content(message.getContent())
                .timestamp(LocalDateTime.now())
                .status("DELIVERED") // directly delivered
                .build();

        chatMessageRepository.save(entity);
        message.setId(entity.getId());

        System.out.println("MESSAGE SAVED TO DATABASE SUCCESSFULLY");

        messagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/messages",
                message
        );

        messagingTemplate.convertAndSendToUser(
                sender,
                "/queue/messages",
                message
        );
    }

    // typing indicator
    @MessageMapping("/typing")
    public void typingIndicator(TypingDTO dto, Principal principal) {

        if (principal == null) return;
        if (dto == null || dto.getReceiver() == null || dto.getReceiver().isBlank()) return;

        String sender = principal.getName();

        messagingTemplate.convertAndSendToUser(
                dto.getReceiver(),
                "/queue/typing",
                sender
        );
    }

    //mark as read
    @MessageMapping("/read")
    @Transactional
    public void markAsRead(ChatMessage message, Principal principal) {

        if (principal == null) return;

        String currentUser = principal.getName();
        String otherUser = message.getReceiver();

        if (otherUser == null || otherUser.isBlank()) return;

        java.util.List<com.shishupal.chatapp.entity.ChatMessageEntity> messages =
                chatMessageRepository.findConversation(otherUser, currentUser);

        for (ChatMessageEntity m : messages) {
            if (m.getReceiver().equals(currentUser)
                    && !"READ".equals(m.getStatus())) {
                m.setStatus("READ");
            }
        }

        chatMessageRepository.saveAll(messages);
    }

    //delete message
    @MessageMapping("/delete")
    @Transactional
    public void deleteMessage(ChatMessage message, Principal principal) {

        if (principal == null) return;

        String currentUser = principal.getName();

        if (message.getId() == null) return;

        var optional = chatMessageRepository.findById(message.getId());

        if (optional.isEmpty()) return;

        ChatMessageEntity entity = optional.get();

        // Only sender can delete
        if (!entity.getSender().equals(currentUser)) return;

        chatMessageRepository.delete(entity);

        // Notify both users
        messagingTemplate.convertAndSendToUser(
                entity.getReceiver(),
                "/queue/delete",
                message.getId()
        );

        messagingTemplate.convertAndSendToUser(
                entity.getSender(),
                "/queue/delete",
                message.getId()
        );
    }
}