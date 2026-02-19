package com.shishupal.chatapp.service;

import com.shishupal.chatapp.dto.RegisterRequest;
import com.shishupal.chatapp.entity.User;
import com.shishupal.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
               .password(request.getPassword())
               .status("OFFLINE")
               .build();

       userRepository.save(user); //save user to db

       return "user registered successfully!!";
    }
}
