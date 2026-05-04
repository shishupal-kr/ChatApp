package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.dto.ChatMessage;
import com.shishupal.chatapp.dto.TypingDTO;
import com.shishupal.chatapp.entity.ChatMessageEntity;
import com.shishupal.chatapp.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private static final ZoneId KOLKATA_ZONE = ZoneId.of("Asia/Kolkata");

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final com.shishupal.chatapp.service.OnlineUserService onlineUserService;

    //Handles private messages; persists; sends; notifies sender
    @MessageMapping("/private-message")
    @Transactional
    public void sendPrivateMessage(ChatMessage message,
                                   org.springframework.messaging.simp.SimpMessageHeaderAccessor accessor) {

        if (accessor.getUser() == null) {
            return;
        }

        String sender = accessor.getUser().getName();
        String receiver = message.getReceiver();

        if (receiver == null || receiver.isBlank()) {
            return;
        }

        if (message.getContent() == null || message.getContent().isBlank()) {
            return;
        }

        message.setSender(sender);

        Long replyToId = message.getReplyToId();
        ChatMessageEntity replyToEntity = null;
        if (replyToId != null) {
            replyToEntity = chatMessageRepository.findById(replyToId).orElse(null);
        }

        // Builds persistable message from sender, receiver, content
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .content(message.getContent())
                .timestamp(LocalDateTime.now(KOLKATA_ZONE))
                .status(ChatMessageEntity.MessageStatus.SENT)
                .replyToId(replyToEntity == null ? null : replyToEntity.getId())
                .replyToSender(replyToEntity == null ? null : replyToEntity.getSender())
                .replyToContent(replyToEntity == null ? null : replyToEntity.getContent())
                .build();

         chatMessageRepository.save(entity);
         message.setId(entity.getId());
         message.setTimestamp(entity.getTimestamp());
         if (replyToEntity != null) {
             message.setReplyToId(replyToEntity.getId());
             message.setReplyToSender(replyToEntity.getSender());
             message.setReplyToContent(replyToEntity.getContent());
         }

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

        // Notify receiver's friends page to update unread count
        messagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/unread-update",
                sender
        );

        // Mark as DELIVERED only if receiver is online
        if (onlineUserService.getOnlineUsers().contains(receiver)) {
            entity.setStatus(ChatMessageEntity.MessageStatus.DELIVERED);
            chatMessageRepository.save(entity);

            // Notify sender that message was delivered
            messagingTemplate.convertAndSendToUser(
                    sender,
                    "/queue/delivered",
                    String.valueOf(entity.getId())
            );
        }
    }

    // Typing indicator for receiver
    @MessageMapping("/typing")
    public void typingIndicator(TypingDTO dto, Principal principal) {

        if (principal == null) return;
        if (dto == null || dto.getReceiver() == null || dto.getReceiver().isBlank()) return;

        String sender = principal.getName();

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("sender", sender);

        messagingTemplate.convertAndSendToUser(
                dto.getReceiver(),
                "/queue/typing",
                response
        );
    }

    //Mark as read the message
    @MessageMapping("/read")
    @Transactional
    public void markAsRead(ChatMessage message, Principal principal) {

        if (principal == null) return;

        String currentUser = principal.getName();
        String otherUser = message.getReceiver();

        if (otherUser == null || otherUser.isBlank()) return;

        java.util.List<com.shishupal.chatapp.entity.ChatMessageEntity> messages =
                chatMessageRepository.findConversation(otherUser, currentUser);

        // Updates message statuses; notifies sender of read receipts
        for (ChatMessageEntity m : messages) {
            if (m.getReceiver().equals(currentUser)
                    && m.getStatus() != ChatMessageEntity.MessageStatus.READ) {
                m.setStatus(ChatMessageEntity.MessageStatus.READ);

                // notify sender about the read receipt
                messagingTemplate.convertAndSendToUser(
                        m.getSender(),
                        "/queue/read-receipt",
                        String.valueOf(m.getId())
                );
            }
        }

        chatMessageRepository.saveAll(messages);
    }

    @MessageMapping("/edit")
    @Transactional
    public void editMessage(ChatMessage message, Principal principal) {

        if (principal == null) return;

        var optional = chatMessageRepository.findById(message.getId());
        if (optional.isEmpty()) return;

        ChatMessageEntity entity = optional.get();

        if (!entity.getSender().equals(principal.getName())) return;

        entity.setContent(message.getContent());
        entity.setEdited(true);

        chatMessageRepository.save(entity);

        // Update DTO before sending
        message.setId(entity.getId());
        message.setContent(entity.getContent());
        message.setTimestamp(entity.getTimestamp());
        message.setEdited(true);

        messagingTemplate.convertAndSendToUser(
                entity.getReceiver(),
                "/queue/edit",
                message
        );

        messagingTemplate.convertAndSendToUser(
                entity.getSender(),
                "/queue/edit",
                message
        );
    }

    //Delete a message
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
