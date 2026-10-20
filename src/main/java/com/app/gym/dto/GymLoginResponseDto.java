package com.app.gym.dto;

public class GymLoginResponseDto {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final boolean mustChangePassword;

    public GymLoginResponseDto(
            Long userId,
            String fullName,
            String email,
            boolean mustChangePassword) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.mustChangePassword = mustChangePassword;
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
}