package com.autoRent.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String displayName;
    private String photoURL;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private String bio;
}
