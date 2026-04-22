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

       if (!isValidEmail(request.getEmail())) {
           throw new IllegalArgumentException("Invalid email format");
       }

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
               .password(passwordEncoder.encode(request.getPassword()))
               .firstName(request.getFirstName())
               .lastName(request.getLastName())
               .fullName(buildFullName(request.getFirstName(), request.getLastName()))
               .age(request.getAge())
               .gender(request.getGender())
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

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = findByUsername(username);

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void changeEmail(String username, String currentPassword, String newEmail) {
        User user = findByUsername(username);

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }

        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("New email is required");
        }

        if (!isValidEmail(newEmail)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        String normalizedEmail = normalizeEmail(newEmail);
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getUsername().equals(user.getUsername()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        user.setEmail(normalizedEmail);
        userRepository.save(user);
    }

    public void updateStatus(String username, String status) {
        User user = findByUsername(username);
        user.setStatus(status);
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void deleteCurrentUser(String username) {
        User user = findByUsername(username);
        userRepository.delete(user);
    }

    public String getDisplayName(User user) {
        if (user == null) {
            return "";
        }

        String fullName = user.getFullName();
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }

        return buildFullName(user.getFirstName(), user.getLastName());
    }

    private String normalizeEmail(String email) {
        String trimmed = email == null ? "" : email.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    private String normalizeUsername(String username) {
        String trimmed = username == null ? "" : username.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isValidEmail(String email) {
        String trimmed = email == null ? "" : email.trim();
        return trimmed.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private String buildFullName(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();

        if (first.isEmpty()) {
            return last;
        }

        if (last.isEmpty()) {
            return first;
        }

        return first + " " + last;
    }
}
