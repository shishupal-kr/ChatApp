package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.entity.ChatMessageEntity;
import com.shishupal.chatapp.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/history/{username}")
    public List<ChatMessageEntity> getChatHistory(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal
    ) {

        String currentUser = principal.getName();
        System.out.println("History requested by: " + currentUser);

        var pageable = org.springframework.data.domain.PageRequest.of(page, size);

        return chatMessageRepository
                .findConversationPaginated(currentUser, username, pageable)
                .getContent();
    }
}
