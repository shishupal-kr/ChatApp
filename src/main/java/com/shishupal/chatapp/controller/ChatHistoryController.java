package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.entity.ChatMessageEntity;
import com.shishupal.chatapp.entity.ChatMessageEntity.MessageStatus;
import com.shishupal.chatapp.repository.ChatMessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatHistoryController(ChatMessageRepository chatMessageRepository,
                                 SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/history/{username}")
    public List<ChatMessageEntity> getChatHistory(
            @PathVariable String username,
            Principal principal
    ) {

        String currentUser = principal.getName();
        System.out.println("History requested by: " + currentUser);

        List<ChatMessageEntity> messages =
                chatMessageRepository.findConversation(currentUser, username);


        return messages;
    }
}