package com.shishupal.chatapp.config;

import com.shishupal.chatapp.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String WS_AUTH_ATTR = "WS_AUTHENTICATION";
    private static final String WS_USERNAME_ATTR = "username";

    private final JwtService jwtService;

    //Authenticates WebSocket connection using JWT; persists authentication
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                org.springframework.messaging.support.MessageHeaderAccessor
                        .getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // 2) If we previously stored auth in the WebSocket session, restore it
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object existing = sessionAttributes.get(WS_AUTH_ATTR);
            if (existing instanceof Authentication auth) {
                accessor.setUser(auth);
                return message;
            }
        }

        // 2.5) Block SEND frames if no authenticated user is attached
        if (StompCommand.SEND.equals(accessor.getCommand())
                && accessor.getUser() == null) {
            System.out.println("Unauthenticated SEND blocked.");
            throw new IllegalArgumentException("User not authenticated");
        }

        // 3) Only attempt JWT extraction when client provides a token (usually on CONNECT)
        //    Be tolerant: header name can be "Authorization" or "authorization"
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            authHeaders = accessor.getNativeHeader("authorization");
        }
        if (authHeaders == null || authHeaders.isEmpty()) {
            return message;
        }

        String bearer = authHeaders.get(0);
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return message;
        }

        String token = bearer.substring(7);
        String username = jwtService.extractUsername(token);
        if (username == null || username.isBlank()) {
            return message;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.emptyList()
                );

        accessor.setUser(authentication);

        // Persist for later frames (SEND/SUBSCRIBE) in the same WebSocket session
        if (sessionAttributes != null) {
            sessionAttributes.put(WS_AUTH_ATTR, authentication);
            sessionAttributes.put(WS_USERNAME_ATTR, username);
        }

        // Optional: helpful if any code relies on SecurityContext in the same thread
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("WebSocket CONNECT authenticated: " + username);
        }

        return message;
    }
}
