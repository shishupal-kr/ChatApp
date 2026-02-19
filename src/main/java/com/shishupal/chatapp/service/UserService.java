package com.shishupal.chatapp.service;

import com.shishupal.chatapp.dto.LoginRequest;
import com.shishupal.chatapp.dto.RegisterRequest;
import com.shishupal.chatapp.entity.User;
import com.shishupal.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //inject password encoder

    public String registerUser(RegisterRequest request){
        //for debug
        System.out.println("Username: " + request.getUsername());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Password: " + request.getPassword());

       if (userRepository.findByUsername(request.getUsername()).isPresent()) {
           return "Username already exists";
       }

       if (userRepository.findByEmail(request.getEmail()).isPresent()) {
           return "Email already exists";
       }

       User user = User.builder()
               .username(request.getUsername())
               .email(request.getEmail())
               //.password(request.getPassword())
               .password(passwordEncoder.encode(request.getPassword())) //update password encoder
               .status("OFFLINE")
               .build();

       userRepository.save(user); //save user to db

       return "user registered successfully!!";
    }

    //added login logic
    public String loginUser(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return "Login successful";
    }
}
