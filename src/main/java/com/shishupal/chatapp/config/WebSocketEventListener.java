package com.shishupal.chatapp.config;

import com.shishupal.chatapp.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate messagingTemplate;

    // Handles authenticated user connection events
    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        String username = null;
        if (event.getUser() != null) {
            username = event.getUser().getName();
        } else if (accessor.getSessionAttributes() != null) {
            username = (String) accessor.getSessionAttributes().get("username");
        }

        if (username != null && sessionId != null) {
            onlineUserService.userConnected(username, sessionId);

            messagingTemplate.convertAndSend(
                    "/topic/online-users",
                    onlineUserService.getOnlineUsers()
            );

            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/online-users",
                    onlineUserService.getOnlineUsers()
            );
        }
    }

    // Handles authenticated user disconnection events
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = null;
        String sessionId = accessor.getSessionId();

        if (accessor.getUser() != null) {
            username = accessor.getUser().getName();
        } else if (accessor.getSessionAttributes() != null) {
            username = (String) accessor.getSessionAttributes().get("username");
        }

        if (username != null && sessionId != null) {
            onlineUserService.userDisconnected(username, sessionId);

            messagingTemplate.convertAndSend(
                    "/topic/online-users",
                    onlineUserService.getOnlineUsers()
            );
        }
    }
}
