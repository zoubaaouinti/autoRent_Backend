// src/main/java/com/autoRent/service/EmailService.java
package com.autoRent.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public void sendVerificationEmail(String to, String otp) {
        String subject = "Vérification de votre email";
        String text = "Votre code de vérification est : " + otp;
        sendEmail(to, subject, text);
    }
    
    public void sendPasswordResetEmail(String to, String otp) {
        String subject = "Réinitialisation de votre mot de passe";
        String text = "Votre code de réinitialisation est : " + otp;
        sendEmail(to, subject, text);
    }
    
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}