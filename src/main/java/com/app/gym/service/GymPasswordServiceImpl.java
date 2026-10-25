package com.app.gym.service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.gym.audit.GymAuditEventEntity;
import com.app.gym.audit.repository.GymAuditEventRepository;
import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.dto.GymChangePasswordRequestDto;
import com.app.gym.user.repository.GymUserRepository;

@Service
public class GymPasswordServiceImpl implements GymPasswordService {

    private final GymUserRepository userRepository;
    private final GymAuditEventRepository auditRepository;
    private final PasswordEncoder passwordEncoder;

    public GymPasswordServiceImpl(
            GymUserRepository userRepository,
            GymAuditEventRepository auditRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void changePassword(GymChangePasswordRequestDto request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof GymAuthenticatedUser)) {

            throw new BadCredentialsException("Login required");
        }

        GymAuthenticatedUser principal =
                (GymAuthenticatedUser) authentication.getPrincipal();

        validateNewPassword(request);

        GymUserEntity user = userRepository
                .findUserForPasswordChange(principal.getUserId())
                .orElseThrow(() -> new BadCredentialsException(
                        "Session is no longer valid"));

        boolean validAccount =
        		Integer.valueOf(0).equals(user.getIsInactive())
                && Integer.valueOf(0).equals(user.getIsDeleted())
                && user.getAuthVersion() != null
                && Objects.equals(
                        user.getAuthVersion(),
                        principal.getAuthVersion());

        if (!validAccount) {
            throw new BadCredentialsException(
                    "Session is no longer valid");
        }

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Current password is incorrect");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "New password must be different from current password");
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword()));

        user.setMustChangePassword(0);
        user.setAuthVersion(
                Math.addExact(user.getAuthVersion(), 1L));
        user.setUpdatedBy(user.getId());

        userRepository.saveAndFlush(user);

        GymAuditEventEntity event = GymAuditEventEntity.success(
                user.getId(),
                "PASSWORD_CHANGED",
                "USER",
                user.getId(),
                null,
                null,
                "Own password changed; previous sessions revoked"
        );

        auditRepository.save(event);
    }

    private void validateNewPassword(
            GymChangePasswordRequestDto request) {

        String newPassword = request.getNewPassword();

        if (request.getCurrentPassword() == null
                || request.getCurrentPassword().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Current password is required");
        }

        if (newPassword == null
                || newPassword.trim().isEmpty()
                || newPassword.length() < 12
                || newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException(
                    "New password must contain at least 12 characters "
                    + "and at most 72 UTF-8 bytes");
        }

        if (!newPassword.equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "New password and confirm password do not match");
        }
    }
}