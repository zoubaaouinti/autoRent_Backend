// src/main/java/com/autoRent/service/AuthService.java
package com.autoRent.service;

import com.autoRent.dto.AuthResponse;
import com.autoRent.dto.LoginRequest;
import com.autoRent.dto.RegisterRequest;
import com.autoRent.dto.ChangePasswordRequest;
import com.autoRent.dto.UpdateProfileRequest;
import com.autoRent.dto.UserDto;
import com.autoRent.model.User;
import com.autoRent.repository.UserRepository;
import com.autoRent.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
     private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        
        var user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .displayName(request.getDisplayName())
            .phoneNumber(request.getPhoneNumber())
            .address(request.getAddress())
            .bio(request.getBio())
            .dateOfBirth(request.getDateOfBirth())
            .role(User.UserRole.CLIENT_AUTH)
            .emailVerified(false)
            .uid(UUID.randomUUID().toString())
            .build();
            
        user = userRepository.save(user);
        
        // Send verification email
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        
        try {
            emailService.sendVerificationEmail(user.getEmail(), otp);
        } catch (Exception ex) {
            log.warn("Failed to send verification email to {}: {}", user.getEmail(), ex.getMessage());
        }

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        
        return AuthResponse.builder()
            .token(jwtToken)
            .refreshToken(refreshToken)
            .user(UserDto.fromUser(user))
            .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        
        var userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);
        
        return AuthResponse.builder()
            .token(jwtToken)
            .refreshToken(refreshToken)
            .user(UserDto.fromUser((User) userDetails))
            .build();
    }
    
    @Transactional
    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already verified");
        }

        if (!otp.equals(user.getOtp()) ||
            user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiration(null);
        userRepository.save(user);
    }
    
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        
        emailService.sendPasswordResetEmail(user.getEmail(), otp);
    }
    
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            
        if (!otp.equals(user.getOtp()) || 
            user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiration(null);
        userRepository.save(user);
    }
    
    private String generateOtp() {
        return String.format("%04d", (int) (Math.random() * 10000));
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserDto updateProfile(String email, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
            }
            user.setEmail(req.getEmail());
            user.setEmailVerified(false);
            String otp = generateOtp();
            user.setOtp(otp);
            user.setOtpExpiration(LocalDateTime.now().plusMinutes(15));
            try {
                emailService.sendVerificationEmail(user.getEmail(), otp);
            } catch (Exception ex) {
                log.warn("Failed to send verification email to {}: {}", user.getEmail(), ex.getMessage());
            }
        }

        if (req.getDisplayName() != null) user.setDisplayName(req.getDisplayName());
        if (req.getPhotoURL() != null) user.setPhotoURL(req.getPhotoURL());
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getBio() != null) user.setBio(req.getBio());
        if (req.getDateOfBirth() != null) user.setDateOfBirth(req.getDateOfBirth());

        user = userRepository.save(user);
        return UserDto.fromUser(user);
    }

    public UserDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserDto.fromUser(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserDto.fromUser(user);
    }

    @Transactional
    public UserDto updateProfilePicture(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }

        // Validate file extension (allow common image formats)
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files (jpg, png, gif, webp, bmp) are allowed");
        }

        try {
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads");
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            String filename = user.getUid() + "-" + System.currentTimeMillis() + ext;
            java.nio.file.Path target = uploadDir.resolve(filename);
            file.transferTo(target.toFile());

            // Set photoURL to served path
            String servedPath = "/uploads/" + filename;
            user.setPhotoURL(servedPath);
            user = userRepository.save(user);
            return UserDto.fromUser(user);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file: " + ex.getMessage());
        }
    }
}