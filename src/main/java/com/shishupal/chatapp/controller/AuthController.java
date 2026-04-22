package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.dto.LoginRequest;
import com.shishupal.chatapp.dto.RegisterRequest;
import com.shishupal.chatapp.entity.User;
import com.shishupal.chatapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request){
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {

        User user = userService.findByUsername(principal.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("fullName", userService.getDisplayName(user));
        response.put("email", user.getEmail());
        response.put("status", user.getStatus());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        userService.deleteCurrentUser(principal.getName());
        return ResponseEntity.ok("Account deleted");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Principal principal, @RequestBody Map<String, String> request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        userService.changePassword(
                principal.getName(),
                request.get("currentPassword"),
                request.get("newPassword")
        );
        return ResponseEntity.ok("Password updated");
    }

    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(Principal principal, @RequestBody Map<String, String> request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        userService.changeEmail(
                principal.getName(),
                request.get("currentPassword"),
                request.get("newEmail")
        );
        return ResponseEntity.ok("Email updated");
    }
}
