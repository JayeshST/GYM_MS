package com.app.gym.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GymMyAccessResponseDto {

    private final Long userId;
    private final List<String> permissions;

    public GymMyAccessResponseDto(
            Long userId,
            List<String> permissions) {

        this.userId = userId;
        this.permissions = Collections.unmodifiableList(
                new ArrayList<>(permissions));
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}