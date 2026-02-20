package com.shishupal.chatapp.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void userOnline(String username) {
        onlineUsers.add(username);
    }

    public void userOffline(String username) {
        onlineUsers.remove(username);
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}