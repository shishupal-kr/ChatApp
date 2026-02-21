package com.shishupal.chatapp.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public OnlineUserService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void userConnected(String username) {
        onlineUsers.add(username);

        // Send full list to ALL users
        messagingTemplate.convertAndSend("/topic/online-users", onlineUsers);
    }

    public void userDisconnected(String username) {
        onlineUsers.remove(username);

        // Send updated list to ALL users
        messagingTemplate.convertAndSend("/topic/online-users", onlineUsers);
    }
}