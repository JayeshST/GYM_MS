package com.app.gym.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GymUserListItemDto {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String mobileNumber;
    private final Integer isInactive;
    private final Integer isDeleted;
    private final LocalDateTime createdAt;

    private List<GymUserRoleSummaryDto> roles =
            Collections.emptyList();

    public GymUserListItemDto(
            Long userId,
            String fullName,
            String email,
            String mobileNumber,
            Integer isInactive,
            Integer isDeleted,
            LocalDateTime createdAt) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.isInactive = isInactive;
        this.isDeleted = isDeleted;
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

    public Integer getIsInactive() {
        return isInactive;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<GymUserRoleSummaryDto> getRoles() {
        return roles;
    }

    public void setRoles(List<GymUserRoleSummaryDto> roles) {
        this.roles = Collections.unmodifiableList(
                new ArrayList<>(roles));
    }
}