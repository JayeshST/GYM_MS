package com.app.gym.config;

import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class GymAuthenticatedUser extends User {

    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final Long authVersion;

    public GymAuthenticatedUser(
            Long userId,
            String email,
            String passwordHash,
            boolean enabled,
            Long authVersion) {

        super(
            email,
            passwordHash,
            enabled,
            true,
            true,
            true,
            Collections.<GrantedAuthority>emptyList()
        );

        this.userId = userId;
        this.authVersion = authVersion;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAuthVersion() {
        return authVersion;
    }
}