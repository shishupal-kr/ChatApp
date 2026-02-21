package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.dto.ConversationDTO;
import com.shishupal.chatapp.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public List<ConversationDTO> getConversations(Principal principal) {

        if (principal == null) {
            return List.of();
        }

        return chatService.getConversations(principal.getName());
    }
}