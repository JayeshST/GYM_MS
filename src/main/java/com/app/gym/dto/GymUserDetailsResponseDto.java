package com.app.gym.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GymUserDetailsResponseDto {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String mobileNumber;
    private final String address;
    private final Integer isInactive;
    private final Integer isDeleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private List<GymUserRoleSummaryDto> roles =
            Collections.emptyList();

    public GymUserDetailsResponseDto(
            Long userId,
            String fullName,
            String email,
            String mobileNumber,
            String address,
            Integer isInactive,
            Integer isDeleted,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.isInactive = isInactive;
        this.isDeleted = isDeleted;
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

    public Integer getIsInactive() {
        return isInactive;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<GymUserRoleSummaryDto> getRoles() {
        return roles;
    }

    public void setRoles(List<GymUserRoleSummaryDto> roles) {
        this.roles = Collections.unmodifiableList(
                new ArrayList<>(roles));
    }
}