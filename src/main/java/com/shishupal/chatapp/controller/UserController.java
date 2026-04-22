package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.entity.User;
import com.shishupal.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        User user = userService.findByUsername(username);

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("fullName", userService.getDisplayName(user));
        response.put("status", user.getStatus());

        return ResponseEntity.ok(response);
    }
}
