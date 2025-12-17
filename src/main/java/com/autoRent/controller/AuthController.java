// src/main/java/com/autoRent/controller/AuthController.java
package com.autoRent.controller;

import com.autoRent.dto.AuthResponse;
import com.autoRent.dto.LoginRequest;
import com.autoRent.dto.RegisterRequest;
import com.autoRent.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @RequestParam String email,
            @RequestParam String otp) {
        authService.verifyEmail(email, otp);
        return ResponseEntity.ok("Email verified successfully");
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok("Password reset OTP sent to your email");
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        authService.resetPassword(email, otp, newPassword);
        return ResponseEntity.ok("Password reset successful");
    }
}