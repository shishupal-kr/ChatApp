package com.shishupal.chatapp.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    private final java.util.Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void userConnected(String username, String sessionId) {
        userSessions.compute(username, (user, sessions) -> {
            if (sessions == null) {
                sessions = ConcurrentHashMap.newKeySet();
            }
            sessions.add(sessionId);
            return sessions;
        });
    }

    public void userDisconnected(String username, String sessionId) {
        userSessions.computeIfPresent(username, (user, sessions) -> {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                return null; // remove user completely
            }
            return sessions;
        });
    }

    public Set<String> getOnlineUsers() {
        return userSessions.keySet();
    }
}
