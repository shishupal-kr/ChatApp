package com.shishupal.chatapp.service;

import java.util.HashMap;
import java.util.Map;

import com.shishupal.chatapp.dto.ConversationDTO;
import com.shishupal.chatapp.entity.ChatMessageEntity;
import com.shishupal.chatapp.repository.ChatMessageRepository;
import com.shishupal.chatapp.service.OnlineUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ChatService {

    private final ChatMessageRepository repository;
    private final OnlineUserService onlineUserService;

    public ChatService(ChatMessageRepository repository,
                       OnlineUserService onlineUserService) {
        this.repository = repository;
        this.onlineUserService = onlineUserService;
    }
    public List<String> searchUsers(String keyword, String currentUser) {
        return repository.searchUsers(keyword, currentUser);
    }

    public List<ConversationDTO> getConversations(String currentUser) {

        List<String> users = repository.findDistinctChatUsers(currentUser);
        List<ConversationDTO> conversations = new ArrayList<>();

        // Iterates users; adds conversation with latest message
        for (String user : users) {

            Optional<ChatMessageEntity> lastSent =
                    repository.findTopBySenderAndReceiverOrderByTimestampDesc(currentUser, user);

            Optional<ChatMessageEntity> lastReceived =
                    repository.findTopBySenderAndReceiverOrderByTimestampDesc(user, currentUser);

            ChatMessageEntity latest = null;

            // Selects latest message from sent or received
            if (lastSent.isPresent() && lastReceived.isPresent()) {
                latest = lastSent.get().getTimestamp()
                        .isAfter(lastReceived.get().getTimestamp())
                        ? lastSent.get()
                        : lastReceived.get();
            } else if (lastSent.isPresent()) {
                latest = lastSent.get();
            } else if (lastReceived.isPresent()) {
                latest = lastReceived.get();
            }

            // Adds conversation
            if (latest != null) {
                conversations.add(
                        new ConversationDTO(
                                user,
                                latest.getContent(),
                                latest.getTimestamp()
                        )
                );
            }
        }

        // Sort by latest message descending
        conversations.sort((a, b) ->
                b.getTimestamp().compareTo(a.getTimestamp())
        );

        return conversations;
    }

    public Set<String> getOnlineUsers() {
        return onlineUserService.getOnlineUsers();
    }

    public Map<String, Long> getUnreadCounts(String username) {

        List<Object[]> result =
                repository.countUnreadGroupedBySender(username);

        Map<String, Long> unreadMap = new HashMap<>();

        for (Object[] row : result) {
            String sender = (String) row[0];
            Long count = (Long) row[1];
            unreadMap.put(sender, count);
        }

        return unreadMap;
    }
}