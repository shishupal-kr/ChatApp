package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.dto.ChatMessage;
import com.shishupal.chatapp.entity.ChatMessageEntity;
import com.shishupal.chatapp.repository.ChatMessageRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

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
                .build();

        chatMessageRepository.saveAndFlush(entity);
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


    @MessageMapping("/typing")
    public void typingIndicator(ChatMessage message, Principal principal) {

        if (principal == null) return;

        String sender = principal.getName();

        messagingTemplate.convertAndSendToUser(
                message.getReceiver(),
                "/queue/typing",
                sender + " is typing..."
        );
    }
}