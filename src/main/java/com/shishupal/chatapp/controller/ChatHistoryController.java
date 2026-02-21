package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.entity.ChatMessageEntity;
import com.shishupal.chatapp.repository.ChatMessageRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    private final ChatMessageRepository chatMessageRepository;

    public ChatHistoryController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @GetMapping("/history/{username}")
    public List<ChatMessageEntity> getChatHistory(
            @PathVariable String username,
            Principal principal
    ) {

        String currentUser = principal.getName();
        System.out.println("History requested by: " + currentUser);

        return chatMessageRepository.findConversation(currentUser, username);
    }
}