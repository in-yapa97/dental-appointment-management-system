package com.dental.management.dto;

import com.dental.management.entity.User;
import com.dental.management.entity.enums.UserRole;

import java.time.Instant;

/**
 * Safe user representation returned by REST endpoints.
 * Never includes passwords or password hashes.
 */
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private UserRole role;
    private boolean active;
    private Instant createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, String fullName, UserRole role, boolean active, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
