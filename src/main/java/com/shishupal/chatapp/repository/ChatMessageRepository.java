package com.shishupal.chatapp.repository;

import com.shishupal.chatapp.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

 // used for CRUD operations on ChatMessageEntity entity
@Repository
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

    @Query(
            "SELECT m FROM ChatMessageEntity m " +
            "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
            "   OR (m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.timestamp DESC"
    )
    Page<ChatMessageEntity> findConversationPaginated(
            @Param("user1") String user1,
            @Param("user2") String user2,
            Pageable pageable
    );

    @Query("""
        SELECT m.sender, COUNT(m)
        FROM ChatMessageEntity m
        WHERE m.receiver = :username
          AND m.status <> com.shishupal.chatapp.entity.ChatMessageEntity.MessageStatus.READ
        GROUP BY m.sender
    """)
    List<Object[]> countUnreadGroupedBySender(@Param("username") String username);

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

}