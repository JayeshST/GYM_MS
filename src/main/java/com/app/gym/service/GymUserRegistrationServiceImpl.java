package com.app.gym.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.gym.audit.GymAuditEventEntity;
import com.app.gym.audit.repository.GymAuditEventRepository;
import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.common.entity.GymUserRoleEntity;
import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.dao.GymAuthorizationDao;
import com.app.gym.dto.GymRegisterUserRequestDto;
import com.app.gym.dto.GymRegisterUserResponseDto;
import com.app.gym.role.entity.GymRoleEntity;
import com.app.gym.role.repository.GymRoleRepository;
import com.app.gym.user.repository.GymUserRepository;
import com.app.gym.user.repository.GymUserRoleRepository;

@Service
public class GymUserRegistrationServiceImpl
        implements GymUserRegistrationService {

    private final GymUserRepository userRepository;
    private final GymRoleRepository roleRepository;
    private final GymUserRoleRepository userRoleRepository;
    private final GymAuthorizationDao authorizationDao;
    private final GymAuditEventRepository auditRepository;
    private final PasswordEncoder passwordEncoder;

    public GymUserRegistrationServiceImpl(
            GymUserRepository userRepository,
            GymRoleRepository roleRepository,
            GymUserRoleRepository userRoleRepository,
            GymAuthorizationDao authorizationDao,
            GymAuditEventRepository auditRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.authorizationDao = authorizationDao;
        this.auditRepository = auditRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public GymRegisterUserResponseDto registerUser(
            GymRegisterUserRequestDto request) {

        GymUserEntity actor = getValidActor();

        requirePermission(actor.getId(), "CAN-CREATE-GYM-USER");
        requirePermission(actor.getId(), "CAN-ASSIGN-GYM-USER-ROLE");

        validatePassword(request);

        String normalizedEmail =
                request.getEmail().trim().toLowerCase(Locale.ROOT);

        String roleCode =
                request.getRoleCode().trim().toUpperCase(Locale.ROOT);

        // This endpoint creates staff accounts only.
        // Creating another administrator requires a separate policy.
        if (!"TRAINER".equals(roleCode)
                && !"RECEPTIONIST".equals(roleCode)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role must be TRAINER or RECEPTIONIST");
        }

        if (userRepository.countUsersByEmail(normalizedEmail) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered");
        }

        GymRoleEntity role = roleRepository
                .findByRoleCodeAndIsActiveAndIsDeleted(roleCode, 1, 0)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Requested role is not available"));

        GymUserEntity user = new GymUserEntity();

        // Internal unique username; application login uses email.
        user.setUsername("gym_" + UUID.randomUUID().toString());

        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setMobileNumber(request.getMobileNumber().trim());
        user.setAddress(request.getAddress().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setMustChangePassword(1);
        user.setAuthVersion(0L);
        user.setIsActive(1);
        user.setIsDeleted(0);
        user.setCreatedBy(actor.getId());
        user.setUpdatedBy(actor.getId());

        user = userRepository.saveAndFlush(user);

        GymUserRoleEntity mapping = new GymUserRoleEntity();

        mapping.setUser(user);
        mapping.setRole(role);
        mapping.setIsActive(1);
        mapping.setIsDeleted(0);
        mapping.setCreatedBy(actor.getId());
        mapping.setUpdatedBy(actor.getId());

        userRoleRepository.saveAndFlush(mapping);

        // roleCode is restricted to the two fixed values above.
        String afterData =
                "{\"roleCode\":\"" + roleCode + "\","
                + "\"isActive\":1,"
                + "\"mustChangePassword\":1}";

        auditRepository.save(
                GymAuditEventEntity.success(
                        actor.getId(),
                        "USER_REGISTERED",
                        "USER",
                        user.getId(),
                        null,
                        afterData,
                        "Staff account created and role assigned"
                )
        );

        return new GymRegisterUserResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                role.getRoleCode(),
                true);
    }

    private GymUserEntity getValidActor() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof GymAuthenticatedUser)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required");
        }

        GymAuthenticatedUser principal =
                (GymAuthenticatedUser) authentication.getPrincipal();

        GymUserEntity actor = userRepository
                .findById(principal.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Session is no longer valid"));

        boolean valid =
                Integer.valueOf(1).equals(actor.getIsActive())
                && Integer.valueOf(0).equals(actor.getIsDeleted())
                && actor.getAuthVersion() != null
                && Objects.equals(
                        actor.getAuthVersion(),
                        principal.getAuthVersion());

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Session is no longer valid");
        }

        if (!Integer.valueOf(0).equals(actor.getMustChangePassword())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Change your password before registering staff");
        }

        return actor;
    }

    private void requirePermission(Long userId, String permissionCode) {

        if (!authorizationDao.hasActivePermission(userId, permissionCode)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access not available");
        }
    }

    private void validatePassword(GymRegisterUserRequestDto request) {

        String password = request.getPassword();

        if (password == null
                || password.trim().isEmpty()
                || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 12 characters "
                    + "and at most 72 UTF-8 bytes");
        }

        if (!password.equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password and confirm password do not match");
        }
    }
}