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

    @GetMapping("/users/search")
    public List<String> searchUsers(@RequestParam String keyword,
                                    Principal principal) {

        if (principal == null) {
            return List.of();
        }

        if (keyword == null || keyword.trim().length() < 2) {
            return List.of();
        }

        return chatService.searchUsers(keyword.trim(), principal.getName());
    }

    @GetMapping("/online-users")
    public List<String> getOnlineUsers() {
        return List.copyOf(chatService.getOnlineUsers());
    }

    @GetMapping("/unread-counts")
    public java.util.Map<String, Long> getUnreadCounts(Principal principal) {
        if (principal == null) {
            return java.util.Map.of();
        }
        return chatService.getUnreadCounts(principal.getName());
    }

}
