package com.shishupal.chatapp.controller;

import com.shishupal.chatapp.dto.RegisterRequest;
import com.shishupal.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public String register(RegisterRequest request){
        return userService.registerUser(request);
    }
}
