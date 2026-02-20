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

    public String registerUser(RegisterRequest request){

       if (userRepository.findByUsername(request.getUsername()).isPresent()) {
           throw new IllegalArgumentException("Username already exists");
       }

       if (userRepository.findByEmail(request.getEmail()).isPresent()) {
           throw new IllegalArgumentException("Email already exists");
       }

       User user = User.builder()
               .username(request.getUsername())
               .email(request.getEmail())
               //.password(request.getPassword())
               .password(passwordEncoder.encode(request.getPassword())) //update password encoder
               .status("OFFLINE")
               .build();

       userRepository.save(user); //save user to db

       return "Registration successful";
    }

    //added login logic
    public String loginUser(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        //return "Login successful";
        return jwtService.generateToken(user.getUsername());
    }
}
