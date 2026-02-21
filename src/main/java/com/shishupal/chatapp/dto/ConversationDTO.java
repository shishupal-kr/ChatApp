package com.shishupal.chatapp.dto;

import java.time.LocalDateTime;

public class ConversationDTO {

    private String username;
    private String lastMessage;
    private LocalDateTime timestamp;

    public ConversationDTO(String username, String lastMessage, LocalDateTime timestamp) {
        this.username = username;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}