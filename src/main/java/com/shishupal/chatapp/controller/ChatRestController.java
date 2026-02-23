package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.dto.ConversationDTO;
import com.shishupal.chatapp.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public List<ConversationDTO> getConversations(Principal principal) {

        if (principal == null) {
            return List.of();
        }

        return chatService.getConversations(principal.getName());
    }
}