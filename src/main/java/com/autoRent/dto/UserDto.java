package com.autoRent.dto;

import com.autoRent.model.User;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;
    private String uid;
    private String email;
    private String displayName;
    private String photoURL;
    private boolean emailVerified;
    private String phoneNumber;
    private User.UserRole role;
    private String address;
    private String bio;
    private LocalDate dateOfBirth;
    
    public static UserDto fromUser(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUid(user.getUid());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setPhotoURL(user.getPhotoURL());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setAddress(user.getAddress());
        dto.setBio(user.getBio());
        dto.setDateOfBirth(user.getDateOfBirth());
        return dto;
    }
}