package com.shishupal.chatapp.config;

import com.shishupal.chatapp.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            List<String> authHeader =
                    accessor.getNativeHeader("Authorization");

            if (authHeader != null && !authHeader.isEmpty()) {

                String token = authHeader.get(0).substring(7);
                String username = jwtService.extractUsername(token);

                accessor.setUser(new Principal() {
                    @Override
                    public String getName() {
                        return username;
                    }
                });
            }
        }

        return message;
    }
}