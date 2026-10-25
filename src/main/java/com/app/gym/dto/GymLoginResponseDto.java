package com.app.gym.dto;

import java.util.List;
import java.util.Objects;

public class GymLoginResponseDto {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final boolean mustChangePassword;
    private final List<GymCurrentUserRoleDto> roles;

    public GymLoginResponseDto(
            Long userId,
            String fullName,
            String email,
            boolean mustChangePassword,
            List<GymCurrentUserRoleDto> roles) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.mustChangePassword = mustChangePassword;

        this.roles = List.copyOf(
                Objects.requireNonNull(
                        roles,
                        "Roles must not be null"));
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public List<GymCurrentUserRoleDto> getRoles() {
        return roles;
    }
}