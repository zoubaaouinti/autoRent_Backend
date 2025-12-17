package com.autoRent.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String uid;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String displayName;
    private String photoURL;
    private boolean emailVerified;
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    private String address;
    private String bio;
    private LocalDate dateOfBirth;
    
    @Column(nullable = false)
    private String password;
    
    private String otp;
    private LocalDateTime otpExpiration;

    @Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
}

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified;
    }

   public enum UserRole {
    CLIENT,          // Client non authentifié
    CLIENT_AUTH,     // Client authentifié
    DRIVER,          // Chauffeur (Partenaire)
    FLEET_MANAGER,   // Gestionnaire de Flotte
    CUSTOMER_SUPPORT, // Support Client
    ADMIN        // Administrateur
   
}
}