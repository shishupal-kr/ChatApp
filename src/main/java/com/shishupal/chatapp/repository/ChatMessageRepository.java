package com.shishupal.chatapp.repository;

import com.shishupal.chatapp.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessageEntity, Long> {

    @Query("""
    SELECT c FROM ChatMessageEntity c
    WHERE (LOWER(c.sender) = LOWER(:user1) AND LOWER(c.receiver) = LOWER(:user2))
       OR (LOWER(c.sender) = LOWER(:user2) AND LOWER(c.receiver) = LOWER(:user1))
    ORDER BY c.timestamp ASC
""")
    List<ChatMessageEntity> findChatHistory(
            @Param("user1") String user1,
            @Param("user2") String user2
    );
}