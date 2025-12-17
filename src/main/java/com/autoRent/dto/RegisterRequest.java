package com.autoRent.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String displayName;
    private String phoneNumber;
    private String address;
    private String bio;
    private LocalDate dateOfBirth;
}