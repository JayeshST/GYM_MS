package com.app.gym.dto;

public class GymRegisterUserResponseDto {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String roleCode;
    private final boolean mustChangePassword;

    public GymRegisterUserResponseDto(
            Long userId,
            String fullName,
            String email,
            String roleCode,
            boolean mustChangePassword) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.roleCode = roleCode;
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

    public String getRoleCode() {
        return roleCode;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }
}