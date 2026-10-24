package com.app.gym.dto;

import java.time.LocalDateTime;

public class GymUserDetailsResponseDto {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String mobileNumber;
    private final String address;
    private final Integer isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public GymUserDetailsResponseDto(
            Long userId,
            String fullName,
            String email,
            String mobileNumber,
            String address,
            Integer isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}