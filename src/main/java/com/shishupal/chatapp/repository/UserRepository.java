package com.shishupal.chatapp.repository;

import com.shishupal.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

// used for CRUD operations on User entity
@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
        SELECT u.username
        FROM User u
        WHERE u.username LIKE %:keyword%
        AND u.username <> :currentUser
    """)
    List<String> searchByUsername(@Param("keyword") String keyword,
                                   @Param("currentUser") String currentUser);

}
