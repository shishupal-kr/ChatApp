package com.shishupal.chatapp.repository;

import com.shishupal.chatapp.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

 // used for CRUD operations on ChatMessageEntity entity
public interface ChatMessageRepository
        extends JpaRepository<ChatMessageEntity, Long> {

    // Returns all messages between two users
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

    // Returns distinct users who have sent or received messages
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

     @Query("SELECT u.username FROM User u " +
             "WHERE u.username LIKE %:keyword% " +
             "AND u.username <> :currentUser")
     List<String> searchUsers(@Param("keyword") String keyword,
                              @Param("currentUser") String currentUser);
}