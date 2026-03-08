package com.shishupal.chatapp.service;

import com.shishupal.chatapp.dto.LoginRequest;
import com.shishupal.chatapp.dto.RegisterRequest;
import com.shishupal.chatapp.entity.User;
import com.shishupal.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.shishupal.chatapp.service.JwtService;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //inject password encoder
    private final JwtService jwtService;

    // Registers user; throws exception if username/email exists
    public String registerUser(RegisterRequest request){

       String normalizedEmail = normalizeEmail(request.getEmail());

       if (userRepository.findByUsername(request.getUsername()).isPresent()) {
           throw new IllegalArgumentException("Username already exists");
       }

       if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
           throw new IllegalArgumentException("Email already exists");
       }

       // Save user to DB
       User user = User.builder()
               .username(request.getUsername())
               .email(normalizedEmail)
               //.password(request.getPassword())
               .password(passwordEncoder.encode(request.getPassword())) //update password encoder
               .status("OFFLINE")
               .build();

       userRepository.save(user); //save user to db

       return "Registration successful";
    }

    //added login logic
    public String loginUser(LoginRequest request) {

        String normalizedUsername = normalizeUsername(request.getUsername());

        User user = userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        //return "Login successful";
        return jwtService.generateToken(user.getUsername());
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String normalizeEmail(String email) {
        String trimmed = email == null ? "" : email.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    private String normalizeUsername(String username) {
        String trimmed = username == null ? "" : username.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
