package com.assetmanager.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private AuthProvider authProvider;
    private Role role;
    private Boolean isActive;
    private String profileImageUrl;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isSocialLogin() {
        return authProvider != null && authProvider != AuthProvider.LOCAL;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean isAccountActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void changeEmail(String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("email cannot be empty");
        }
        this.email = newEmail;
    }
}
