package com.shishupal.chatapp.repository;

import com.shishupal.chatapp.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessageEntity, Long> {

    @Query(
            "SELECT m FROM ChatMessageEntity m " +
                    "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
                    "   OR (m.sender = :user2 AND m.receiver = :user1) " +
                    "ORDER BY m.timestamp ASC"
    )
    List<ChatMessageEntity> findConversation(
            @Param("user1") String user1,
            @Param("user2") String user2
    );

    @Query("""
    SELECT DISTINCT
    CASE
        WHEN m.sender = :currentUser THEN m.receiver
        ELSE m.sender
    END
    FROM ChatMessageEntity m
    WHERE m.sender = :currentUser
       OR m.receiver = :currentUser
""")
    List<String> findDistinctChatUsers(@Param("currentUser") String currentUser);

    Optional<ChatMessageEntity>
    findTopBySenderAndReceiverOrderByTimestampDesc(String sender, String receiver);
}