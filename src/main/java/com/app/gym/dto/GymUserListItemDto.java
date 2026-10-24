package com.app.gym.dto;

import java.time.LocalDateTime;

public class GymUserListItemDto {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String mobileNumber;
    private final Integer isActive;
    private final LocalDateTime createdAt;

    public GymUserListItemDto(
            Long userId,
            String fullName,
            String email,
            String mobileNumber,
            Integer isActive,
            LocalDateTime createdAt) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.isActive = isActive;
        this.createdAt = createdAt;
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

    public Integer getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}