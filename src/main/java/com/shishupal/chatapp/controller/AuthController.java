package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.dto.LoginRequest;
import com.shishupal.chatapp.dto.RegisterRequest;
import com.shishupal.chatapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
